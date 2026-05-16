package com.nihith.iam.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Lightweight metadata captured at refresh-token issuance, supporting the
 * session management UI and theft-detection forensics.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceInfo {

    private String userAgent;
    private String platform;
    private String ip;

    public DeviceInfo() {
    }

    public DeviceInfo(String userAgent, String platform, String ip) {
        this.userAgent = userAgent;
        this.platform = platform;
        this.ip = ip;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "userAgent='" + userAgent + '\'' +
                ", platform='" + platform + '\'' +
                ", ip='" + ip + '\'' +
                '}';
    }
}
