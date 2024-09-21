package org.hibernate.omm.type.struct;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.SessionFactory;
import org.hibernate.annotations.Struct;
import org.hibernate.omm.extension.MongoIntegrationTest;
import org.hibernate.omm.extension.SessionFactoryInjected;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * @author Nathan Xu
 * @since 1.0.0
 */
@MongoIntegrationTest
class ElementMatchTests {

    @SessionFactoryInjected
    SessionFactory sessionFactory;

    @Test
    @Disabled("disabled for currently there is no way to simulate Mongo's $elementMatch")
    void test() {
        sessionFactory.inTransaction(session -> {
            var tagByAuthor = new TagByAuthor();
            tagByAuthor.author = "Nathan";
            tagByAuthor.tag = "drama";
            var movie = new Movie();
            movie.id = 1;
            movie.title = "Forrest Gump";
            movie.tagByAuthors = new TagByAuthor[] { tagByAuthor };
            session.persist(movie);
        });
        sessionFactory.inTransaction(session -> {
            /**
             * the following query will end up with SemanticException "org.hibernate.query.SemanticException: Path is not a plural path 'org.hibernate.omm.type.struct.ElementMatchTests$Movie(m).tagByAuthors(tags)' [from Movie as m join m.tagByAuthors as tags where :struct in elements(tags)"
             */
            session.createSelectionQuery("""
                    from Movie as m join m.tagByAuthors as tags where :struct in elements(tags)
                    """, Movie.class)
                    .setParameter("struct", new TagByAuthor("drama", "Nathan"))
                    .getSingleResult();
        });
    }

    @Entity(name = "Movie")
    @Table(name = "movies")
    static class Movie {
        @Id
        int id;

        String title;

        @Embedded
        TagByAuthor[] tagByAuthors;
    }

    @Embeddable
    @Struct(name = "TagByAuthor")
    static class TagByAuthor {
        String tag;
        String author;

        TagByAuthor() {}
        TagByAuthor(String tag, String author) {
            this.tag = tag;
            this.author = author;
        }
    }
}