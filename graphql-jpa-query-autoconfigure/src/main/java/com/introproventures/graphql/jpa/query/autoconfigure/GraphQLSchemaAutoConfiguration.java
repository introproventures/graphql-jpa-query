package com.introproventures.graphql.jpa.query.autoconfigure;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;

@Configuration
@ConditionalOnClass(GraphQL.class)
@EnableConfigurationProperties(GraphQLJpaQueryProperties.class)
public class GraphQLSchemaAutoConfiguration {

    private final List<GraphQLSchemaConfigurer> graphQLSchemaConfigurers = new ArrayList<>();
    
    @Autowired
    private GraphQLJpaQueryProperties properties;
	
    @Autowired(required = true)
    public void setGraphQLSchemaConfigurers(List<GraphQLSchemaConfigurer> configurers) {
        if (!CollectionUtils.isEmpty(configurers)) {
        	graphQLSchemaConfigurers.addAll(configurers);
        }
    }
    
    @Bean
    @ConditionalOnMissingBean(GraphQLSchema.class)
    public GraphQLSchemaFactoryBean graphQLSchemaFactoryBean() {
    	GraphQLShemaRegistrationImpl graphQLShemaRegistration = new GraphQLShemaRegistrationImpl();

        for (GraphQLSchemaConfigurer configurer : graphQLSchemaConfigurers) {
            configurer.configure(graphQLShemaRegistration);
        }
        
        return new GraphQLSchemaFactoryBean(graphQLShemaRegistration.getManagedGraphQLSchemas())
	        		.setQueryName(properties.getName())
	    			.setQueryDescription(properties.getDescription());
        
        
    };
    
    
}
