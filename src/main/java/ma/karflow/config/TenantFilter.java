package ma.karflow.config;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import ma.karflow.shared.util.TenantContext;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * Filtre HTTP qui active le Hibernate Filter "tenantFilter"
 * pour isoler automatiquement les données par tenant.
 *
 * Le tenantId est injecté dans le TenantContext par le JWT filter (F-02).
 * Ce filtre se contente de le lire et d'activer le filtre Hibernate.
 */
@Component
@RequiredArgsConstructor
public class TenantFilter extends OncePerRequestFilter {

    private final EntityManager entityManager;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            UUID tenantId = TenantContext.getTenantId();
            // Don't apply tenant filter for SUPER_ADMIN or platform endpoints
            if (tenantId != null && !isSuperAdmin()) {
                Session session = entityManager.unwrap(Session.class);
                session.enableFilter("tenantFilter").setParameter("tenantId", tenantId);
            }
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private boolean isSuperAdmin() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/v1/auth")
                || path.startsWith("/api/v1/public")
                || path.startsWith("/api/v1/platform")
                || path.startsWith("/api/v1/client")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/api-docs")
                || path.startsWith("/actuator");
    }
}
