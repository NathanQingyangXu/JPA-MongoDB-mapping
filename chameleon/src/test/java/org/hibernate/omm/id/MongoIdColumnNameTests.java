package org.hibernate.omm.id;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.bson.Document;
import org.hibernate.omm.AbstractMongodbIntegrationTests;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Nathan Xu
 */
class MongoIdColumnNameTests extends AbstractMongodbIntegrationTests {

    final Long id = 21344L;

    @Test
    @DisplayName("when @Id field was not annotated with @Column(name = \"xxx\")")
    void test_implicit_id_column_spec() {

        getSessionFactory().inTransaction(session -> {
            var entity = new WithImplicitIdColumnSpec();
            entity.id = id;
            entity.title = "Bible";
            session.persist(entity);
        });

        var findQuery = "{ find: \"books\", filter: { _id: { $eq: " + id + " } } }";

        var response = getMongoDatabase().runCommand(Document.parse(findQuery));
        assertThat(response.getDouble("ok")).isEqualTo(1.0);

        List<Document> docs = response.get("cursor", Document.class).getList("firstBatch", Document.class);
        assertThat(docs).hasSize(1);

        assertThat(docs.get(0)).doesNotContainKey("id"); // 'id' is the implicit column name per entity
    }

    @Test
    @DisplayName("when @Id field was annotated with @Column(name = \"xxx\")")
    void test_explicit_id_column_spec() {
        getSessionFactory().inTransaction(session -> {
            var entity = new WithExplicitIdColumnSpec();
            entity.id = id;
            entity.title = "Bible";
            session.persist(entity);
        });

        var findQuery = "{ find: \"books\", filter: { _id: { $eq: " + id + " } } }";

        var response = getMongoDatabase().runCommand(Document.parse(findQuery));
        assertThat(response.getDouble("ok")).isEqualTo(1.0);

        List<Document> docs = response.get("cursor", Document.class).getList("firstBatch", Document.class);
        assertThat(docs).hasSize(1);

        assertThat(docs.get(0)).doesNotContainKey("identifier"); // 'identifier' is the explicit column name per entity
    }


    @Override
    public List<Class<?>> getAnnotatedClasses() {
        return List.of(WithImplicitIdColumnSpec.class, WithExplicitIdColumnSpec.class);
    }

    @Entity(name = "WithImplicitIdColumnSpec")
    @Table(name = "books")
    static class WithImplicitIdColumnSpec {

        @Id
        Long id;

        String title;

    }

    @Entity(name = "WithExplicitIdColumnSpec")
    @Table(name = "books")
    static class WithExplicitIdColumnSpec {

        @Id
        @Column(name = "identifier")
        Long id;

        String title;

    }
}
