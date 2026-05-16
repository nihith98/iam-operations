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

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Generates RS256-signed JWT access tokens for the {@code /auth/login} flow.
 * Loads the RSA key pair from {@code JWT_PRIVATE_KEY} and {@code JWT_PUBLIC_KEY}
 * environment variables (base64-encoded PEM body, headers stripped) at startup.
 * The {@code kid} claim ({@value #DEFAULT_KEY_ID}) identifies the key for downstream
 * JWKS-based verification.
 */
@Component
@Scope(value = BeanDefinition.SCOPE_SINGLETON)
public class JwtUtil {

    private static final Logger logger = LogManager.getLogger(JwtUtil.class);

    public static final String JWT_PRIVATE_KEY = "JWT_PRIVATE_KEY";
    public static final String JWT_PUBLIC_KEY = "JWT_PUBLIC_KEY";
    public static final String JWT_ISSUER = "JWT_ISSUER";

    public static final String DEFAULT_KEY_ID = "breakdown-key-v1";
    public static final String DEFAULT_ISSUER = "breakdown-auth";

    public static final long ACCESS_TOKEN_TTL_SECONDS = 15L * 60L;

    private PrivateKey privateKey;
    private PublicKey publicKey;
    private String issuer;

    /**
     * Reads the configured key material from the environment and parses it into
     * {@link PrivateKey} / {@link PublicKey} instances. Invoked by the Spring
     * container after dependency injection.
     */
    @PostConstruct
    public void init() {
        logger.info("Initializing JwtUtil");
        String privateKeyMaterial = EnvironmentUtil.getEnvironmentVariable(JWT_PRIVATE_KEY);
        String publicKeyMaterial = EnvironmentUtil.getEnvironmentVariable(JWT_PUBLIC_KEY);
        String configuredIssuer = EnvironmentUtil.getEnvironmentVariable(JWT_ISSUER);
        this.issuer = (configuredIssuer == null || configuredIssuer.isBlank()) ? DEFAULT_ISSUER : configuredIssuer;

        try {
            this.privateKey = parsePrivateKey(privateKeyMaterial);
            this.publicKey = parsePublicKey(publicKeyMaterial);
            logger.info("JwtUtil initialized with issuer::{}", this.issuer);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            throw new IAMException("Failed to initialize JwtUtil RSA key pair");
        }
    }

    /**
     * Signs a short-lived access token for the supplied {@link User} carrying the
     * subject, email, roles, issued-at, expiry, JWT id, and {@code kid} header.
     *
     * @param user the authenticated user to mint a token for
     * @return the compact serialised JWT
     * @throws IAMException if signing fails
     */
    public String generateAccessToken(User user) {
        try {
            Instant now = Instant.now();
            Instant exp = now.plusSeconds(ACCESS_TOKEN_TTL_SECONDS);

            return Jwts.builder()
                    .header().keyId(DEFAULT_KEY_ID).and()
                    .id(UUID.randomUUID().toString())
                    .issuer(this.issuer)
                    .subject(user.getUserId())
                    .claim("email", user.getEmail())
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
