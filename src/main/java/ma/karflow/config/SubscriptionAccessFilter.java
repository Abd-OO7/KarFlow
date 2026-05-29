package ma.karflow.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.karflow.feature.subscription.entity.Subscription;
import ma.karflow.feature.subscription.enums.SubscriptionStatus;
import ma.karflow.feature.subscription.repository.SubscriptionRepository;
import ma.karflow.shared.util.TenantContext;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionAccessFilter extends OncePerRequestFilter {

    private final SubscriptionRepository subscriptionRepository;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        UUID tenantId = TenantContext.getTenantId();

        if (tenantId != null) {
            Optional<Subscription> subscriptionOpt = subscriptionRepository.findByTenantId(tenantId);

            if (subscriptionOpt.isPresent()) {
                Subscription subscription = subscriptionOpt.get();
                SubscriptionStatus status = subscription.getStatus();

                if (status == SubscriptionStatus.EXPIRED || status == SubscriptionStatus.SUSPENDED) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding("UTF-8");

                    Map<String, Object> body = Map.of(
                            "success", false,
                            "code", "SUBSCRIPTION_REQUIRED",
                            "message", "Votre abonnement a expiré",
                            "timestamp", LocalDateTime.now().toString()
                    );

                    objectMapper.writeValue(response.getOutputStream(), body);
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/v1/")
                || path.startsWith("/api/v1/public/")
                || path.startsWith("/api/v1/auth/")
                || path.startsWith("/api/v1/subscription/")
                || path.startsWith("/api/v1/subscription")
                || path.startsWith("/api/v1/platform/")
                || path.startsWith("/api/v1/health");
    }
}
