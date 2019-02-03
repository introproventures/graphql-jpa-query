package com.introproventures.graphql.jpa.query.web.autoconfigure;

import com.introproventures.graphql.jpa.query.schema.GraphQLExecutor;
import com.introproventures.graphql.jpa.query.web.GraphQLController;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnWebApplication
@ConditionalOnClass(GraphQLExecutor.class)
public class GraphQLControllerAutoConfiguration {
    
    @Import(GraphQLController.class)
    public static class DefaultGraphQLControllerConfiguration {
        
    }
    
}
