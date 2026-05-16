package com.nihith.iam.util;

import com.nihith.iam.exception.IAMException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * SHA-256 hashing for opaque refresh tokens. Only the hash is stored in the
 * {@code tokens} collection; the raw token value never touches MongoDB so a database
 * dump cannot be replayed against {@code POST /auth/refresh}.
 */
public class TokenHashUtil {

    public static final Logger logger = LogManager.getLogger(TokenHashUtil.class);

    private static final String SHA_256 = "SHA-256";

    /**
     * Computes a hex-encoded SHA-256 digest of the supplied refresh token value.
     *
     * @param tokenValue the raw refresh token (opaque UUID) to hash
     * @return the lowercase hex SHA-256 digest
     * @throws IAMException if the platform does not support SHA-256
     */
    public static String sha256Hex(String tokenValue) {
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA_256);
            byte[] hashBytes = digest.digest(tokenValue.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            throw new IAMException("SHA-256 algorithm unavailable");
        }
    }
}
