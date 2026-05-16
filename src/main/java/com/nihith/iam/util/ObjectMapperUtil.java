package com.nihith.iam.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nihith.iam.exception.IAMException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Static helper for converting between IAM domain POJOs and MongoDB {@link Document}
 * objects via Jackson. Registers {@link JavaTimeModule} so {@link java.time.Instant}
 * fields on {@code Token} serialise as ISO strings rather than failing.
 */
public class ObjectMapperUtil {

    public static final Logger logger = LogManager.getLogger(ObjectMapperUtil.class);

    public static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    /**
     * Serialises a POJO to a MongoDB {@link Document} via JSON.
     *
     * @param pojo the object to convert; must be serialisable by Jackson
     * @return a {@link Document} representation of the object
     * @throws IAMException if Jackson fails to serialise the object
     */
    public static Document castToDocument(Object pojo) {
        logger.info("Entered castToDocument");
        try {
            return Document.parse(objectMapper.writeValueAsString(pojo));
        } catch (JsonProcessingException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            throw new IAMException("JSON Processing Exception Occurred");
        }
    }

    /**
     * Deserialises a MongoDB {@link Document} into an instance of the specified class.
     *
     * @param <T>         the target type
     * @param document    the source MongoDB document
     * @param objectClass the class to deserialise into
     * @return a populated instance of {@code T}
     * @throws IAMException if Jackson fails to deserialise the document
     */
    public static <T> T castToObject(Document document, Class<T> objectClass) {
        logger.info("Entered castToObject");
        try {
            logger.debug("Class to be casted::{}", objectClass);
            return objectMapper.readValue(document.toJson(), objectClass);
        } catch (JsonProcessingException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            throw new IAMException("JSON Processing Exception Occurred");
        }
    }

    /**
     * Converts a list of POJOs to a list of MongoDB {@link Document} objects.
     *
     * @param <T>      the type of elements in the input list
     * @param pojoList the list of objects to convert
     * @return a list of {@link Document} instances
     * @throws IAMException if any element fails to serialise
     */
    public static <T> List<Document> castToDocumentList(List<T> pojoList) {
        logger.info("Entered castToDocumentList");
        List<Document> documentList = new ArrayList<>();
        try {
            documentList = pojoList.parallelStream().map(ObjectMapperUtil::castToDocument).toList();
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            throw new IAMException(e);
        }
        return documentList;
    }
}
