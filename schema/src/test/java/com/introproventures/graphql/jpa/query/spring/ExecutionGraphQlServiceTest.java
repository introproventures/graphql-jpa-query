package com.introproventures.graphql.jpa.query.spring;

import com.introproventures.graphql.jpa.query.AbstractSpringBootTestSupport;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;
import com.introproventures.graphql.jpa.query.schema.model.book.Book;
import graphql.schema.GraphQLSchema;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.graphql.ExecutionGraphQlService;
import org.springframework.graphql.execution.DefaultExecutionGraphQlService;
import org.springframework.graphql.execution.GraphQlSource;
import org.springframework.graphql.test.tester.ExecutionGraphQlServiceTester;

@SpringBootTest(properties = "spring.sql.init.data-locations=classpath:books.sql")
public class ExecutionGraphQlServiceTest extends AbstractSpringBootTestSupport {

    protected static ExecutionGraphQlServiceTester graphQlServiceTester;

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = Book.class)
    static class Application {

        @Bean
        public GraphQLSchema graphQLSchemaBuilder(final EntityManager entityManager) {
            return new GraphQLJpaSchemaBuilder(entityManager)
                .name("GraphQLBooks")
                .description("Books JPA test schema")
                .build();
        }

        @Bean
        GraphQlSource graphQlSource(GraphQLSchema graphQLSchema) {
            return GraphQlSource.builder(graphQLSchema).build();
        }

        @Bean
        ExecutionGraphQlService executionGraphQlService(GraphQlSource graphQlSource) {
            return new DefaultExecutionGraphQlService(graphQlSource);
        }
    }

    @BeforeAll
    static void configureGraphQlServiceTester(@Autowired ExecutionGraphQlService executionGraphQlService) {
        graphQlServiceTester = ExecutionGraphQlServiceTester.create(executionGraphQlService);
    }

    @Test
    void graphQlServiceTester() {
        graphQlServiceTester
            .document("{Books{select{id title}}}")
            .execute()
            .path(".select[*]")
            .matchesJson(
                """
                [{"id":2,"title":"War and Peace"},
                {"id":3,"title":"Anna Karenina"},
                {"id":5,"title":"The Cherry Orchard"},
                {"id":6,"title":"The Seagull"},
                {"id":7,"title":"Three Sisters"}]
                """
            );
    }
}
