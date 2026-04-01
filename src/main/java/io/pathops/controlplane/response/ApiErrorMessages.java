package io.pathops.controlplane.response;

public final class ApiErrorMessages {                                                                                                   
    
    private ApiErrorMessages() {
        throw new IllegalStateException("Utility class");                                                                               
    }

    public static final String NOT_FOUND = "Not Found";                                                                                 
    public static final String FORBIDDEN = "Forbidden";
    public static final String NOT_VALID = "Not Valid";
 }
