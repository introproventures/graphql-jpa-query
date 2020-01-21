package com.introproventures.graphql.jpa.query.web.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.introproventures.graphql.jpa.query.schema.GraphQLExecutor;
import com.introproventures.graphql.jpa.query.web.GraphQLController;

@Configuration
@ConditionalOnWebApplication
@ConditionalOnClass(GraphQLExecutor.class)
@ConditionalOnProperty(prefix = "spring.graphql.jpa.query", name = {"enabled", "web.enabled"}, havingValue="true", matchIfMissing=true)
public class GraphQLControllerAutoConfiguration {

    @Configuration
    public static class DefaultGraphQLControllerConfiguration {
        
        @Bean
        @ConditionalOnMissingBean
        public GraphQLController graphQLController(GraphQLExecutor graphQLExecutor,
                                                   ObjectMapper objectMapper) {
            return new GraphQLController(graphQLExecutor, 
                                         objectMapper);
        }
        
    }
    
}
