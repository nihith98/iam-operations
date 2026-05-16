package com.nihith.iam.response;

/**
 * Severity of a response message used to bucket it under the correct
 * {@link AuthResponseMessages} list.
 */
public enum AuthMessageType {
    INFORMATION,
    WARNING,
    ERROR
}
