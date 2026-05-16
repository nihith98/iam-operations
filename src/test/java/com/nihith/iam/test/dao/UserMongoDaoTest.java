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
        boolean result = userMongoDao.createUser(new User("u-1", "alice", "alice@example.com", "h"));

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
                () -> userMongoDao.createUser(new User("u-1", "alice", "alice@example.com", "h")));
    }

    @Test
    void findByUsername_ExistingUsername_ReturnsUser() {
        // Arrange
        when(mongoDBOperations.getCollection(UserMongoDao.USER_COLLECTION_NAME)).thenReturn(mongoCollection);
        when(mongoDBOperations.fetchRecordsWithFilter(eq(mongoCollection), any(BasicDBObject.class), eq(User.class)))
                .thenReturn(List.of(new User("u-1", "alice", "alice@example.com", "h")));

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
    void findByEmail_ExistingEmail_ReturnsUser() {
        // Arrange
        when(mongoDBOperations.getCollection(UserMongoDao.USER_COLLECTION_NAME)).thenReturn(mongoCollection);
        when(mongoDBOperations.fetchRecordsWithFilter(eq(mongoCollection), any(BasicDBObject.class), eq(User.class)))
                .thenReturn(List.of(new User("u-1", "alice", "alice@example.com", "h")));

        // Act
        User result = userMongoDao.findByEmail("alice@example.com");

        // Assert
        assertEquals("alice@example.com", result.getEmail());
    }

    @Test
    void findByEmail_Missing_ReturnsNull() {
        // Arrange
        when(mongoDBOperations.getCollection(UserMongoDao.USER_COLLECTION_NAME)).thenReturn(mongoCollection);
        when(mongoDBOperations.fetchRecordsWithFilter(eq(mongoCollection), any(BasicDBObject.class), eq(User.class)))
                .thenReturn(List.of());

        // Act + Assert
        assertNull(userMongoDao.findByEmail("missing@example.com"));
    }

    @Test
    void findByEmail_MongoException_ThrowsIAMException() {
        // Arrange
        when(mongoDBOperations.getCollection(UserMongoDao.USER_COLLECTION_NAME)).thenReturn(mongoCollection);
        when(mongoDBOperations.fetchRecordsWithFilter(eq(mongoCollection), any(BasicDBObject.class), eq(User.class)))
                .thenThrow(new MongoException("boom"));

        // Act + Assert
        assertThrows(IAMException.class, () -> userMongoDao.findByEmail("alice@example.com"));
    }
}
