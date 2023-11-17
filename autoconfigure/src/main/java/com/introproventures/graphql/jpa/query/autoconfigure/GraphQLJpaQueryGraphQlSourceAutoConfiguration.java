/*
 * Copyright 2017 IntroPro Ventures, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.introproventures.graphql.jpa.query.autoconfigure;

import graphql.GraphQL;
import graphql.execution.instrumentation.Instrumentation;
import graphql.schema.GraphQLSchema;
import java.util.function.Consumer;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.graphql.ConditionalOnGraphQlSchema;
import org.springframework.boot.autoconfigure.graphql.GraphQlAutoConfiguration;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.boot.autoconfigure.graphql.GraphQlSourceBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.graphql.execution.DataFetcherExceptionResolver;
import org.springframework.graphql.execution.GraphQlSource;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;
import org.springframework.graphql.execution.SubscriptionExceptionResolver;

@AutoConfiguration(before = GraphQlAutoConfiguration.class, after = GraphQLSchemaAutoConfiguration.class)
@ConditionalOnClass({ GraphQL.class, GraphQlSource.class, GraphQLSchemaConfigurer.class })
@ConditionalOnProperty(name = "spring.graphql.jpa.query.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(GraphQlProperties.class)
public class GraphQLJpaQueryGraphQlSourceAutoConfiguration {

    @Bean
    @ConditionalOnBean(GraphQLSchema.class)
    Consumer<GraphQL.Builder> graphQlExecutionStrategyConfigurer(
        ObjectProvider<QueryExecutionStrategyProvider> queryExecutionStrategy,
        ObjectProvider<MutationExecutionStrategyProvider> mutationExecutionStrategy,
        ObjectProvider<SubscriptionExecutionStrategyProvider> subscriptionExecutionStrategy
    ) {
        return builder -> {
            queryExecutionStrategy.ifAvailable(it -> builder.queryExecutionStrategy(it.get()));
            mutationExecutionStrategy.ifAvailable(it -> builder.mutationExecutionStrategy(it.get()));
            subscriptionExecutionStrategy.ifAvailable(it -> builder.subscriptionExecutionStrategy(it.get()));
        };
    }

    @Bean
    @ConditionalOnGraphQlSchema
    GraphQlSourceBuilderCustomizer graphQlSourceBuilderExecutionStrategyCustomizer(
        Consumer<GraphQL.Builder> graphQlExecutionStrategyConfigurer
    ) {
        return builder -> builder.configureGraphQl(graphQlExecutionStrategyConfigurer);
    }

    @Bean
    @ConditionalOnGraphQlSchema
    GraphQLSchemaConfigurer graphQlSourceSchemaConfigurer(
        ListableBeanFactory beanFactory,
        ResourcePatternResolver resourcePatternResolver,
        GraphQlProperties properties,
        ObjectProvider<DataFetcherExceptionResolver> exceptionResolvers,
        ObjectProvider<SubscriptionExceptionResolver> subscriptionExceptionResolvers,
        ObjectProvider<Instrumentation> instrumentations,
        ObjectProvider<RuntimeWiringConfigurer> wiringConfigurers,
        ObjectProvider<GraphQlSourceBuilderCustomizer> sourceCustomizers
    ) {
        return registry -> {
            GraphQlAutoConfiguration graphQlAutoConfiguration = new GraphQlAutoConfiguration(beanFactory);

            GraphQlSource graphQlSource = graphQlAutoConfiguration.graphQlSource(
                resourcePatternResolver,
                properties,
                exceptionResolvers,
                subscriptionExceptionResolvers,
                instrumentations,
                wiringConfigurers,
                sourceCustomizers
            );
            registry.register(graphQlSource.schema());
        };
    }

    @Bean
    @ConditionalOnMissingBean(GraphQlSource.class)
    @ConditionalOnBean(GraphQLSchema.class)
    public GraphQlSource graphQlSource(
        GraphQLSchema graphQLSchema,
        ObjectProvider<DataFetcherExceptionResolver> exceptionResolvers,
        ObjectProvider<SubscriptionExceptionResolver> subscriptionExceptionResolvers,
        ObjectProvider<Instrumentation> instrumentations,
        ObjectProvider<Consumer<GraphQL.Builder>> configurers
    ) {
        GraphQlSource.Builder<?> builder = GraphQlSource.builder(graphQLSchema);

        builder
            .exceptionResolvers(exceptionResolvers.orderedStream().toList())
            .subscriptionExceptionResolvers(subscriptionExceptionResolvers.orderedStream().toList())
            .instrumentation(instrumentations.orderedStream().toList());

        configurers.orderedStream().forEach(builder::configureGraphQl);

        return builder.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public JavaScalarsRuntimeWiringConfigurer javaScalarsRuntimeWiringConfigurer() {
        return new JavaScalarsRuntimeWiringConfigurer();
    }
}
