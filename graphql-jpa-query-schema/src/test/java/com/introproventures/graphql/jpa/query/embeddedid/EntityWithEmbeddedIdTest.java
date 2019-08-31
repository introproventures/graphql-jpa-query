package com.introproventures.graphql.jpa.query.embeddedid;

import static org.assertj.core.api.Assertions.assertThat;

import javax.persistence.EntityManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.introproventures.graphql.jpa.query.schema.GraphQLExecutor;
import com.introproventures.graphql.jpa.query.schema.GraphQLSchemaBuilder;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaExecutor;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = "spring.datasource.data=EntityWithEmbeddedIdTest.sql")
@TestPropertySource({"classpath:hibernate.properties"})
public class EntityWithEmbeddedIdTest {

    @SpringBootApplication
    static class Application {
        @Bean
        public GraphQLExecutor graphQLExecutor(final GraphQLSchemaBuilder graphQLSchemaBuilder) {
            return new GraphQLJpaExecutor(graphQLSchemaBuilder.build());
        }

        @Bean
        public GraphQLSchemaBuilder graphQLSchemaBuilder(final EntityManager entityManager) {
            return new GraphQLJpaSchemaBuilder(entityManager)
                    .name("EntityWithEmbeddedIdTest");
        }
    }

    @Autowired
    private GraphQLExecutor executor;

    @Test
    public void queryBookWithEmbeddedId() {
        //given
        String query = "query {" +
                "  Book(" +
                "    bookId: {"
                + "    title: \"War and Piece\"" +
                "      language: \"Russian\"" +
                "    }" +
                "  )" +
                "  {" +
                "    bookId {" +
                "      title" +
                "      language" +
                "    }" +
                "    description" +
                "  }" +
                "}";

        String expected = "{Book={bookId={title=War and Piece, language=Russian}, description=War and Piece Novel}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryBooksWithyWithEmbeddedId() {
        //given
        String query = "query {" +
                "  Books {" +
                "    total" +
                "    pages" +
                "    select {" +
                "      bookId {" +
                "        title" +
                "        language" +
                "      }" +
                "      description" +
                "    }" +
                "  }" +
                "}";

        String expected = "{Books={total=2, pages=1, select=["
                + "{bookId={title=Witch Of Water, language=English}, description=Witch Of Water Fantasy}, "
                + "{bookId={title=War and Piece, language=Russian}, description=War and Piece Novel}"
                + "]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }
    
    @Test
    public void queryBooksWithyWithEmbeddedIdWhereCriteriaExpression() {
        //given
        String query = "query {" +
                "  Books( " +
                "    where: {" +
                "      bookId: {" +
                "        EQ: {" +
                "          title: \"War and Piece\"" +
                "          language: \"Russian\"" +
                "        }" +
                "      }" +
                "    }" +
                "  ){" +
                "    total" +
                "    pages" +
                "    select {" +
                "      bookId {" +
                "        title" +
                "        language" +
                "      }" +
                "      description" +
                "    }" +
                "  }" +
                "}";
        
        String expected = "{Books={total=1, pages=1, select=["
                + "{bookId={title=War and Piece, language=Russian}, description=War and Piece Novel}"
                + "]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

}