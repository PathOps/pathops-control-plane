package io.pathops.controlplane.utils;

import org.springframework.security.oauth2.jwt.Jwt;

public final class JwtUtils {

    private JwtUtils() {
    }

    public static String getSubject(Jwt jwt) {
        return jwt.getSubject();
    }

    public static String getIssuer(Jwt jwt) {
        return jwt.getIssuer() != null ? jwt.getIssuer().toString() : null;
    }

    public static String getPreferredUsername(Jwt jwt) {
        return jwt.getClaimAsString("preferred_username");
    }

    public static String getEmail(Jwt jwt) {
        return jwt.getClaimAsString("email");
    }
}
