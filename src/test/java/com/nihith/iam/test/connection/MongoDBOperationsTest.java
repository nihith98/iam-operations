package com.nihith.iam.test.connection;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.InsertOneResult;
import com.nihith.iam.connection.MongoDBConnection;
import com.nihith.iam.connection.MongoDBOperations;
import com.nihith.iam.exception.IAMException;
import com.nihith.iam.model.User;
import org.bson.BsonString;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MongoDBOperationsTest {

    @Mock
    private MongoDBConnection mongoDBConnection;

    @Mock
    private MongoDatabase mongoDatabase;

    @Mock
    private MongoCollection<Document> mongoCollection;

    @InjectMocks
    private MongoDBOperations mongoDBOperations;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reset(mongoDBConnection, mongoDatabase, mongoCollection);
    }

    @Test
    void getCollection_ValidName_ReturnsCollection() {
        // Arrange
        when(mongoDBConnection.getMongoDB()).thenReturn(mongoDatabase);
        when(mongoDatabase.getCollection("users")).thenReturn(mongoCollection);

        // Act
        MongoCollection<Document> result = mongoDBOperations.getCollection("users");

        // Assert
        assertNotNull(result);
        assertEquals(mongoCollection, result);
    }

    @Test
    void getCollection_MongoException_ThrowsIAMException() {
        // Arrange
        when(mongoDBConnection.getMongoDB()).thenThrow(new MongoException("boom"));

        // Act + Assert
        assertThrows(IAMException.class, () -> mongoDBOperations.getCollection("users"));
    }

    @Test
    void insertSingleDocument_Success_ReturnsTrue() {
        // Arrange
        InsertOneResult result = InsertOneResult.acknowledged(new BsonString("inserted"));
        when(mongoCollection.insertOne(any(Document.class))).thenReturn(result);

        // Act
        boolean inserted = mongoDBOperations.insertSingleDocument(mongoCollection, new Document("k", "v"));

        // Assert
        assertTrue(inserted);
    }

    @Test
    void insertSingleDocument_MongoException_ThrowsIAMException() {
        // Arrange
        when(mongoCollection.insertOne(any(Document.class))).thenThrow(new MongoException("boom"));

        // Act + Assert
        assertThrows(IAMException.class,
                () -> mongoDBOperations.insertSingleDocument(mongoCollection, new Document("k", "v")));
    }

    @Test
    void insertMultipleDocuments_Success_ReturnsTrue() {
        // Arrange
        when(mongoCollection.insertMany(any())).thenReturn(InsertManyResult.acknowledged(java.util.Map.of()));

        // Act
        boolean inserted = mongoDBOperations.insertMultipleDocuments(
                mongoCollection,
                List.of(new Document("a", 1), new Document("b", 2)));

        // Assert
        assertTrue(inserted);
    }

    @Test
    void insertMultipleDocuments_MongoException_ThrowsIAMException() {
        // Arrange
        when(mongoCollection.insertMany(any())).thenThrow(new MongoException("boom"));

        // Act + Assert
        assertThrows(IAMException.class,
                () -> mongoDBOperations.insertMultipleDocuments(mongoCollection, List.of(new Document())));
    }

    @Test
    void deleteDocument_Success_ReturnsTrue() {
        // Arrange
        when(mongoCollection.deleteMany(any(BasicDBObject.class))).thenReturn(DeleteResult.acknowledged(1));

        // Act
        boolean deleted = mongoDBOperations.deleteDocument(mongoCollection, new BasicDBObject("k", "v"));

        // Assert
        assertTrue(deleted);
    }

    @Test
    void deleteDocument_MongoException_ThrowsIAMException() {
        // Arrange
        when(mongoCollection.deleteMany(any(BasicDBObject.class))).thenThrow(new MongoException("boom"));

        // Act + Assert
        assertThrows(IAMException.class,
                () -> mongoDBOperations.deleteDocument(mongoCollection, new BasicDBObject("k", "v")));
    }

    @Test
    void fetchRecordsWithFilter_ValidFilter_ReturnsMappedObjects() {
        // Arrange
        @SuppressWarnings("unchecked")
        FindIterable<Document> findIterable = mock(FindIterable.class);
        @SuppressWarnings("unchecked")
        MongoCursor<Document> cursor = mock(MongoCursor.class);

        Document doc = new Document()
                .append("userId", "u-1")
                .append("username", "alice")
                .append("email", "alice@example.com")
                .append("passwordHash", "h");

        when(mongoCollection.find(any(BasicDBObject.class))).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        // Production calls cursor.forEachRemaining(consumer). Because MongoCursor's
        // forEachRemaining is a default method on Iterator, Mockito intercepts it
        // and short-circuits — stub it explicitly to drive the consumer.
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Consumer<Document> consumer = invocation.getArgument(0);
            consumer.accept(doc);
            return null;
        }).when(cursor).forEachRemaining(any());

        // Act
        List<User> users = mongoDBOperations.fetchRecordsWithFilter(
                mongoCollection, new BasicDBObject("userId", "u-1"), User.class);

        // Assert
        assertEquals(1, users.size());
        assertEquals("alice", users.get(0).getUsername());
    }

    @Test
    void fetchRecordsWithFilter_MongoException_ThrowsIAMException() {
        // Arrange
        when(mongoCollection.find(any(BasicDBObject.class))).thenThrow(new MongoException("boom"));

        // Act + Assert
        assertThrows(IAMException.class,
                () -> mongoDBOperations.fetchRecordsWithFilter(
                        mongoCollection, new BasicDBObject("k", "v"), User.class));
    }

    @Test
    void fetchRecordsWithFilter_NoMatches_ReturnsEmptyList() {
        // Arrange
        @SuppressWarnings("unchecked")
        FindIterable<Document> findIterable = mock(FindIterable.class);
        @SuppressWarnings("unchecked")
        MongoCursor<Document> cursor = mock(MongoCursor.class);

        when(mongoCollection.find(any(BasicDBObject.class))).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        // No documents — forEachRemaining is a no-op (Mockito's default).

        // Act
        List<User> users = mongoDBOperations.fetchRecordsWithFilter(
                mongoCollection, new BasicDBObject("userId", "missing"), User.class);

        // Assert
        assertFalse(users.iterator().hasNext());
        assertEquals(0, users.size());
    }
}
