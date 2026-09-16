package com.university.knowledge;

import com.university.knowledge.dto.RegisterRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegisterRequestValidationTest {

    private final Validator validator;

    RegisterRequestValidationTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validRequestShouldHaveNoViolations() {
        RegisterRequest request =
                new RegisterRequest("student123", "password123");

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void blankUsernameShouldBeRejected() {
        RegisterRequest request =
                new RegisterRequest("", "password123");

        var messages = validator.validate(request)
                .stream()
                .map(violation -> violation.getMessage())
                .toList();

        assertTrue(messages.contains("Username is required"));
    }

    @Test
    void shortPasswordShouldBeRejected() {
        RegisterRequest request =
                new RegisterRequest("student123", "short");

        var violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals(
                "Password must be between 8 and 100 characters",
                violations.iterator().next().getMessage()
        );
    }
}
