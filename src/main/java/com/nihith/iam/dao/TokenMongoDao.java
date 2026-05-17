package com.nihith.iam.dao;

import com.mongodb.MongoException;
import com.nihith.iam.connection.MongoDBOperations;
import com.nihith.iam.exception.IAMException;
import com.nihith.iam.interfaces.TokenIAMService;
import com.nihith.iam.model.Token;
import com.nihith.iam.util.ObjectMapperUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * MongoDB-backed implementation of {@link TokenIAMService}. Persists refresh-token
 * records in the {@value #TOKEN_COLLECTION_NAME} collection. A TTL index on the
 * {@code expiresAt} field auto-cleans expired rows.
 */
@Component
@Scope(value = BeanDefinition.SCOPE_SINGLETON)
public class TokenMongoDao implements TokenIAMService {

    private static final Logger logger = LogManager.getLogger(TokenMongoDao.class);

    public static final String TOKEN_COLLECTION_NAME = "tokens";

    @Autowired
    private MongoDBOperations mongoDBOperations;

    /**
     * {@inheritDoc}
     * <p>Serialises the token to a {@link org.bson.Document} and inserts it into
     * the {@value #TOKEN_COLLECTION_NAME} collection.</p>
     */
    @Override
    public boolean createToken(Token token) throws IAMException {
        logger.info("Entered createToken");
        try {
            boolean result = mongoDBOperations.insertSingleDocument(
                    mongoDBOperations.getCollection(TOKEN_COLLECTION_NAME),
                    ObjectMapperUtil.castToDocument(token));
            logger.info("Exiting createToken");
            return result;
        } catch (MongoException e) {
            throwIAMException(e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     * <p>Queries the {@value #TOKEN_COLLECTION_NAME} collection by
     * tokenHash field and returns the first matching document as a Token.</p>
     */
    @Override
    public Token findByTokenHash(String tokenHash) throws IAMException {
        logger.info("Entered findByTokenHash");
        try {
            Document filter = new Document("tokenHash", tokenHash);
            Document tokenDoc = mongoDBOperations.getCollection(TOKEN_COLLECTION_NAME)
                    .find(filter)
                    .first();

            if (tokenDoc == null) {
                logger.info("Exiting findByTokenHash with null");
                return null;
            }

            Token token = ObjectMapperUtil.castToObject(tokenDoc, Token.class);
            logger.info("Exiting findByTokenHash");
            return token;
        } catch (MongoException e) {
            throwIAMException(e);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     * <p>Deletes the token record matching oldTokenHash, then inserts the newToken.
     * Both operations are sequential; callers are responsible for handling the
     * rare edge case where the delete succeeds but the insert fails.</p>
     */
    @Override
    public boolean refreshToken(String oldTokenHash, Token newToken) throws IAMException {
        logger.info("Entered refreshToken");
        try {
            // Delete the old token record
            MongoDBOperations operations = mongoDBOperations;
            com.mongodb.client.MongoCollection<Document> collection = operations.getCollection(TOKEN_COLLECTION_NAME);
            Document deleteFilter = new Document("tokenHash", oldTokenHash);
            collection.deleteOne(deleteFilter);

            // Insert the new token record
            boolean insertResult = operations.insertSingleDocument(
                    collection,
                    ObjectMapperUtil.castToDocument(newToken));

            logger.info("Exiting refreshToken");
            return insertResult;
        } catch (MongoException e) {
            throwIAMException(e);
            return false;
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
