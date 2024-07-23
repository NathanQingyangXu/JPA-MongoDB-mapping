package org.hibernate.omm;

import com.mongodb.client.MongoDatabase;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.omm.cfg.MongoAvailableSettings;
import org.hibernate.omm.jdbc.MongoConnectionProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.MongoDBContainer;

import java.util.List;

public abstract class AbstractMongodbIntegrationTests {

    private static final String MONGODB_DOCKER_IMAGE_NAME = "mongo:5.0.28";

    private MongoDBContainer mongoDBContainer;

    private SessionFactory sessionFactory;

    @BeforeEach
    void createSessionFactory() {
        Configuration cfg = new Configuration();

        // Testcontainer will only be launched when no mongo connection url is provided
        // if you prefer connecting with external existing MongoDB (e.g. MongoDB Atlas),
        // simply config connection url and database in hibernate.properties
        if (cfg.getProperty(MongoAvailableSettings.MONGODB_CONNECTION_URL) == null) {
            mongoDBContainer = new MongoDBContainer(MONGODB_DOCKER_IMAGE_NAME);
            mongoDBContainer.start();
            cfg.setProperty(MongoAvailableSettings.MONGODB_CONNECTION_URL, mongoDBContainer.getConnectionString());
            cfg.setProperty(MongoAvailableSettings.MONGODB_DATABASE, "test");
        }
        getAnnotatedClasses().forEach(cfg::addAnnotatedClass);
        sessionFactory = cfg.buildSessionFactory();
    }

    @AfterEach
    void closeSessionFactory() {
        sessionFactory.close();
        if (mongoDBContainer != null) {
            mongoDBContainer.stop();
        }
    }

    public abstract List<Class<?>> getAnnotatedClasses();

    protected SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    protected MongoDatabase getMongoDatabase() {
        return MongoConnectionProvider.mongoDatabase;
    }
}
