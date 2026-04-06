package io.pathops.controlplane.utils;

public final class PathOpsUtils {

    private PathOpsUtils() {
    }
    
    public static boolean isPathopsRealmIssuer(String issuer) {
    	return issuer != null 
    			&& issuer.equals(Constants.KEYCLOAK_ISSUER);
    }
}