package ma.karflow.shared.util;

import java.util.UUID;

/**
 * ThreadLocal pour stocker le tenantId de la requête courante.
 * Alimenté par le JWT filter (F-02), consommé par le TenantFilter Hibernate.
 */
public final class TenantContext {

    private static final ThreadLocal<UUID> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static UUID getTenantId() {
        return CURRENT_TENANT.get();
    }

    public static void setTenantId(UUID tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
