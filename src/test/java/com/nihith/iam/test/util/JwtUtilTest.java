package com.nihith.iam.test.util;

import com.nihith.iam.model.Role;
import com.nihith.iam.model.User;
import com.nihith.iam.util.EnvironmentUtil;
import com.nihith.iam.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JwtUtilTest {

    private JwtUtil jwtUtil;
    private KeyPair keyPair;

    @BeforeAll
    void setUp() throws Exception {
        // Arrange — generate a real RSA keypair, base64 it and seed it into the
        // EnvironmentUtil-backed system properties so JwtUtil.init() can pick it up.
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        keyPair = generator.generateKeyPair();

        String privateKeyB64 = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
        String publicKeyB64 = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());

        System.setProperty(JwtUtil.JWT_PRIVATE_KEY, privateKeyB64);
        System.setProperty(JwtUtil.JWT_PUBLIC_KEY, publicKeyB64);
        System.setProperty(JwtUtil.JWT_ISSUER, "test-issuer");

        jwtUtil = new JwtUtil();
        jwtUtil.init();
    }

    @AfterAll
    void cleanup() {
        System.clearProperty(JwtUtil.JWT_PRIVATE_KEY);
        System.clearProperty(JwtUtil.JWT_PUBLIC_KEY);
        System.clearProperty(JwtUtil.JWT_ISSUER);
    }

    @Test
    void generateAccessToken_ValidUser_ReturnsParseableJwt() {
        // Arrange
        User user = new User("u-1", "alice", "alice@example.com", "hash");
        user.setRoles(List.of(new Role("r-1", "admin"), new Role("r-2", "user")));

        // Act
        String token = jwtUtil.generateAccessToken(user);

        // Assert — token parses with the matching public key and carries the claims
        assertNotNull(token);
        Jws<Claims> parsed = Jwts.parser()
                .verifyWith(keyPair.getPublic())
                .build()
                .parseSignedClaims(token);

        Claims claims = parsed.getPayload();
        assertEquals("u-1", claims.getSubject());
        assertEquals("alice@example.com", claims.get("email", String.class));
        assertEquals("test-issuer", claims.getIssuer());
        assertNotNull(claims.getId());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());

        @SuppressWarnings("unchecked")
        List<String> roleClaim = claims.get("roles", List.class);
        assertEquals(List.of("admin", "user"), roleClaim);

        assertEquals(JwtUtil.DEFAULT_KEY_ID, parsed.getHeader().get("kid"));
    }

    @Test
    void generateAccessToken_UserWithoutRoles_ProducesTokenWithEmptyRolesClaim() {
        // Arrange
        User user = new User("u-2", "bob", "bob@example.com", "hash");

        // Act
        String token = jwtUtil.generateAccessToken(user);

        // Assert
        Claims claims = Jwts.parser()
                .verifyWith(keyPair.getPublic())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        @SuppressWarnings("unchecked")
        List<String> roleClaim = claims.get("roles", List.class);
        assertTrue(roleClaim.isEmpty());
    }

    @Test
    void getAccessTokenTtlSeconds_ReturnsConfiguredValue() {
        // Act + Assert
        assertEquals(JwtUtil.ACCESS_TOKEN_TTL_SECONDS, jwtUtil.getAccessTokenTtlSeconds());
        assertEquals(15L * 60L, jwtUtil.getAccessTokenTtlSeconds());
    }

    @Test
    void getPublicKey_AfterInit_ReturnsConfiguredPublicKey() {
        // Act + Assert
        assertNotNull(jwtUtil.getPublicKey());
        assertEquals(keyPair.getPublic(), jwtUtil.getPublicKey());
    }

    @Test
    void environmentUtilWiring_PropertyTakesPrecedence() {
        // Sanity check that the EnvironmentUtil contract used in init() resolves
        // the system property we set above. Acts as a guard against accidental
        // regressions in EnvironmentUtil's lookup order.
        assertNotNull(EnvironmentUtil.getEnvironmentVariable(JwtUtil.JWT_PRIVATE_KEY));
        assertEquals("test-issuer", EnvironmentUtil.getEnvironmentVariable(JwtUtil.JWT_ISSUER));
    }
}
