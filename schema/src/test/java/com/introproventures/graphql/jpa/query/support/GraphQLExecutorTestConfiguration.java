package com.introproventures.graphql.jpa.query.support;

import com.introproventures.graphql.jpa.query.schema.GraphQLSchemaBuilder;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaExecutor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class GraphQLExecutorTestConfiguration {

    @Bean
    GraphQLJpaExecutor graphQLJpaExecutor(GraphQLSchemaBuilder graphQLSchemaBuilder) {
        return new GraphQLJpaExecutor(graphQLSchemaBuilder.build());
    }
}
