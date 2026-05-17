package com.nihith.iam.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;

/**
 * POJO request payload for the `/auth/refresh` endpoint.
 *
 * The refresh token value itself is optional in the JSON body — it is prioritized
 * from the HttpOnly cookie when present. Platform and device metadata are populated
 * server-side from HTTP headers.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RefreshTokenRequest {

    @NotEmpty
    private String refreshToken;
    private String platform;
    private String userAgent;
    private String ipAddress;

    public RefreshTokenRequest() {
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
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
        // Intentionally exclude refreshToken for security
        return "RefreshTokenRequest{" +
                "platform='" + platform + '\'' +
                ", userAgent='" + userAgent + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                '}';
    }
}
