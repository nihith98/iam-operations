package com.nihith.iam.test.util;

import com.nihith.iam.exception.IAMException;
import com.nihith.iam.model.User;
import com.nihith.iam.util.ObjectMapperUtil;
import org.bson.Document;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ObjectMapperUtilTest {

    @Test
    void castToDocument_ValidUser_ReturnsDocumentWithFields() {
        // Arrange
        User user = new User("u-1", "alice", "hash");

        // Act
        Document document = ObjectMapperUtil.castToDocument(user);

        // Assert
        assertNotNull(document);
        assertEquals("u-1", document.getString("userId"));
        assertEquals("alice", document.getString("username"));
    }

    @Test
    void castToObject_ValidDocument_ReturnsPopulatedUser() {
        // Arrange
        Document document = new Document()
                .append("userId", "u-2")
                .append("username", "bob")
                .append("passwordHash", "h");

        // Act
        User user = ObjectMapperUtil.castToObject(document, User.class);

        // Assert
        assertEquals("u-2", user.getUserId());
        assertEquals("bob", user.getUsername());
    }

    @Test
    void castToObject_MalformedJson_ThrowsIAMException() {
        // Arrange — Document with an int where User.username expects a string would
        // still coerce via Jackson; instead use an explicitly invalid Class<T> target.
        Document document = new Document("userId", "u-3").append("username", "ok").append("passwordHash", "h");

        // Act + Assert — trying to deserialize into a class with no matching shape
        assertThrows(IAMException.class, () -> ObjectMapperUtil.castToObject(document, java.net.URI.class));
    }

    @Test
    void castToDocumentList_ListOfUsers_ReturnsListOfDocuments() {
        // Arrange
        List<User> users = List.of(
                new User("u-1", "alice", "h1"),
                new User("u-2", "bob", "h2"));

        // Act
        List<Document> documents = ObjectMapperUtil.castToDocumentList(users);

        // Assert
        assertEquals(2, documents.size());
    }
}
