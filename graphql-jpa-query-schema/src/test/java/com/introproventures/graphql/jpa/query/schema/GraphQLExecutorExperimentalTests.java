package com.introproventures.graphql.jpa.query.schema;

import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaExecutor;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;
import com.introproventures.graphql.jpa.query.support.GraphQLExecutorExperimentalTestsSupport;
import jakarta.persistence.EntityManager;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;

@SpringBootTest
public class GraphQLExecutorExperimentalTests extends GraphQLExecutorExperimentalTestsSupport {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class Application {
        @Bean
        public GraphQLExecutor graphQLExecutor(final GraphQLSchemaBuilder graphQLSchemaBuilder) {
            return new GraphQLJpaExecutor(graphQLSchemaBuilder.build());
        }

        @Bean
        public GraphQLSchemaBuilder graphQLSchemaBuilder(final EntityManager entityManager) {
            
            return new GraphQLJpaSchemaBuilder(entityManager)
                .name("GraphQLBooks")
                .description("Books JPA test schema")
                .enableSubscription(true)
                .enableResultStream(false);
        }
        
    }
    
}
