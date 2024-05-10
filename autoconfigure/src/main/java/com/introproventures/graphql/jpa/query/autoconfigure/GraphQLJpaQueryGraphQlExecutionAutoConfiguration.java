package com.introproventures.graphql.jpa.query.autoconfigure;

import static com.introproventures.graphql.jpa.query.schema.impl.BatchLoaderRegistry.getMappedBatchDataLoaderMap;

import graphql.GraphQL;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.dataloader.DataLoaderOptions;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.graphql.GraphQlAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.graphql.ExecutionGraphQlService;
import org.springframework.graphql.execution.BatchLoaderRegistry;
import org.springframework.graphql.execution.GraphQlSource;
import reactor.core.publisher.Mono;

@AutoConfiguration(after = GraphQLJpaQueryGraphQlSourceAutoConfiguration.class)
@ConditionalOnClass({ GraphQL.class, GraphQlSource.class })
public class GraphQLJpaQueryGraphQlExecutionAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    BatchLoaderRegistry batchLoaderRegistry(ListableBeanFactory beanFactory) {
        var batchLoaderRegistry = new GraphQlAutoConfiguration(beanFactory).batchLoaderRegistry();

        DataLoaderOptions options = DataLoaderOptions.newOptions().setCachingEnabled(false);

        batchLoaderRegistry
            .forName(GraphQLJpaQueryGraphQlExecutionAutoConfiguration.class.getName())
            .withOptions(options)
            .registerMappedBatchLoader((keys, env) ->
                Mono.fromCompletionStage(CompletableFuture.completedStage(Map.of()))
            );

        return batchLoaderRegistry;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(GraphQlSource.class)
    ExecutionGraphQlService executionGraphQlService(
        ListableBeanFactory beanFactory,
        GraphQlSource graphQlSource,
        BatchLoaderRegistry batchLoaderRegistry
    ) {
        return new GraphQlAutoConfiguration(beanFactory).executionGraphQlService(graphQlSource, batchLoaderRegistry);
    }

    @Bean
    InitializingBean batchLoaderRegistryConfigurer(BatchLoaderRegistry batchLoaderRegistry) {
        return () -> {
            DataLoaderOptions options = DataLoaderOptions.newOptions().setCachingEnabled(false);

            getMappedBatchDataLoaderMap()
                .forEach((name, mappedBatchLoader) ->
                    batchLoaderRegistry
                        .forName(name)
                        .withOptions(options)
                        .registerMappedBatchLoader((keys, env) ->
                            Mono.fromCompletionStage(mappedBatchLoader.load(keys, env))
                        )
                );
        };
    }
}
