package com.nihith.iam.test.util;

import com.nihith.iam.util.PasswordUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PasswordUtilTest {

    @Test
    void hash_NonEmptyPassword_ReturnsBCryptDigestDifferentFromInput() {
        // Arrange
        String plaintext = "S3cret!";

        // Act
        String hash = PasswordUtil.hash(plaintext);

        // Assert
        assertNotNull(hash);
        assertNotEquals(plaintext, hash);
        assertTrue(hash.startsWith("$2"));
    }

    @Test
    void matches_CorrectPassword_ReturnsTrue() {
        // Arrange
        String plaintext = "S3cret!";
        String hash = PasswordUtil.hash(plaintext);

        // Act
        boolean matched = PasswordUtil.matches(plaintext, hash);

        // Assert
        assertTrue(matched);
    }

    @Test
    void matches_WrongPassword_ReturnsFalse() {
        // Arrange
        String hash = PasswordUtil.hash("correct");

        // Act
        boolean matched = PasswordUtil.matches("wrong", hash);

        // Assert
        assertFalse(matched);
    }

    @Test
    void hash_SameInputTwice_ReturnsDifferentHashes() {
        // Arrange
        String plaintext = "S3cret!";

        // Act
        String hash1 = PasswordUtil.hash(plaintext);
        String hash2 = PasswordUtil.hash(plaintext);

        // Assert — BCrypt uses random salt so digests must differ
        assertNotEquals(hash1, hash2);
    }
}
