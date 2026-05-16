package com.nihith.iam.constants;

/**
 * MongoDB field name constants used by {@code UserQueryBuilder} and any DAO that
 * builds a {@code BasicDBObject} filter. Field names are kept here so they are
 * never hardcoded inline at the call site.
 */
public class IAMFieldNameConstants {

    /* User collection fields */
    public static final String USER_ID = "userId";
    public static final String USERNAME = "username";
    public static final String EMAIL = "email";

    /* Token collection fields */
    public static final String TOKEN_HASH = "tokenHash";
    public static final String TOKEN_STATUS = "tokenStatus";
}
