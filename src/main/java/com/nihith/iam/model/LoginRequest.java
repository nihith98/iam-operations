package com.nihith.iam.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;

/**
 * Request payload received by {@code POST /auth/login}. The {@code userId} field
 * holds the human-readable username the user chose at registration — it is not the
 * internal UUID. The {@code platform}, {@code userAgent}, and {@code ipAddress}
 * fields are populated server-side from request headers and are not expected from
 * the client body.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginRequest {

    @NotEmpty
    private String userId;

    @NotEmpty
    private String password;

    private String platform;
    private String userAgent;
    private String ipAddress;

    public LoginRequest() {
    }

    public LoginRequest(String userId, String password) {
        this.userId = userId;
        this.password = password;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    @Override
    public String toString() {
        return "LoginRequest{" +
                "userId='" + userId + '\'' +
                ", platform='" + platform + '\'' +
                '}';
    }
}
