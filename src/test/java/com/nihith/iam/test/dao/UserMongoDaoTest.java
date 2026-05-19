package com.nihith.iam.test.dao;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.nihith.iam.connection.MongoDBOperations;
import com.nihith.iam.dao.UserMongoDao;
import com.nihith.iam.exception.IAMException;
import com.nihith.iam.model.User;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserMongoDaoTest {

    @Mock
    private MongoDBOperations mongoDBOperations;

    @Mock
    private MongoCollection<Document> mongoCollection;

    @InjectMocks
    private UserMongoDao userMongoDao;

    @BeforeEach
    void setUp() {
        reset(mongoDBOperations, mongoCollection);
    }

    @Test
    void createUser_Success_ReturnsTrue() {
        // Arrange
        when(mongoDBOperations.getCollection(UserMongoDao.USER_COLLECTION_NAME)).thenReturn(mongoCollection);
        when(mongoDBOperations.insertSingleDocument(eq(mongoCollection), any(Document.class))).thenReturn(true);

        // Act
        boolean result = userMongoDao.createUser(new User("u-1", "alice", "h"));

        // Assert
        assertTrue(result);
        verify(mongoDBOperations, times(1)).insertSingleDocument(eq(mongoCollection), any(Document.class));
    }

    @Test
    void createUser_MongoException_ThrowsIAMException() {
        // Arrange
        when(mongoDBOperations.getCollection(UserMongoDao.USER_COLLECTION_NAME)).thenReturn(mongoCollection);
        when(mongoDBOperations.insertSingleDocument(eq(mongoCollection), any(Document.class)))
                .thenThrow(new MongoException("boom"));

        // Act + Assert
        assertThrows(IAMException.class,
                () -> userMongoDao.createUser(new User("u-1", "alice", "h")));
    }

    @Test
    void findByUsername_ExistingUsername_ReturnsUser() {
        // Arrange
        when(mongoDBOperations.getCollection(UserMongoDao.USER_COLLECTION_NAME)).thenReturn(mongoCollection);
        when(mongoDBOperations.fetchRecordsWithFilter(eq(mongoCollection), any(BasicDBObject.class), eq(User.class)))
                .thenReturn(List.of(new User("u-1", "alice", "h")));

        // Act
        User result = userMongoDao.findByUsername("alice");

        // Assert
        assertEquals("alice", result.getUsername());
    }

    @Test
    void findByUsername_Missing_ReturnsNull() {
        // Arrange
        when(mongoDBOperations.getCollection(UserMongoDao.USER_COLLECTION_NAME)).thenReturn(mongoCollection);
        when(mongoDBOperations.fetchRecordsWithFilter(eq(mongoCollection), any(BasicDBObject.class), eq(User.class)))
                .thenReturn(List.of());

        // Act + Assert
        assertNull(userMongoDao.findByUsername("ghost"));
    }

    @Test
    void findByUsername_MongoException_ThrowsIAMException() {
        // Arrange
        when(mongoDBOperations.getCollection(UserMongoDao.USER_COLLECTION_NAME)).thenReturn(mongoCollection);
        when(mongoDBOperations.fetchRecordsWithFilter(eq(mongoCollection), any(BasicDBObject.class), eq(User.class)))
                .thenThrow(new MongoException("boom"));

        // Act + Assert
        assertThrows(IAMException.class, () -> userMongoDao.findByUsername("alice"));
    }

    @Test
    void usernameExists_UsernameFound_ReturnsTrue() {
        // Arrange
        String username = "existing_user";
        User existingUser = new User("user-123", username, "hashedPassword");
        when(mongoDBOperations.getCollection(UserMongoDao.USER_COLLECTION_NAME)).thenReturn(mongoCollection);
        when(mongoDBOperations.fetchRecordsWithFilter(
                eq(mongoCollection),
                any(BasicDBObject.class),
                eq(User.class)))
            .thenReturn(List.of(existingUser));

        // Act
        boolean result = userMongoDao.usernameExists(username);

        // Assert
        assertEquals(true, result, "usernameExists should return true when username is found");
    }

    @Test
    void usernameExists_UsernameNotFound_ReturnsFalse() {
        // Arrange
        String username = "nonexistent_user";
        when(mongoDBOperations.getCollection(UserMongoDao.USER_COLLECTION_NAME)).thenReturn(mongoCollection);
        when(mongoDBOperations.fetchRecordsWithFilter(
                eq(mongoCollection),
                any(BasicDBObject.class),
                eq(User.class)))
            .thenReturn(List.of());

        // Act
        boolean result = userMongoDao.usernameExists(username);

        // Assert
        assertEquals(false, result, "usernameExists should return false when username is not found");
    }

    @Test
    void usernameExists_MongoException_ThrowsIAMException() {
        // Arrange
        String username = "any_user";
        when(mongoDBOperations.getCollection(UserMongoDao.USER_COLLECTION_NAME)).thenReturn(mongoCollection);
        when(mongoDBOperations.fetchRecordsWithFilter(
                eq(mongoCollection),
                any(BasicDBObject.class),
                eq(User.class)))
            .thenThrow(new MongoException("Connection failed"));

        // Act & Assert
        assertThrows(IAMException.class, () -> userMongoDao.usernameExists(username),
                "usernameExists should throw IAMException when MongoException occurs");
    }
}
