package com.introproventures.graphql.jpa.query.schema;

import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaExecutor;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;
import graphql.ExecutionResult;
import graphql.validation.ValidationErrorType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;

import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment= SpringBootTest.WebEnvironment.NONE
)
public class AccessTests {

    @SpringBootApplication
    static class Application {
        private static String currentRole = "user";

        public static String getCurrentRole() {
            return currentRole;
        }

        public static void setCurrentRole(String currentRole) {
            Application.currentRole = currentRole;
        }

        @Bean
        public GraphQLExecutor graphQLExecutor(final GraphQLSchemaBuilder graphQLSchemaBuilder) {
            return new GraphQLJpaExecutor(graphQLSchemaBuilder.build());
        }

        @Bean
        public GraphQLSchemaBuilder graphQLSchemaBuilder(final EntityManager entityManager) {

            Predicate<String[]> predicateAccess = roles -> {
                for(int i = 0; i < roles.length; i++) {
                    if (roles[i].equals(this.currentRole)) {
                        return true;
                    }
                }
                return false;
            };

            return new GraphQLJpaSchemaBuilder(entityManager)
                    .predicateRole(predicateAccess)
                    .name("BooksExampleSchema")
                    .description("Books Example Schema");
        }

    }


    @Autowired
    private GraphQLJpaSchemaBuilder builder;

    @Autowired
    private GraphQLExecutor executor;

    @Before
    public void setup() {
    }

    @Test
    public void readErrorOperation() {
        Application.setCurrentRole("user");

        String query = "" +
                "query { " +
                "   Books { " +
                "       select {" +
                "           id title author {id}" +
                "       } " +
                "   } " +
                "}";

        //when
        ExecutionResult result = executor.execute(query);

        //then
        assertThat(result.getErrors())
                .isNotEmpty()
                .extracting("message")
                .containsOnly(
                        "Exception while fetching data (/Books) : Read access error for entity Author"
                );
    }

    @Test
    public void readOkOperation() {
        Application.setCurrentRole("admin");

        String query = "" +
                "query { " +
                "   Books { " +
                "       select {" +
                "           id title author {id}" +
                "       } " +
                "   } " +
                "}";

        //when
        ExecutionResult result = executor.execute(query);

        //then
        assertThat(result.getErrors()).isEmpty();


    }
}
