package com.nihith.iam.test.util;

import com.nihith.iam.response.AuthMessageType;
import com.nihith.iam.response.AuthResponseStatus;
import com.nihith.iam.response.AuthResponseStructure;
import com.nihith.iam.util.AuthResponseStructureUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuthResponseStructureUtilTest {

    @Test
    void generateResponseStructure_SuccessInformation_PopulatesInfoMessages() {
        // Arrange + Act
        AuthResponseStructure result = AuthResponseStructureUtil.generateResponseStructure(
                "payload-value",
                AuthResponseStatus.SUCCESS,
                "Login Successful",
                AuthMessageType.INFORMATION,
                null);

        // Assert
        assertEquals(AuthResponseStatus.SUCCESS, result.getResponseStatus());
        assertEquals("payload-value", result.getPayload());
        assertNotNull(result.getMessages().getInformationMessages());
        assertEquals("Login Successful", result.getMessages().getInformationMessages().get(0));
        assertNull(result.getMessages().getErrorMessages());
    }

    @Test
    void generateResponseStructure_FailureError_PopulatesErrorMessages() {
        // Arrange + Act
        AuthResponseStructure result = AuthResponseStructureUtil.generateResponseStructure(
                false,
                AuthResponseStatus.FAILURE,
                "Login Failed",
                AuthMessageType.ERROR,
                null);

        // Assert
        assertEquals(AuthResponseStatus.FAILURE, result.getResponseStatus());
        assertNotNull(result.getMessages().getErrorMessages());
        assertEquals("Login Failed", result.getMessages().getErrorMessages().get(0));
    }

    @Test
    void generateResponseStructure_NullPayload_LeavesPayloadNull() {
        // Arrange + Act
        AuthResponseStructure result = AuthResponseStructureUtil.generateResponseStructure(
                null,
                AuthResponseStatus.FAILURE,
                "Invalid",
                AuthMessageType.ERROR,
                null);

        // Assert
        assertNull(result.getPayload());
    }

    @Test
    void generateResponseStructure_WithWarning_AppendsWarningMessage() {
        // Arrange + Act
        AuthResponseStructure result = AuthResponseStructureUtil.generateResponseStructure(
                true,
                AuthResponseStatus.SUCCESS,
                "Created",
                AuthMessageType.INFORMATION,
                "Token will expire soon");

        // Assert
        assertNotNull(result.getMessages().getWarningMessages());
        assertTrue(result.getMessages().getWarningMessages().contains("Token will expire soon"));
    }
}
