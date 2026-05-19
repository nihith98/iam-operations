package com.nihith.iam.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Represents an authenticated principal in the Breakdown platform. Stored in the
 * users collection (when {@code PREFERRED_IAM} is the MongoDB backend) or sourced
 * from Ory Kratos via the {@code identities} API (when Kratos is preferred).
 *
 * <p>No email or other PII is stored on this model per the platform branding
 * philosophy (minimal PII: username only).</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

    private String userId;

    @NotEmpty
    private String username;

    @NotEmpty
    private String passwordHash;

    private List<Role> roles;

    public User() {
    }

    public User(String userId, String username, String passwordHash) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", roles=" + roles +
                '}';
    }
}
