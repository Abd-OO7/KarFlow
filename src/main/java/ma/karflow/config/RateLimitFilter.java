package ma.karflow.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate limiter for auth endpoints.
 * 5 requests per minute per IP, 20 per hour.
 * Uses in-memory ConcurrentHashMap (sufficient for single-instance deployments).
 */
@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, RateBucket> minuteBuckets = new ConcurrentHashMap<>();
    private final Map<String, RateBucket> hourBuckets = new ConcurrentHashMap<>();

    private static final int MAX_PER_MINUTE = 5;
    private static final int MAX_PER_HOUR = 20;
    private static final long ONE_MINUTE_MS = 60_000L;
    private static final long ONE_HOUR_MS = 3_600_000L;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String clientIp = getClientIp(request);

        // Check minute bucket
        RateBucket minuteBucket = minuteBuckets.compute(clientIp, (key, bucket) -> {
            long now = System.currentTimeMillis();
            if (bucket == null || now - bucket.windowStart > ONE_MINUTE_MS) {
                return new RateBucket(now);
            }
            return bucket;
        });

        // Check hour bucket
        RateBucket hourBucket = hourBuckets.compute(clientIp, (key, bucket) -> {
            long now = System.currentTimeMillis();
            if (bucket == null || now - bucket.windowStart > ONE_HOUR_MS) {
                return new RateBucket(now);
            }
            return bucket;
        });

        int minuteCount = minuteBucket.counter.incrementAndGet();
        int hourCount = hourBucket.counter.incrementAndGet();

        if (minuteCount > MAX_PER_MINUTE) {
            long retryAfterSec = (ONE_MINUTE_MS - (System.currentTimeMillis() - minuteBucket.windowStart)) / 1000 + 1;
            sendTooManyRequests(response, retryAfterSec, clientIp);
            return;
        }

        if (hourCount > MAX_PER_HOUR) {
            long retryAfterSec = (ONE_HOUR_MS - (System.currentTimeMillis() - hourBucket.windowStart)) / 1000 + 1;
            sendTooManyRequests(response, retryAfterSec, clientIp);
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/v1/auth/");
    }

    private void sendTooManyRequests(HttpServletResponse response, long retryAfterSec, String ip) throws IOException {
        log.warn("Rate limit exceeded for IP: {}", ip);
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader("Retry-After", String.valueOf(Math.max(retryAfterSec, 1)));
        response.setContentType("application/json");
        response.getWriter().write("""
                {"success":false,"message":"Trop de tentatives. Réessayez dans %d secondes.","data":null}
                """.formatted(Math.max(retryAfterSec, 1)));
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class RateBucket {
        final long windowStart;
        final AtomicInteger counter;

        RateBucket(long windowStart) {
            this.windowStart = windowStart;
            this.counter = new AtomicInteger(0);
        }
    }
}
