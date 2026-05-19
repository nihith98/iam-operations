package com.nihith.iam.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RegistrationRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    @Test
    public void register_ValidInput_PassesValidation() {
        RegistrationRequest request = new RegistrationRequest("john_doe", "SecurePassword123");
        Set<ConstraintViolation<RegistrationRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Valid input should pass validation");
    }

    @Test
    public void register_EmptyUsername_FailsValidation() {
        RegistrationRequest request = new RegistrationRequest("", "SecurePassword123");
        Set<ConstraintViolation<RegistrationRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Empty username should fail validation");
    }

    @Test
    public void register_NullUsername_FailsValidation() {
        RegistrationRequest request = new RegistrationRequest(null, "SecurePassword123");
        Set<ConstraintViolation<RegistrationRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Null username should fail validation");
    }

    @Test
    public void register_UsernameUnder3Chars_FailsValidation() {
        RegistrationRequest request = new RegistrationRequest("ab", "SecurePassword123");
        Set<ConstraintViolation<RegistrationRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Username under 3 chars should fail validation");
    }

    @Test
    public void register_UsernameOver50Chars_FailsValidation() {
        String longUsername = "a".repeat(51);
        RegistrationRequest request = new RegistrationRequest(longUsername, "SecurePassword123");
        Set<ConstraintViolation<RegistrationRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Username over 50 chars should fail validation");
    }

    @Test
    public void register_EmptyPassword_FailsValidation() {
        RegistrationRequest request = new RegistrationRequest("john_doe", "");
        Set<ConstraintViolation<RegistrationRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Empty password should fail validation");
    }

    @Test
    public void register_NullPassword_FailsValidation() {
        RegistrationRequest request = new RegistrationRequest("john_doe", null);
        Set<ConstraintViolation<RegistrationRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Null password should fail validation");
    }

    @Test
    public void register_PasswordUnder8Chars_FailsValidation() {
        RegistrationRequest request = new RegistrationRequest("john_doe", "short");
        Set<ConstraintViolation<RegistrationRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Password under 8 chars should fail validation");
    }

    @Test
    public void register_PasswordOver255Chars_FailsValidation() {
        String longPassword = "a".repeat(256);
        RegistrationRequest request = new RegistrationRequest("john_doe", longPassword);
        Set<ConstraintViolation<RegistrationRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Password over 255 chars should fail validation");
    }
}
