package com.nihith.iam.test.dao;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.nihith.iam.connection.MongoDBOperations;
import com.nihith.iam.dao.TokenMongoDao;
import com.nihith.iam.exception.IAMException;
import com.nihith.iam.model.Token;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TokenMongoDaoTest {

    @Mock
    private MongoDBOperations mongoDBOperations;

    @Mock
    private MongoCollection<Document> mongoCollection;

    @InjectMocks
    private TokenMongoDao tokenMongoDao;

    @BeforeEach
    void setUp() {
        reset(mongoDBOperations, mongoCollection);
    }

    @Test
    void createToken_Success_ReturnsTrue() {
        // Arrange
        when(mongoDBOperations.getCollection(TokenMongoDao.TOKEN_COLLECTION_NAME)).thenReturn(mongoCollection);
        when(mongoDBOperations.insertSingleDocument(eq(mongoCollection), any(Document.class))).thenReturn(true);

        Token token = new Token("u-1", "deadbeef");
        token.setCreatedAt(Instant.now());
        token.setExpiresAt(Instant.now().plusSeconds(3600));

        // Act
        boolean result = tokenMongoDao.createToken(token);

        // Assert
        assertTrue(result);
        verify(mongoDBOperations, times(1)).insertSingleDocument(eq(mongoCollection), any(Document.class));
    }

    @Test
    void createToken_MongoException_ThrowsIAMException() {
        // Arrange
        when(mongoDBOperations.getCollection(TokenMongoDao.TOKEN_COLLECTION_NAME)).thenReturn(mongoCollection);
        when(mongoDBOperations.insertSingleDocument(eq(mongoCollection), any(Document.class)))
                .thenThrow(new MongoException("boom"));

        Token token = new Token("u-1", "deadbeef");
        token.setCreatedAt(Instant.now());
        token.setExpiresAt(Instant.now().plusSeconds(3600));

        // Act + Assert
        assertThrows(IAMException.class, () -> tokenMongoDao.createToken(token));
    }
}
