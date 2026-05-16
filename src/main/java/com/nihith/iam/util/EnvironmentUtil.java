package com.nihith.iam.util;

import java.util.Optional;

/**
 * Single access point for all environment-based configuration. Checks JVM system
 * properties first, then falls back to OS environment variables. All callers across
 * the auth stack must use this utility — never call {@link System#getenv(String)}
 * or {@link System#getProperty(String)} directly.
 */
public class EnvironmentUtil {

    /**
     * Retrieves the value for the given key by first checking JVM system properties
     * ({@link System#getProperty(String)}) and then falling back to OS environment
     * variables ({@link System#getenv(String)}).
     *
     * @param key the property or environment variable name to look up
     * @return the resolved value, or {@code null} if the key is not set in either source
     */
    public static String getEnvironmentVariable(String key) {
        return Optional.ofNullable(System.getProperty(key)).orElse(System.getenv(key));
    }
}
