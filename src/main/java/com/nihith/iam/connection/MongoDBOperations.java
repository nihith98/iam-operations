package com.nihith.iam.connection;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.InsertOneResult;
import com.nihith.iam.exception.IAMException;
import com.nihith.iam.util.ObjectMapperUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Thin wrapper around the native MongoDB Java driver providing the CRUD primitives
 * needed by IAM DAO classes. Translates {@link MongoException} into
 * {@link IAMException} so callers only ever surface IAM-typed errors.
 */
@Component
@Scope(value = BeanDefinition.SCOPE_SINGLETON)
public class MongoDBOperations {

    public static final Logger logger = LogManager.getLogger(MongoDBOperations.class);

    @Autowired
    private MongoDBConnection mongoDBConnection;

    /**
     * Returns the {@link MongoCollection} for the given collection name.
     *
     * @param collectionName the name of the MongoDB collection
     * @return the {@link MongoCollection}, or {@code null} if a {@link MongoException}
     *         is caught (in which case an {@link IAMException} is re-thrown)
     * @throws IAMException if the collection cannot be accessed
     */
    public MongoCollection<Document> getCollection(String collectionName) {
        logger.info("Getting collection::{}", collectionName);
        try {
            return mongoDBConnection.getMongoDB().getCollection(collectionName);
        } catch (MongoException e) {
            throwIAMException(e);
        }
        return null;
    }

    /**
     * Inserts a single {@link Document} into the given collection.
     *
     * @param collection        the target MongoDB collection
     * @param insertionDocument the document to insert
     * @return {@code true} if the document was inserted successfully
     * @throws IAMException if a {@link MongoException} occurs
     */
    public boolean insertSingleDocument(MongoCollection<Document> collection, Document insertionDocument) {
        logger.info("Entered insertSingleDocument");
        try {
            InsertOneResult result = collection.insertOne(insertionDocument);
            logger.debug("Inserted DocumentId::{}", result.getInsertedId());
            return true;
        } catch (MongoException e) {
            throwIAMException(e);
            return false;
        }
    }

    /**
     * Inserts multiple {@link Document}s in one batch.
     *
     * @param collection         the target MongoDB collection
     * @param insertionDocuments the documents to insert
     * @return {@code true} if all documents were inserted successfully
     * @throws IAMException if a {@link MongoException} occurs
     */
    public boolean insertMultipleDocuments(MongoCollection<Document> collection, List<Document> insertionDocuments) {
        logger.info("Entered insertMultipleDocuments");
        try {
            InsertManyResult result = collection.insertMany(insertionDocuments);
            logger.debug("Inserted DocumentIds::{}", result.getInsertedIds());
            return true;
        } catch (MongoException e) {
            throwIAMException(e);
            return false;
        }
    }

    /**
     * Deletes all documents matching {@code filter} from the given collection.
     *
     * @param collection the target MongoDB collection
     * @param filter     the deletion criteria
     * @return {@code true} if the delete operation succeeded
     * @throws IAMException if a {@link MongoException} occurs
     */
    public boolean deleteDocument(MongoCollection<Document> collection, BasicDBObject filter) {
        logger.info("Entered deleteDocument");
        try {
            logger.debug("Filter Object::{}", filter.toJson());
            DeleteResult result = collection.deleteMany(filter);
            logger.debug("Deleted Record Count::{}", result.getDeletedCount());
            return true;
        } catch (MongoException e) {
            throwIAMException(e);
            return false;
        }
    }

    /**
     * Fetches all matching documents and deserialises them into {@code objectClass}.
     *
     * @param <T>         the target type
     * @param collection  the source MongoDB collection
     * @param filter      the query criteria
     * @param objectClass the deserialisation target class
     * @return a list of populated instances; empty if nothing matches
     * @throws IAMException if a {@link MongoException} occurs
     */
    public <T> List<T> fetchRecordsWithFilter(MongoCollection<Document> collection, BasicDBObject filter, Class<T> objectClass) {
        logger.info("Entered fetchRecordsWithFilter");
        List<T> objectList = new ArrayList<>();
        try {
            logger.debug("Filter Object::{}", filter.toJson());
            collection.find(filter).iterator().forEachRemaining(document -> {
                objectList.add(ObjectMapperUtil.castToObject(document, objectClass));
            });
            logger.debug("Fetched Record Count::{}", objectList.size());
            return objectList;
        } catch (MongoException e) {
            throwIAMException(e);
            return objectList;
        }
    }

    /**
     * Logs and rethrows a {@link MongoException} as an {@link IAMException}.
     *
     * @param e the MongoDB exception encountered
     * @throws IAMException always
     */
    private void throwIAMException(MongoException e) {
        logger.error("Mongo Exception Occurred");
        logger.error(ExceptionUtils.getStackTrace(e));
        throw new IAMException("Mongo Exception Occurred");
    }
}
