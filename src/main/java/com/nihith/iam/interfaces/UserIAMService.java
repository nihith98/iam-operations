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
     * Retrieves a user by their unique username.
     *
     * @param username the username to look up
     * @return the matching {@link User}, or {@code null} if not found
     * @throws IAMException if a data store or upstream error occurs
     */
    User findByUsername(String username) throws IAMException;

    /**
     * Retrieves a user by their unique email address. Used by {@code /auth/login}
     * since the login request is keyed on email per the design spec.
     *
     * @param email the email address to look up
     * @return the matching {@link User}, or {@code null} if not found
     * @throws IAMException if a data store or upstream error occurs
     */
    User findByEmail(String email) throws IAMException;
}
