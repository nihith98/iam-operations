package com.nihith.iam.test.query;

import com.mongodb.BasicDBObject;
import com.nihith.iam.constants.IAMFieldNameConstants;
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
        assertEquals("u-1", filter.getString(IAMFieldNameConstants.USER_ID));
        assertEquals(1, filter.size());
    }

    @Test
    void filterByUsername_ValidUsername_ReturnsFilterOnUsernameField() {
        // Act
        BasicDBObject filter = UserQueryBuilder.filterByUsername("alice");

        // Assert
        assertEquals("alice", filter.getString(IAMFieldNameConstants.USERNAME));
        assertEquals(1, filter.size());
    }

    @Test
    void filterByEmail_ValidEmail_ReturnsFilterOnEmailField() {
        // Act
        BasicDBObject filter = UserQueryBuilder.filterByEmail("alice@example.com");

        // Assert
        assertEquals("alice@example.com", filter.getString(IAMFieldNameConstants.EMAIL));
        assertEquals(1, filter.size());
    }

    @Test
    void filterActiveTokensByUserId_ValidId_ReturnsCompoundFilter() {
        // Act
        BasicDBObject filter = UserQueryBuilder.filterActiveTokensByUserId("u-1");

        // Assert
        assertEquals("u-1", filter.getString(IAMFieldNameConstants.USER_ID));
        assertEquals(TokenStatus.ACTIVE.toString(), filter.getString(IAMFieldNameConstants.TOKEN_STATUS));
        assertEquals(2, filter.size());
    }
}
