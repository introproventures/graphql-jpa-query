package com.introproventures.graphql.jpa.query.autoconfigure;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

import java.util.List;

@Configuration
@ConditionalOnClass(GraphQL.class)
@EnableConfigurationProperties(GraphQLJpaQueryProperties.class)
@PropertySources(value= {
    @PropertySource("classpath:com/introproventures/graphql/jpa/query/boot/autoconfigure/default.properties"),
    @PropertySource(value = "classpath:graphql-jpa-autoconfigure.properties", ignoreResourceNotFound = true)
})
@AutoConfigureAfter(GraphQLSchemaBuilderAutoConfiguration.class)
public class GraphQLSchemaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public GraphQLShemaRegistration graphQLShemaRegistration() {
        return new GraphQLShemaRegistrationImpl();
    }

    @Bean
    @ConditionalOnMissingBean(GraphQLSchema.class)
    public GraphQLSchemaFactoryBean graphQLSchemaFactoryBean(GraphQLJpaQueryProperties properties,
                                                             GraphQLShemaRegistration graphQLShemaRegistration,
                                                             List<GraphQLSchemaConfigurer> graphQLSchemaConfigurers) {
        for (GraphQLSchemaConfigurer configurer : graphQLSchemaConfigurers) {
            configurer.configure(graphQLShemaRegistration);
        }

        return new GraphQLSchemaFactoryBean(graphQLShemaRegistration.getManagedGraphQLSchemas())
	        		.setQueryName(properties.getName())
	    			.setQueryDescription(properties.getDescription());


    };

}
