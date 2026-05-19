package com.nihith.iam.interfaces;

import com.nihith.iam.exception.IAMException;
import com.nihith.iam.model.User;

/**
 * Contract that every user-identity backend must satisfy. Implementations include
 * {@code UserMongoDao} (native MongoDB) and {@code KratosUserIAMService} (Ory Kratos
 * REST). Selection at runtime is performed by {@code IAMDelegator}.
 */
public interface UserIAMService {

    /**
     * Persists a new user record.
     *
     * @param user the user to create; must carry a hashed password (never plaintext)
     * @return {@code true} if the creation was successful, {@code false} otherwise
     * @throws IAMException if a data store or upstream error occurs
     */
    boolean createUser(User user) throws IAMException;

    /**
     * Retrieves a user by their unique username. This is the primary lookup used
     * by {@code POST /auth/login} — the {@code userId} field of the login request
     * holds the human-readable username the user chose at registration.
     *
     * @param username the username to look up
     * @return the matching {@link User}, or {@code null} if not found
     * @throws IAMException if a data store or upstream error occurs
     */
    User findByUsername(String username) throws IAMException;

    /**
     * Retrieves a user by their unique user ID. Used by {@code /auth/refresh}
     * to validate that the user still exists when refreshing a token.
     *
     * @param userId the user ID to look up
     * @return the matching {@link User}, or {@code null} if not found
     * @throws IAMException if a data store or upstream error occurs
     */
    User findByUserId(String userId) throws IAMException;

    /**
     * Checks whether a username already exists in the user store.
     * Used during registration to detect duplicate usernames and prevent
     * enumeration attacks by returning a generic error.
     *
     * @param username the username to check
     * @return {@code true} if username exists, {@code false} if available
     * @throws IAMException if a data store error occurs
     */
    boolean usernameExists(String username) throws IAMException;
}
