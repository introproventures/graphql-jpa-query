package com.introproventures.graphql.jpa.query.autoconfigure;

import java.util.ArrayList;
import java.util.List;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.util.CollectionUtils;

@Configuration
@ConditionalOnClass(GraphQL.class)
@EnableConfigurationProperties(GraphQLJpaQueryProperties.class)
@PropertySources(value= {
    @PropertySource("classpath:com/introproventures/graphql/jpa/query/boot/autoconfigure/default.properties"),
    @PropertySource(value = "classpath:graphql-jpa-autoconfigure.properties", ignoreResourceNotFound = true)
})
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
    	GraphQLShemaRegistrationImpl schemaRegistry = new GraphQLShemaRegistrationImpl();

        for (GraphQLSchemaConfigurer configurer : graphQLSchemaConfigurers) {
            configurer.configure(schemaRegistry);
        }
        
        return new GraphQLSchemaFactoryBean(schemaRegistry.getManagedGraphQLSchemas(), schemaRegistry.getCodeRegistry())
	        		.setQueryName(properties.getName())
	    			.setQueryDescription(properties.getDescription());
        
        
    };
    
    
}
