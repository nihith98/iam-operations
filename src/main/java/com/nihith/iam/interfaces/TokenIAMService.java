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
}
