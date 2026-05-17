package com.nihith.iam.test.dao;

import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
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

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TokenMongoDaoRefreshTest {

    @Mock
    private MongoDBOperations mongoDBOperations;

    @Mock
    private MongoCollection<Document> mongoCollection;

    @Mock
    private FindIterable<Document> findIterable;

    @InjectMocks
    private TokenMongoDao tokenMongoDao;

    @BeforeEach
    void setUp() {
        reset(mongoDBOperations, mongoCollection, findIterable);
    }

    // Test 1: findByTokenHash_ValidHash_ReturnsToken
    @Test
    void testFindByTokenHash_ValidHash_ReturnsToken() {
        // Arrange
        String tokenHash = "abc123def456";
        Document tokenDoc = new Document()
                .append("tokenId", "token-123")
                .append("userId", "user-456")
                .append("tokenHash", tokenHash)
                .append("revoked", false);

        when(mongoDBOperations.getCollection(TokenMongoDao.TOKEN_COLLECTION_NAME))
                .thenReturn(mongoCollection);
        when(mongoCollection.find(any(Document.class)))
                .thenReturn(findIterable);
        when(findIterable.first())
                .thenReturn(tokenDoc);

        // Act
        Token result = tokenMongoDao.findByTokenHash(tokenHash);

        // Assert
        assertNotNull(result);
        assertEquals("user-456", result.getUserId());
        assertEquals(tokenHash, result.getTokenHash());
    }

    // Test 2: findByTokenHash_NotFound_ReturnsNull
    @Test
    void testFindByTokenHash_NotFound_ReturnsNull() {
        // Arrange
        String tokenHash = "invalid-hash";

        when(mongoDBOperations.getCollection(TokenMongoDao.TOKEN_COLLECTION_NAME))
                .thenReturn(mongoCollection);
        when(mongoCollection.find(any(Document.class)))
                .thenReturn(findIterable);
        when(findIterable.first())
                .thenReturn(null);

        // Act
        Token result = tokenMongoDao.findByTokenHash(tokenHash);

        // Assert
        assertNull(result);
    }

    // Test 3: findByTokenHash_MongoException_ThrowsIAMException
    @Test
    void testFindByTokenHash_MongoException_ThrowsIAMException() {
        // Arrange
        String tokenHash = "test-hash";

        when(mongoDBOperations.getCollection(TokenMongoDao.TOKEN_COLLECTION_NAME))
                .thenReturn(mongoCollection);
        when(mongoCollection.find(any(Document.class)))
                .thenThrow(new MongoException("Connection lost"));

        // Act & Assert
        assertThrows(IAMException.class, () -> tokenMongoDao.findByTokenHash(tokenHash));
    }

    // Test 4: refreshToken_Success
    @Test
    void testRefreshToken_Success() {
        // Arrange
        String oldTokenHash = "old-hash-123";
        Token newToken = new Token();
        newToken.setTokenId("new-token-id");
        newToken.setUserId("user-456");
        newToken.setTokenHash("new-hash-456");
        newToken.setRevoked(false);

        when(mongoDBOperations.getCollection(TokenMongoDao.TOKEN_COLLECTION_NAME))
                .thenReturn(mongoCollection);
        when(mongoDBOperations.insertSingleDocument(eq(mongoCollection), any(Document.class)))
                .thenReturn(true);

        // Act
        boolean result = tokenMongoDao.refreshToken(oldTokenHash, newToken);

        // Assert
        assertTrue(result);
        verify(mongoCollection, times(1)).deleteOne(any(Document.class));
        verify(mongoDBOperations, times(1)).insertSingleDocument(eq(mongoCollection), any(Document.class));
    }

    // Test 5: refreshToken_Failure_DeleteFails
    @Test
    void testRefreshToken_Failure_DeleteFails() {
        // Arrange
        String oldTokenHash = "old-hash-123";
        Token newToken = new Token();
        newToken.setTokenId("new-token-id");
        newToken.setUserId("user-456");
        newToken.setTokenHash("new-hash-456");

        when(mongoDBOperations.getCollection(TokenMongoDao.TOKEN_COLLECTION_NAME))
                .thenReturn(mongoCollection);
        when(mongoCollection.deleteOne(any(Document.class)))
                .thenThrow(new MongoException("Delete failed"));

        // Act & Assert
        assertThrows(IAMException.class, () -> tokenMongoDao.refreshToken(oldTokenHash, newToken));
    }

    // Test 6: refreshToken_Failure_InsertFails
    @Test
    void testRefreshToken_Failure_InsertFails() {
        // Arrange
        String oldTokenHash = "old-hash-123";
        Token newToken = new Token();
        newToken.setTokenId("new-token-id");
        newToken.setTokenHash("new-hash-456");

        when(mongoDBOperations.getCollection(TokenMongoDao.TOKEN_COLLECTION_NAME))
                .thenReturn(mongoCollection);
        when(mongoCollection.deleteOne(any(Document.class)))
                .thenReturn(null);
        when(mongoDBOperations.insertSingleDocument(eq(mongoCollection), any(Document.class)))
                .thenReturn(false);

        // Act
        boolean result = tokenMongoDao.refreshToken(oldTokenHash, newToken);

        // Assert
        assertFalse(result);
    }
}
