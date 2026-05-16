package com.nihith.iam.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

/**
 * Request payload received by {@code POST /auth/login}. The {@code platform},
 * {@code userAgent}, and {@code ipAddress} fields are populated server-side
 * from request headers and are not expected from the client body.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginRequest {

    @NotEmpty
    @Email
    private String email;

    @NotEmpty
    private String password;

    private String platform;
    private String userAgent;
    private String ipAddress;

    public LoginRequest() {
    }

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
                "email='" + email + '\'' +
                ", platform='" + platform + '\'' +
                '}';
    }
}
