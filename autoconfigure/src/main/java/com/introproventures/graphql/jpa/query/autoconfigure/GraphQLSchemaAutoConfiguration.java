package com.introproventures.graphql.jpa.query.autoconfigure;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = GraphQLSchemaBuilderAutoConfiguration.class)
@EnableConfigurationProperties(GraphQLJpaQueryProperties.class)
@ConditionalOnClass(GraphQL.class)
@ConditionalOnProperty(name = "spring.graphql.jpa.query.enabled", havingValue = "true", matchIfMissing = true)
@EnableGraphQLJpaQuerySchema
public class GraphQLSchemaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public GraphQLShemaRegistration graphQLShemaRegistration() {
        return new GraphQLShemaRegistrationImpl();
    }

    @Bean
    @ConditionalOnMissingBean(GraphQLSchema.class)
    @ConditionalOnBean(GraphQLSchemaConfigurer.class)
    public GraphQLSchemaFactoryBean graphQLSchemaFactoryBean(
        GraphQLJpaQueryProperties properties,
        GraphQLShemaRegistration graphQLShemaRegistration,
        ObjectProvider<GraphQLSchemaConfigurer> graphQLSchemaConfigurers
    ) {
        for (GraphQLSchemaConfigurer configurer : graphQLSchemaConfigurers) {
            configurer.configure(graphQLShemaRegistration);
        }

        return new GraphQLSchemaFactoryBean(graphQLShemaRegistration.getManagedGraphQLSchemas())
            .setQueryName(properties.getName())
            .setQueryDescription(properties.getDescription());
    }
}
