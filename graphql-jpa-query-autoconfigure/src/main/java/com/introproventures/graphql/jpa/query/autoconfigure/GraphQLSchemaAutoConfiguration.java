package com.introproventures.graphql.jpa.query.autoconfigure;

import java.util.ArrayList;
import java.util.List;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

@Configuration
@ConditionalOnClass(GraphQL.class)
public class GraphQLSchemaAutoConfiguration {

    private final List<GraphQLSchemaConfigurer> graphQLSchemaConfigurers = new ArrayList<>();
	
    @Autowired(required = true)
    public void setGraphQLSchemaConfigurers(List<GraphQLSchemaConfigurer> configurers) {
        if (!CollectionUtils.isEmpty(configurers)) {
        	graphQLSchemaConfigurers.addAll(configurers);
        }
    }
    
    @Bean
    @ConditionalOnMissingBean(GraphQLSchema.class)
    public GraphQLSchemaFactoryBean graphQLSchemaFactoryBean() {
    	GraphQLShemaRegistration graphQLShemaRegistration = new GraphQLShemaRegistration();

        for (GraphQLSchemaConfigurer configurer : graphQLSchemaConfigurers) {
            configurer.configure(graphQLShemaRegistration);
        }
        
        return new GraphQLSchemaFactoryBean(graphQLShemaRegistration.getManagedGraphQLSchemas());
        
    };
    
    
}
