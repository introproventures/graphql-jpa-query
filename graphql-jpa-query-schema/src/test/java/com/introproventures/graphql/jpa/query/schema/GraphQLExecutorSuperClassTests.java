package com.introproventures.graphql.jpa.query.schema;


import static org.assertj.core.api.Assertions.assertThat;

import javax.persistence.EntityManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaExecutor;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.NONE)
@TestPropertySource({"classpath:hibernate.properties"})
@DirtiesContext
public class GraphQLExecutorSuperClassTests {

    @SpringBootApplication
    static class Application {
        @Bean
        public GraphQLExecutor graphQLExecutor(final GraphQLSchemaBuilder graphQLSchemaBuilder) {
            return new GraphQLJpaExecutor(graphQLSchemaBuilder.build());
        }

        @Bean
        public GraphQLSchemaBuilder graphQLSchemaBuilder(final EntityManager entityManager) {

            return new GraphQLJpaSchemaBuilder(entityManager)
                    .name("GraphQLBooks")
                    .description("Books JPA test schema");
        }
    }

    @Autowired
    private GraphQLExecutor executor;

    @Test
    public void contextLoads() {
        Assert.isAssignable(GraphQLExecutor.class, executor.getClass());
    }


    @Test
    public void querySuperAuthors() {
        //given
        String query = "{ SuperAuthors { select { id name genre} }}";

        String expected = "{SuperAuthors={select=[" +
                "{id=1, name=Leo Tolstoy, genre=NOVEL}, " +
                "{id=4, name=Anton Chekhov, genre=PLAY}, " +
                "{id=8, name=Igor Dianov, genre=JAVA}" +
                "]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void querySuperBooks() {
        //given
        String query = "{ SuperBooks(where: {genre: {IN: PLAY}}) { select { id title, genre } }}";

        String expected = "{SuperBooks={select=["
                + "{id=5, title=The Cherry Orchard, genre=PLAY}, "
                + "{id=6, title=The Seagull, genre=PLAY}, "
                + "{id=7, title=Three Sisters, genre=PLAY}"
                + "]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }
}

