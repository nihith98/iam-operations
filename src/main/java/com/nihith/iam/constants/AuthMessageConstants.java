package com.nihith.iam.constants;

/**
 * Human-readable message strings used when populating {@code AuthResponseStructure}.
 * Kept as a flat list of {@code public static final} constants — never used as a
 * configuration container.
 */
public class AuthMessageConstants {

    /* Error Messages */
    public static final String INTERNAL_SERVER_ERROR = "Internal Server Error";
    public static final String INVALID_CREDENTIALS = "Invalid username or password";
    public static final String USER_CREATION_FAILURE = "User Creation Failed";
    public static final String TOKEN_CREATION_FAILURE = "Token Creation Failed";
    public static final String LOGIN_FAILURE = "Login Failed";
    public static final String INVALID_REFRESH_TOKEN = "Invalid or expired refresh token";

    /* Success Messages */
    public static final String USER_CREATION_SUCCESS = "User Registered Successfully";
    public static final String LOGIN_SUCCESS = "Login Successful";
    public static final String LOGOUT_SUCCESS = "Logged Out Successfully";

    /* Registration Messages */
    public static final String REGISTRATION_SUCCESS = "User Registered Successfully";
    public static final String REGISTRATION_INVALID_DATA = "Unable to complete registration. Please check your username and password and try again.";
}
