package com.nihith.iam.util;

import com.nihith.iam.exception.IAMException;
import com.nihith.iam.model.Role;
import com.nihith.iam.model.User;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Generates RS256-signed JWT access tokens for the {@code /auth/login} flow.
 * Loads the RSA key pair from a keystore file configured via environment variables:
 * {@value #KEYSTORE_PATH}, {@value #KEYSTORE_PASSWORD}, {@value #KEYSTORE_ALIAS},
 * and optionally {@value #KEYSTORE_KEY_PASSWORD} at startup.
 * The {@code kid} claim ({@value #DEFAULT_KEY_ID}) identifies the key for downstream
 * JWKS-based verification.
 */
@Component
@Scope(value = BeanDefinition.SCOPE_SINGLETON)
public class JwtUtil {

    private static final Logger logger = LogManager.getLogger(JwtUtil.class);

    public static final String KEYSTORE_PATH = "KEYSTORE_PATH";
    public static final String KEYSTORE_PASSWORD = "KEYSTORE_PASSWORD";
    public static final String KEYSTORE_ALIAS = "KEYSTORE_ALIAS";
    public static final String KEYSTORE_KEY_PASSWORD = "KEYSTORE_KEY_PASSWORD";
    public static final String JWT_ISSUER = "JWT_ISSUER";

    public static final String DEFAULT_KEY_ID = "breakdown-key-v1";
    public static final String DEFAULT_ISSUER = "breakdown-auth";

    public static final long ACCESS_TOKEN_TTL_SECONDS = 15L * 60L;

    private PrivateKey privateKey;
    private PublicKey publicKey;
    private String issuer;

    /**
     * Reads RSA keys from a keystore file specified by environment variables.
     * Keystore configuration is read from: KEYSTORE_PATH, KEYSTORE_PASSWORD,
     * KEYSTORE_ALIAS, and KEYSTORE_KEY_PASSWORD. Invoked by the Spring
     * container after dependency injection.
     */
    @PostConstruct
    public void init() {
        logger.info("Initializing JwtUtil");
        String keystorePath = EnvironmentUtil.getEnvironmentVariable(KEYSTORE_PATH);
        String keystorePassword = EnvironmentUtil.getEnvironmentVariable(KEYSTORE_PASSWORD);
        String keystoreAlias = EnvironmentUtil.getEnvironmentVariable(KEYSTORE_ALIAS);
        String keystoreKeyPassword = EnvironmentUtil.getEnvironmentVariable(KEYSTORE_KEY_PASSWORD);
        String configuredIssuer = EnvironmentUtil.getEnvironmentVariable(JWT_ISSUER);
        this.issuer = (configuredIssuer == null || configuredIssuer.isBlank()) ? DEFAULT_ISSUER : configuredIssuer;

        // BUG FIX: Changed from reading base64-encoded key material from environment to loading from keystore file
        try {
            loadKeysFromKeystore(keystorePath, keystorePassword, keystoreAlias, keystoreKeyPassword);
            logger.info("JwtUtil initialized with issuer::{}", this.issuer);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            throw new IAMException("Failed to initialize JwtUtil RSA key pair from keystore");
        }
    }

    /**
     * Loads RSA private and public keys from a keystore file.
     *
     * @param keystorePath path to the keystore file
     * @param keystorePassword password to access the keystore
     * @param keystoreAlias the certificate alias to extract
     * @param keystoreKeyPassword password for the private key entry
     * @throws Exception if keystore loading or key extraction fails
     */
    private void loadKeysFromKeystore(String keystorePath, String keystorePassword, String keystoreAlias, String keystoreKeyPassword) throws Exception {
        if (keystorePath == null || keystorePath.isBlank()) {
            throw new IAMException("Keystore path not configured in environment");
        }
        if (keystorePassword == null || keystorePassword.isBlank()) {
            throw new IAMException("Keystore password not configured in environment");
        }
        if (keystoreAlias == null || keystoreAlias.isBlank()) {
            throw new IAMException("Keystore alias not configured in environment");
        }

        String effectiveKeyPassword = (keystoreKeyPassword == null || keystoreKeyPassword.isBlank()) ? keystorePassword : keystoreKeyPassword;

        try (FileInputStream keyStoreFile = new FileInputStream(keystorePath)) {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(keyStoreFile, keystorePassword.toCharArray());

            if (!keyStore.containsAlias(keystoreAlias)) {
                throw new IAMException("Certificate alias '" + keystoreAlias + "' not found in keystore");
            }

            this.privateKey = (PrivateKey) keyStore.getKey(keystoreAlias, effectiveKeyPassword.toCharArray());
            if (this.privateKey == null) {
                throw new IAMException("Failed to retrieve private key for alias '" + keystoreAlias + "'");
            }

            Certificate certificate = keyStore.getCertificate(keystoreAlias);
            if (certificate == null) {
                throw new IAMException("Certificate not found for alias '" + keystoreAlias + "'");
            }

            this.publicKey = certificate.getPublicKey();
            if (this.publicKey == null) {
                throw new IAMException("Public key not found in certificate for alias '" + keystoreAlias + "'");
            }
        }
    }

    /**
     * Signs a short-lived access token for the supplied {@link User} carrying the
     * subject, username, roles, userId, displayName, issued-at, expiry, JWT id,
     * and {@code kid} header. No email or other PII is included in the token per
     * the platform branding philosophy (minimal PII: username only).
     *
     * @param user the authenticated user to mint a token for
     * @return the compact serialised JWT
     * @throws IAMException if signing fails
     */
    public String generateAccessToken(User user) {
        try {
            Instant now = Instant.now();
            Instant exp = now.plusSeconds(ACCESS_TOKEN_TTL_SECONDS);

            // BUG FIX: Add displayName and userId as explicit JWT claims for client access
            return Jwts.builder()
                    .header().keyId(DEFAULT_KEY_ID).and()
                    .id(UUID.randomUUID().toString())
                    .issuer(this.issuer)
                    .subject(user.getUserId())
                    .claim("username", user.getUsername())
                    .claim("userId", user.getUserId())
                    .claim("displayName", user.getDisplayName() != null ? user.getDisplayName() : user.getUsername())
                    .claim("roles", extractRoleNames(user.getRoles()))
                    .issuedAt(Date.from(now))
                    .expiration(Date.from(exp))
                    .signWith(this.privateKey, Jwts.SIG.RS256)
                    .compact();
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            throw new IAMException("Failed to sign JWT");
        }
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public long getAccessTokenTtlSeconds() {
        return ACCESS_TOKEN_TTL_SECONDS;
    }

    private List<String> extractRoleNames(List<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }
        return roles.stream().map(Role::getRoleName).toList();
    }

    private PrivateKey parsePrivateKey(String pem) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(stripPemBody(pem));
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
        return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
    }

    private PublicKey parsePublicKey(String pem) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(stripPemBody(pem));
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
        return KeyFactory.getInstance("RSA").generatePublic(keySpec);
    }

    private String stripPemBody(String pem) {
        if (pem == null) {
            throw new IAMException("Missing RSA key material in environment");
        }
        return new String(pem.getBytes(StandardCharsets.UTF_8))
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
    }
}
