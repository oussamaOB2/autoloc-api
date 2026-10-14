package tn.esprit.autoloc;

public final class Utils {

    private Utils() {
    }

    /** Returns true if the string is not null and contains at least one non-blank character. */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
