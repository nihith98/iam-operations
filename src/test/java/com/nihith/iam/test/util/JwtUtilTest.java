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

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JwtUtilTest {

    private JwtUtil jwtUtil;
    private KeyPair keyPair;
    private Path keystorePath;
    private static final String KEYSTORE_PASSWORD = "test-keystore-password";
    private static final String KEYSTORE_ALIAS = "breakdown-auth-key";

    @BeforeAll
    void setUp() throws Exception {
        // Arrange — generate a test keystore
        keystorePath = Files.createTempFile("test-keystore", ".jks");
        Files.delete(keystorePath); // Delete the empty file so keytool can create a new one
        createTestKeystore(keystorePath.toString());

        System.setProperty(JwtUtil.KEYSTORE_PATH, keystorePath.toString());
        System.setProperty(JwtUtil.KEYSTORE_PASSWORD, KEYSTORE_PASSWORD);
        System.setProperty(JwtUtil.KEYSTORE_ALIAS, KEYSTORE_ALIAS);
        System.setProperty(JwtUtil.KEYSTORE_KEY_PASSWORD, KEYSTORE_PASSWORD);
        System.setProperty(JwtUtil.JWT_ISSUER, "test-issuer");

        jwtUtil = new JwtUtil();
        jwtUtil.init();
    }

    private void createTestKeystore(String keystorePath) throws Exception {
        // Get the keytool path from JAVA_HOME
        String javaHome = System.getProperty("java.home");
        String keytoolPath = javaHome + "/bin/keytool";

        ProcessBuilder pb = new ProcessBuilder(
            keytoolPath, "-genkeypair", "-alias", KEYSTORE_ALIAS,
            "-keyalg", "RSA", "-keysize", "2048",
            "-keystore", keystorePath,
            "-storepass", KEYSTORE_PASSWORD,
            "-keypass", KEYSTORE_PASSWORD,
            "-dname", "CN=breakdown-auth",
            "-validity", "365", "-noprompt"
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();

        StringBuilder output = new StringBuilder();
        java.io.BufferedReader reader = new java.io.BufferedReader(
            new java.io.InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new Exception("Failed to create test keystore with keytool, exit code: " + exitCode + "\nOutput: " + output);
        }
    }

    @AfterAll
    void cleanup() {
        System.clearProperty(JwtUtil.KEYSTORE_PATH);
        System.clearProperty(JwtUtil.KEYSTORE_PASSWORD);
        System.clearProperty(JwtUtil.KEYSTORE_ALIAS);
        System.clearProperty(JwtUtil.KEYSTORE_KEY_PASSWORD);
        System.clearProperty(JwtUtil.JWT_ISSUER);
        try {
            if (keystorePath != null) {
                Files.deleteIfExists(keystorePath);
            }
        } catch (Exception e) {
            // Cleanup best effort
        }
    }

    @Test
    void generateAccessToken_ValidUser_ReturnsParseableJwt() {
        // Arrange
        User user = new User("u-1", "alice", "hash");
        user.setRoles(List.of(new Role("r-1", "admin"), new Role("r-2", "user")));

        // Act
        String token = jwtUtil.generateAccessToken(user);

        // Assert — token parses with the public key from the keystore and carries the claims
        assertNotNull(token);
        Jws<Claims> parsed = Jwts.parser()
                .verifyWith(jwtUtil.getPublicKey())
                .build()
                .parseSignedClaims(token);

        Claims claims = parsed.getPayload();
        assertEquals("u-1", claims.getSubject());
        assertEquals("alice", claims.get("username", String.class));
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
        User user = new User("u-2", "bob", "hash");

        // Act
        String token = jwtUtil.generateAccessToken(user);

        // Assert
        Claims claims = Jwts.parser()
                .verifyWith(jwtUtil.getPublicKey())
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
        assertTrue(jwtUtil.getPublicKey() instanceof java.security.PublicKey);
    }

    @Test
    void environmentUtilWiring_PropertyTakesPrecedence() {
        // Sanity check that the EnvironmentUtil contract used in init() resolves
        // the keystore configuration properties we set above. Acts as a guard against accidental
        // regressions in EnvironmentUtil's lookup order.
        assertNotNull(EnvironmentUtil.getEnvironmentVariable(JwtUtil.KEYSTORE_PATH));
        assertNotNull(EnvironmentUtil.getEnvironmentVariable(JwtUtil.KEYSTORE_ALIAS));
        assertEquals("test-issuer", EnvironmentUtil.getEnvironmentVariable(JwtUtil.JWT_ISSUER));
    }
}
