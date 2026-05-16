package com.nihith.iam.util;

import com.nihith.iam.response.AuthMessageType;
import com.nihith.iam.response.AuthResponseMessages;
import com.nihith.iam.response.AuthResponseStatus;
import com.nihith.iam.response.AuthResponseStructure;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Single factory for assembling {@link AuthResponseStructure} instances. No business
 * code should construct {@code AuthResponseStructure} or {@code AuthResponseMessages}
 * directly.
 */
public class AuthResponseStructureUtil {

    public static final Logger logger = LogManager.getLogger(AuthResponseStructureUtil.class);

    /**
     * Builds an {@link AuthResponseStructure} carrying the given payload, outcome,
     * primary message (bucketed under the supplied {@link AuthMessageType}), and an
     * optional warning string.
     *
     * @param response       payload to include; if {@code null}, the payload field is left null
     * @param status         the outcome (SUCCESS or FAILURE)
     * @param message        the primary message
     * @param messageType    the severity that controls which list {@code message} is appended to
     * @param warningMessage an optional secondary warning; ignored when blank
     * @return a fully populated response wrapper
     */
    public static AuthResponseStructure generateResponseStructure(Object response,
                                                                  AuthResponseStatus status,
                                                                  String message,
                                                                  AuthMessageType messageType,
                                                                  String warningMessage) {
        AuthResponseStructure responseStructure = new AuthResponseStructure();
        if (null != response) {
            responseStructure.setPayload(response);
        }
        responseStructure.setResponseStatus(status);
        AuthResponseMessages responseMessages = new AuthResponseMessages();
        switch (messageType) {
            case INFORMATION:
                responseMessages.setInformationMessages(List.of(message));
                break;
            case ERROR:
                responseMessages.setErrorMessages(List.of(message));
                break;
            case WARNING:
                responseMessages.setWarningMessages(List.of(message));
                break;
        }
        responseStructure.setMessages(responseMessages);
        if (StringUtils.isNotBlank(warningMessage)) {
            responseMessages.setWarningMessages(List.of(warningMessage));
        }
        logger.debug("Response Structure Generated::{}", responseStructure.toString());
        return responseStructure;
    }
}
