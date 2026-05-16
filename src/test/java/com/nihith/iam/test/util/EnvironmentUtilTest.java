package com.nihith.iam.test.util;

import com.nihith.iam.util.EnvironmentUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class EnvironmentUtilTest {

    private static final String TEST_KEY = "BREAKDOWN_TEST_KEY_PROP";

    @AfterEach
    void cleanup() {
        System.clearProperty(TEST_KEY);
    }

    @Test
    void getEnvironmentVariable_SystemPropertySet_ReturnsSystemPropertyValue() {
        // Arrange
        System.setProperty(TEST_KEY, "from-system-property");

        // Act
        String value = EnvironmentUtil.getEnvironmentVariable(TEST_KEY);

        // Assert
        assertEquals("from-system-property", value);
    }

    @Test
    void getEnvironmentVariable_NeitherSet_ReturnsNull() {
        // Arrange — neither system property nor env var is set
        System.clearProperty(TEST_KEY);

        // Act
        String value = EnvironmentUtil.getEnvironmentVariable(TEST_KEY);

        // Assert
        assertNull(value);
    }
}
