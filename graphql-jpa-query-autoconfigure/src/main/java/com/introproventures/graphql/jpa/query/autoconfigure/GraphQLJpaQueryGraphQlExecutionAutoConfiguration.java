package com.introproventures.graphql.jpa.query.autoconfigure;

import graphql.GraphQL;
import org.dataloader.DataLoaderOptions;
import org.dataloader.MappedBatchLoaderWithContext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.graphql.GraphQlAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.ExecutionGraphQlService;
import org.springframework.graphql.execution.BatchLoaderRegistry;
import org.springframework.graphql.execution.GraphQlSource;
import reactor.core.publisher.Mono;

import static com.introproventures.graphql.jpa.query.schema.impl.BatchLoaderRegistry.newDataLoaderRegistry;

@Configuration
@ConditionalOnClass({GraphQL.class, GraphQlSource.class})
@AutoConfigureAfter(GraphQlAutoConfiguration.class)
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

    @Bean
    InitializingBean batchLoaderRegistryConfigurer(BatchLoaderRegistry batchLoaderRegistry) {
        return () -> {
            DataLoaderOptions options = DataLoaderOptions.newOptions()
                                                         .setCachingEnabled(false);
            newDataLoaderRegistry(options)
                               .getDataLoadersMap()
                               .entrySet()
                               .stream()
                               .forEach(entry -> batchLoaderRegistry.forName(entry.getKey())
                                                                    .withOptions(options)
                                                                    .registerMappedBatchLoader((keys, env) ->
                                                                           Mono.fromCompletionStage(((MappedBatchLoaderWithContext) entry.getValue()).load(keys, env))));
        };
    }
}
