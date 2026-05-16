package com.nihith.iam.connection;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.nihith.iam.util.EnvironmentUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Spring component that manages the {@link MongoClient} lifecycle for the
 * authentication system. Reads connection parameters from the environment on
 * startup and closes the client on shutdown.
 */
@Component
@Scope(value = BeanDefinition.SCOPE_SINGLETON)
public class MongoDBConnection {

    public static final Logger logger = LogManager.getLogger(MongoDBConnection.class);

    public static final String MONGODB_CONNECTION_STRING = "MONGODB_CONNECTION_STRING";
    public static final String MONGODB_DATABASE_NAME = "MONGODB_DATABASE_NAME";

    private MongoClient mongoClient;
    private String dbConnectionString;
    private String dbName;

    /**
     * Initialises the MongoDB connection using {@link WriteConcern#MAJORITY}.
     * Invoked by the Spring container after dependency injection.
     */
    @PostConstruct
    public void init() {
        logger.info("Initializing Mongo Client");
        setupMongoConnectionString();

        MongoClientSettings mongoClientSettings =
                MongoClientSettings
                        .builder()
                        .writeConcern(WriteConcern.MAJORITY)
                        .applyConnectionString(new ConnectionString(this.dbConnectionString))
                        .build();

        this.mongoClient = MongoClients.create(mongoClientSettings);
        logger.info("Successfully initialized MongoClient");
    }

    /**
     * Closes the {@link MongoClient} on Spring bean destruction.
     */
    @PreDestroy
    public void killSession() {
        logger.info("Closing MongoClient");
        this.mongoClient.close();
        logger.info("Successfully closed MongoClient");
    }

    /**
     * Returns the {@link MongoDatabase} instance for the configured database name.
     *
     * @return the connected {@link MongoDatabase}
     */
    public MongoDatabase getMongoDB() {
        return mongoClient.getDatabase(this.dbName);
    }

    /**
     * Reads connection string and database name from the environment into the
     * corresponding fields.
     */
    private void setupMongoConnectionString() {
        this.dbConnectionString = EnvironmentUtil.getEnvironmentVariable(MONGODB_CONNECTION_STRING);
        this.dbName = EnvironmentUtil.getEnvironmentVariable(MONGODB_DATABASE_NAME);
        logger.debug("Value of dbConnectionString::{}", this.dbConnectionString);
        logger.debug("Value of dbName::{}", this.dbName);
    }
}
