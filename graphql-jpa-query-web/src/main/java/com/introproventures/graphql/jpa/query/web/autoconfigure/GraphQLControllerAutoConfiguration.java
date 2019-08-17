package com.introproventures.graphql.jpa.query.web.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.introproventures.graphql.jpa.query.schema.GraphQLExecutor;
import com.introproventures.graphql.jpa.query.web.GraphQLController;

@Configuration
@ConditionalOnWebApplication
@ConditionalOnClass(GraphQLExecutor.class)
@ConditionalOnProperty(name= "spring.graphql.jpa.query.web.enabled", havingValue="true", matchIfMissing=true)
public class GraphQLControllerAutoConfiguration {
    
    @Import(GraphQLController.class)
    public static class DefaultGraphQLControllerConfiguration {
        
    }
    
}
