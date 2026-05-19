package com.nihith.iam.dao;

import com.mongodb.MongoException;
import com.nihith.iam.connection.MongoDBOperations;
import com.nihith.iam.exception.IAMException;
import com.nihith.iam.interfaces.UserIAMService;
import com.nihith.iam.model.User;
import com.nihith.iam.query.UserQueryBuilder;
import com.nihith.iam.util.ObjectMapperUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * MongoDB-backed implementation of {@link UserIAMService}. Reads from and writes
 * to the {@value #USER_COLLECTION_NAME} collection via {@link MongoDBOperations}.
 */
@Component
@Scope(value = BeanDefinition.SCOPE_SINGLETON)
public class UserMongoDao implements UserIAMService {

    private static final Logger logger = LogManager.getLogger(UserMongoDao.class);

    public static final String USER_COLLECTION_NAME = "users";

    @Autowired
    private MongoDBOperations mongoDBOperations;

    /**
     * {@inheritDoc}
     * <p>Serialises the user to a {@link org.bson.Document} and inserts it into
     * the {@value #USER_COLLECTION_NAME} collection.</p>
     */
    @Override
    public boolean createUser(User user) throws IAMException {
        logger.info("Entered createUser");
        try {
            boolean result = mongoDBOperations.insertSingleDocument(
                    mongoDBOperations.getCollection(USER_COLLECTION_NAME),
                    ObjectMapperUtil.castToDocument(user));
            logger.info("Exiting createUser");
            return result;
        } catch (MongoException e) {
            throwIAMException(e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     * <p>Queries the {@value #USER_COLLECTION_NAME} collection by username and
     * returns the first matching document deserialised into a {@link User}, or
     * {@code null} when no match is found.</p>
     */
    @Override
    public User findByUsername(String username) throws IAMException {
        logger.info("Entered findByUsername");
        try {
            List<User> users = mongoDBOperations.fetchRecordsWithFilter(
                    mongoDBOperations.getCollection(USER_COLLECTION_NAME),
                    UserQueryBuilder.filterByUsername(username),
                    User.class);
            logger.info("Exiting findByUsername");
            return users.isEmpty() ? null : users.get(0);
        } catch (MongoException e) {
            throwIAMException(e);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     * <p>Queries the {@value #USER_COLLECTION_NAME} collection by userId and
     * returns the first matching document deserialised into a {@link User}, or
     * {@code null} when no match is found.</p>
     */
    @Override
    public User findByUserId(String userId) throws IAMException {
        logger.info("Entered findByUserId");
        try {
            List<User> users = mongoDBOperations.fetchRecordsWithFilter(
                    mongoDBOperations.getCollection(USER_COLLECTION_NAME),
                    UserQueryBuilder.filterByUserId(userId),
                    User.class);
            logger.info("Exiting findByUserId");
            return users.isEmpty() ? null : users.get(0);
        } catch (MongoException e) {
            throwIAMException(e);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     * <p>Queries the {@value #USER_COLLECTION_NAME} collection by username to determine
     * if the username is already registered. Returns {@code true} if at least one
     * user document matches the username, {@code false} if the collection is empty
     * for that username.</p>
     */
    @Override
    public boolean usernameExists(String username) throws IAMException {
        logger.info("Entered usernameExists");
        try {
            List<User> users = mongoDBOperations.fetchRecordsWithFilter(
                    mongoDBOperations.getCollection(USER_COLLECTION_NAME),
                    UserQueryBuilder.filterByUsername(username),
                    User.class);
            logger.info("Exiting usernameExists");
            return !users.isEmpty();
        } catch (MongoException e) {
            throwIAMException(e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     * <p>Queries the {@value #USER_COLLECTION_NAME} collection by email and
     * returns the first matching document deserialised into a {@link User}, or
     * {@code null} when no match is found.</p>
     */
    @Override
    public User findByEmail(String email) throws IAMException {
        logger.info("Entered findByEmail");
        try {
            List<User> users = mongoDBOperations.fetchRecordsWithFilter(
                    mongoDBOperations.getCollection(USER_COLLECTION_NAME),
                    UserQueryBuilder.filterByEmail(email),
                    User.class);
            logger.info("Exiting findByEmail");
            return users.isEmpty() ? null : users.get(0);
        } catch (MongoException e) {
            throwIAMException(e);
            return null;
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
