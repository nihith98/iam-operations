package com.nihith.iam.test.dao;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import com.nihith.iam.connection.MongoDBOperations;
import com.nihith.iam.dao.TokenMongoDao;
import com.nihith.iam.exception.IAMException;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TokenMongoDaoLogoutTest {

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
    void revokeToken_ValidToken_ReturnsTrue() {
        // Arrange
        String tokenHash = "validhashvalue";
        when(mongoDBOperations.getCollection(TokenMongoDao.TOKEN_COLLECTION_NAME))
                .thenReturn(mongoCollection);
        when(mongoCollection.deleteOne(any(Document.class)))
                .thenReturn(DeleteResult.acknowledged(1));

        // Act
        boolean result = tokenMongoDao.revokeToken(tokenHash);

        // Assert
        assertTrue(result);
        verify(mongoCollection, times(1)).deleteOne(any(Document.class));
    }

    @Test
    void revokeToken_TokenNotFound_ReturnsTrue() {
        // Arrange — deleteOne removes 0 documents (token not in store — idempotent)
        String tokenHash = "hashfornonexistenttoken";
        when(mongoDBOperations.getCollection(TokenMongoDao.TOKEN_COLLECTION_NAME))
                .thenReturn(mongoCollection);
        when(mongoCollection.deleteOne(any(Document.class)))
                .thenReturn(DeleteResult.acknowledged(0));

        // Act
        boolean result = tokenMongoDao.revokeToken(tokenHash);

        // Assert
        assertTrue(result);
        verify(mongoCollection, times(1)).deleteOne(any(Document.class));
    }

    @Test
    void revokeToken_MongoException_ThrowsIAMException() {
        // Arrange
        String tokenHash = "hashthrowsexception";
        when(mongoDBOperations.getCollection(TokenMongoDao.TOKEN_COLLECTION_NAME))
                .thenReturn(mongoCollection);
        when(mongoCollection.deleteOne(any(Document.class)))
                .thenThrow(new MongoException("Connection lost"));

        // Act + Assert
        assertThrows(IAMException.class, () -> tokenMongoDao.revokeToken(tokenHash));
    }
}
