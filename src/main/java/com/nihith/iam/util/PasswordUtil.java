package com.nihith.iam.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Static wrapper around {@link BCryptPasswordEncoder} for hashing plaintext passwords
 * at registration and verifying them during login. The encoder instance is shared
 * across all callers — BCrypt encoders are thread-safe and stateless.
 */
public class PasswordUtil {

    public static final Logger logger = LogManager.getLogger(PasswordUtil.class);

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * Hashes the given plaintext password using BCrypt with a random salt.
     *
     * @param plaintext the raw password to hash
     * @return the BCrypt-encoded password digest
     */
    public static String hash(String plaintext) {
        return encoder.encode(plaintext);
    }

    /**
     * Constant-time comparison of a plaintext password against a stored BCrypt hash.
     *
     * @param plaintext the raw password supplied by the user
     * @param hash      the stored BCrypt hash from the user record
     * @return {@code true} if the password matches, {@code false} otherwise
     */
    public static boolean matches(String plaintext, String hash) {
        return encoder.matches(plaintext, hash);
    }
}
