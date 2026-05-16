package com.nihith.iam.test.dao;

import com.nihith.iam.dao.KratosUserIAMService;
import com.nihith.iam.exception.IAMException;
import com.nihith.iam.model.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class KratosUserIAMServiceTest {

    private KratosUserIAMService kratosUserIAMService;

    @BeforeAll
    void setUp() {
        // Arrange — seed the env-var lookups used in @PostConstruct
        System.setProperty(KratosUserIAMService.KRATOS_ADMIN_URL, "http://localhost:4434");
        System.setProperty(KratosUserIAMService.KRATOS_PUBLIC_URL, "http://localhost:4433");

        kratosUserIAMService = new KratosUserIAMService();
        kratosUserIAMService.init();
    }

    @AfterAll
    void cleanup() {
        System.clearProperty(KratosUserIAMService.KRATOS_ADMIN_URL);
        System.clearProperty(KratosUserIAMService.KRATOS_PUBLIC_URL);
    }

    @Test
    void createUser_NotYetImplemented_ThrowsIAMException() {
        // Act + Assert
        assertThrows(IAMException.class,
                () -> kratosUserIAMService.createUser(new User("u-1", "alice", "alice@example.com", "h")));
    }

    @Test
    void findByUsername_NotYetImplemented_ThrowsIAMException() {
        // Act + Assert
        assertThrows(IAMException.class, () -> kratosUserIAMService.findByUsername("alice"));
    }

    @Test
    void findByEmail_NotYetImplemented_ThrowsIAMException() {
        // Act + Assert
        assertThrows(IAMException.class, () -> kratosUserIAMService.findByEmail("alice@example.com"));
    }
}
