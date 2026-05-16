package com.nihith.iam.exception;

/**
 * Unchecked exception used to signal IAM and infrastructure failures (MongoDB,
 * Kratos HTTP, JWT signing, etc.). Mirrors all {@link RuntimeException} constructors.
 */
public class IAMException extends RuntimeException {

    public IAMException() {
        super();
    }

    public IAMException(String message) {
        super(message);
    }

    public IAMException(String message, Throwable cause) {
        super(message, cause);
    }

    public IAMException(Throwable cause) {
        super(cause);
    }

    protected IAMException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
