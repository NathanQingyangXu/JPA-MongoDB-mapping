package org.hibernate.omm;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.omm.cfg.MongodbAvailableSettings;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import org.testcontainers.containers.MongoDBContainer;

public abstract class AbstractMongodbContainerTests {
	private final MongoDBContainer mongodbContainer = new MongoDBContainer("mongo:4.0.10" );

	private SessionFactory sessionFactory;

	@BeforeEach
	void startContainers() {
		if (mongodbContainer != null) {
			mongodbContainer.start();
		}
	}

	@AfterEach
	void stopContainers() {
		if (mongodbContainer != null) {
			mongodbContainer.stop();
		}
	}

	protected abstract List<Class<?>> getAnnotatedClasses();

	protected SessionFactory getSessionFactory() {
		if (sessionFactory == null) {
			Configuration cfg = new Configuration();
			getAnnotatedClasses().forEach( cfg::addAnnotatedClass );
			cfg.setProperty( AvailableSettings.DIALECT, "org.hibernate.omm.dialect.MongodbDialect" );
			cfg.setProperty(
					AvailableSettings.CONNECTION_PROVIDER,
					"org.hibernate.omm.jdbc.MongodbConnectionProvider"
			);
			cfg.setProperty( MongodbAvailableSettings.MONGODB_CONNECTION_URL, mongodbContainer.getReplicaSetUrl() );
			cfg.setProperty( MongodbAvailableSettings.MONGODB_DATABASE, "test" );
			sessionFactory = cfg.buildSessionFactory();
		}
		return sessionFactory;
	}
}
