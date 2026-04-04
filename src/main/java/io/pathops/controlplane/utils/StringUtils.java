package io.pathops.controlplane.utils;

public final class StringUtils {

	private StringUtils() {
	}

	public static boolean equalsNullable(String a, String b) {
		return a == null ? b == null : a.equals(b);
	}
	
	public static String truncateErrorMessage(String value) {
        if (value == null) {
            return null;
        }
        return value.length() <= 4000 ? value : value.substring(0, 4000);
    }
}
