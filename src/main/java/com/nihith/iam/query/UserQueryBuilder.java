package com.nihith.iam.query;

import com.mongodb.BasicDBObject;
import com.nihith.iam.constants.IAMFieldNameConstants;
import com.nihith.iam.model.TokenStatus;

/**
 * Static factory of {@link BasicDBObject} filters used by IAM DAO classes. Field
 * names come from {@link IAMFieldNameConstants} — never hardcoded inline.
 */
public class UserQueryBuilder {

    /**
     * Builds a filter matching a single user by {@code userId}.
     *
     * @param userId the user identifier
     * @return a filter on the user-id field
     */
    public static BasicDBObject filterByUserId(String userId) {
        BasicDBObject filter = new BasicDBObject();
        filter.put(IAMFieldNameConstants.USER_ID, userId);
        return filter;
    }

    /**
     * Builds a filter matching a user by their unique {@code username}.
     *
     * @param username the username
     * @return a filter on the username field
     */
    public static BasicDBObject filterByUsername(String username) {
        BasicDBObject filter = new BasicDBObject();
        filter.put(IAMFieldNameConstants.USERNAME, username);
        return filter;
    }

    /**
     * Builds a filter matching a user by their unique {@code email}.
     *
     * @param email the email address
     * @return a filter on the email field
     */
    public static BasicDBObject filterByEmail(String email) {
        BasicDBObject filter = new BasicDBObject();
        filter.put(IAMFieldNameConstants.EMAIL, email);
        return filter;
    }

    /**
     * Builds a filter matching all active token records for a user.
     *
     * @param userId the user identifier
     * @return a filter on user-id and {@link TokenStatus#ACTIVE} status
     */
    public static BasicDBObject filterActiveTokensByUserId(String userId) {
        BasicDBObject filter = new BasicDBObject();
        filter.put(IAMFieldNameConstants.USER_ID, userId);
        filter.append(IAMFieldNameConstants.TOKEN_STATUS, TokenStatus.ACTIVE.toString());
        return filter;
    }
}
