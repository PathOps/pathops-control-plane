package io.pathops.controlplane.utils;

import java.util.Locale;
import java.util.UUID;

public final class TenantUtils {

    private TenantUtils() {
    }
    
    public static String defaultTenantName(String preferredUsername) {
        if (preferredUsername == null || preferredUsername.isBlank()) {
            return "Personal Tenant";
        }
        return preferredUsername + " Personal Tenant";
    }

    public static String generateTenantSlug(String preferredUsername) {
        String base = preferredUsername == null || preferredUsername.isBlank()
            ? "tenant"
            : preferredUsername.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");

        if (base.isBlank()) {
            base = "tenant";
        }

        return base + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
