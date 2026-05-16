package com.nihith.iam.response;

/**
 * Universal response wrapper returned by every endpoint in {@code authentication-svc}.
 * The HTTP status is always 200 — outcome is communicated by {@link #responseStatus}
 * (SUCCESS or FAILURE) and the messages list.
 */
public class AuthResponseStructure {

    private AuthResponseStatus responseStatus;
    private AuthResponseMessages messages;
    private Object payload;

    public AuthResponseStatus getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(AuthResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
    }

    public AuthResponseMessages getMessages() {
        return messages;
    }

    public void setMessages(AuthResponseMessages messages) {
        this.messages = messages;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "AuthResponseStructure{" +
                "responseStatus=" + responseStatus +
                ", messages=" + messages +
                ", payload=" + payload +
                '}';
    }
}
