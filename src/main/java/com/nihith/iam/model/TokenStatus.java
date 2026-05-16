package com.nihith.iam.model;

/**
 * Lifecycle state of a stored refresh token document.
 */
public enum TokenStatus {
    ACTIVE,
    REVOKED,
    EXPIRED
}
