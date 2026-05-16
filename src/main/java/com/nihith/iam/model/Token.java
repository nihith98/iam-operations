package com.nihith.iam.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * Represents a refresh-token record persisted in the {@code tokens} collection.
 * The token value itself is never stored — only its SHA-256 hash is, in {@code tokenHash}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Token {

    private String tokenId;

    @NotEmpty
    private String userId;

    @NotEmpty
    private String tokenHash;

    @NotNull
    private Instant expiresAt;

    @NotNull
    private Instant createdAt;

    private Instant lastUsedAt;

    private DeviceInfo deviceInfo;

    private boolean revoked;

    public Token() {
    }

    public Token(String userId, String tokenHash) {
        this.userId = userId;
        this.tokenHash = tokenHash;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(Instant lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }

    @Override
    public String toString() {
        return "Token{" +
                "tokenId='" + tokenId + '\'' +
                ", userId='" + userId + '\'' +
                ", expiresAt=" + expiresAt +
                ", createdAt=" + createdAt +
                ", lastUsedAt=" + lastUsedAt +
                ", deviceInfo=" + deviceInfo +
                ", revoked=" + revoked +
                '}';
    }
}
