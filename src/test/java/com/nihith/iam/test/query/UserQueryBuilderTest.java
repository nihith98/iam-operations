package com.nihith.iam.test.query;

import com.mongodb.BasicDBObject;
import com.nihith.iam.model.TokenStatus;
import com.nihith.iam.query.UserQueryBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserQueryBuilderTest {

    @Test
    void filterByUserId_ValidId_ReturnsFilterOnUserIdField() {
        // Act
        BasicDBObject filter = UserQueryBuilder.filterByUserId("u-1");

        // Assert
        assertEquals("u-1", filter.getString("userId"));
        assertEquals(1, filter.size());
    }

    @Test
    void filterByUsername_ValidUsername_ReturnsFilterOnUsernameField() {
        // Act
        BasicDBObject filter = UserQueryBuilder.filterByUsername("alice");

        // Assert
        assertEquals("alice", filter.getString("username"));
        assertEquals(1, filter.size());
    }

    @Test
    void filterActiveTokensByUserId_ValidId_ReturnsCompoundFilter() {
        // Act
        BasicDBObject filter = UserQueryBuilder.filterActiveTokensByUserId("u-1");

        // Assert
        assertEquals("u-1", filter.getString("userId"));
        assertEquals(TokenStatus.ACTIVE.toString(), filter.getString("tokenStatus"));
        assertEquals(2, filter.size());
    }
}
