package com.introproventures.graphql.jpa.query.web.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.introproventures.graphql.jpa.query.schema.GraphQLExecutor;
import com.introproventures.graphql.jpa.query.web.GraphQLController;
import com.introproventures.graphql.jpa.query.web.GraphQLControllerProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnWebApplication
@ConditionalOnClass(GraphQLExecutor.class)
@ConditionalOnProperty(prefix = "spring.graphql.jpa.query", name = {"enabled", "web.enabled"}, havingValue="true", matchIfMissing=true)
@EnableConfigurationProperties(GraphQLControllerProperties.class)
public class GraphQLControllerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(GraphQLController.class)
    @ConditionalOnBean(GraphQLExecutor.class)
    public GraphQLController graphQLController(GraphQLExecutor graphQLExecutor,
                                               ObjectMapper objectMapper) {
        return new GraphQLController(graphQLExecutor,
                                     objectMapper);
    }

}
