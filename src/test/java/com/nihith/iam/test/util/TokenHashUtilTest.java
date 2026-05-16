package com.nihith.iam.test.util;

import com.nihith.iam.util.TokenHashUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class TokenHashUtilTest {

    @Test
    void sha256Hex_KnownInput_ReturnsKnownDigest() {
        // Arrange — SHA-256 of "abc" is a well-known vector
        String input = "abc";

        // Act
        String digest = TokenHashUtil.sha256Hex(input);

        // Assert
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", digest);
    }

    @Test
    void sha256Hex_DifferentInputs_ProduceDifferentDigests() {
        // Arrange + Act
        String hash1 = TokenHashUtil.sha256Hex("token-one");
        String hash2 = TokenHashUtil.sha256Hex("token-two");

        // Assert
        assertNotEquals(hash1, hash2);
    }

    @Test
    void sha256Hex_SameInputTwice_ReturnsSameDigest() {
        // Arrange + Act
        String hash1 = TokenHashUtil.sha256Hex("repeatable");
        String hash2 = TokenHashUtil.sha256Hex("repeatable");

        // Assert
        assertEquals(hash1, hash2);
    }
}
