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
