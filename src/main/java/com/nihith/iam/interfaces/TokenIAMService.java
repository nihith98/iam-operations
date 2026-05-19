package com.nihith.iam.interfaces;

import com.nihith.iam.exception.IAMException;
import com.nihith.iam.model.Token;

/**
 * Contract for the refresh-token store. Implementations always target MongoDB —
 * the value of {@code PREFERRED_IAM} does not switch this binding because token
 * storage is decoupled from the user-identity backend.
 */
public interface TokenIAMService {

    /**
     * Persists a new refresh-token record. The {@link Token#getTokenHash()} field
     * must already contain the SHA-256 digest — never the raw token value.
     *
     * @param token the token record to persist
     * @return {@code true} if the insertion was successful, {@code false} otherwise
     * @throws IAMException if a data store error occurs
     */
    boolean createToken(Token token) throws IAMException;

    /**
     * Retrieves a refresh-token record by its SHA-256 hash.
     *
     * @param tokenHash the SHA-256 digest of the raw refresh token
     * @return the matching {@link Token}, or {@code null} if not found
     * @throws IAMException if a data store error occurs
     */
    Token findByTokenHash(String tokenHash) throws IAMException;

    /**
     * Atomically revokes the old refresh-token record and inserts a new one.
     * This method must ensure the old record is marked revoked before the new
     * record is inserted, to prevent a gap where both are valid.
     *
     * @param oldTokenHash the SHA-256 hash of the token being revoked
     * @param newToken     the new token record to insert
     * @return {@code true} if both operations succeeded, {@code false} otherwise
     * @throws IAMException if a data store error occurs
     */
    boolean refreshToken(String oldTokenHash, Token newToken) throws IAMException;

    /**
     * Deletes the refresh-token record identified by its SHA-256 hash.
     * This operation is idempotent — if no record matches the hash the method
     * returns {@code true} without error, so callers should not rely on the
     * return value to infer whether a token was actually present.
     *
     * @param tokenHash the SHA-256 digest of the raw refresh token to revoke
     * @return {@code true} if the operation completed without error (including
     *         when no matching record existed), {@code false} otherwise
     * @throws IAMException if a data store error occurs
     */
    boolean revokeToken(String tokenHash) throws IAMException;
}
