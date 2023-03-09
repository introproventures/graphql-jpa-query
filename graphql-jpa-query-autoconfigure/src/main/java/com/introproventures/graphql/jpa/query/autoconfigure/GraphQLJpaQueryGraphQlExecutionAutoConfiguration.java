package com.introproventures.graphql.jpa.query.autoconfigure;

import graphql.GraphQL;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.graphql.GraphQlAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.graphql.ExecutionGraphQlService;
import org.springframework.graphql.execution.BatchLoaderRegistry;
import org.springframework.graphql.execution.GraphQlSource;

@AutoConfiguration(after = GraphQlAutoConfiguration.class)
@ConditionalOnClass({GraphQL.class, GraphQlSource.class})
public class GraphQLJpaQueryGraphQlExecutionAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    BatchLoaderRegistry batchLoaderRegistry(ListableBeanFactory beanFactory) {
        return new GraphQlAutoConfiguration(beanFactory).batchLoaderRegistry();
    }

    @Bean
    @ConditionalOnMissingBean
    ExecutionGraphQlService executionGraphQlService(ListableBeanFactory beanFactory,
                                                    GraphQlSource graphQlSource,
                                                    BatchLoaderRegistry batchLoaderRegistry) {
        return new GraphQlAutoConfiguration(beanFactory).executionGraphQlService(graphQlSource,
                                                                                 batchLoaderRegistry);
    }

}
