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

import com.introproventures.graphql.jpa.query.schema.JavaScalarsWiringPostProcessor;
import graphql.GraphQL;
import graphql.execution.instrumentation.Instrumentation;
import graphql.introspection.Introspection;
import graphql.schema.GraphQLSchema;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.graphql.autoconfigure.ConditionalOnGraphQlSchema;
import org.springframework.boot.graphql.autoconfigure.GraphQlAutoConfiguration;
import org.springframework.boot.graphql.autoconfigure.GraphQlProperties;
import org.springframework.boot.graphql.autoconfigure.GraphQlSourceBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.log.LogMessage;
import org.springframework.graphql.execution.ConnectionTypeDefinitionConfigurer;
import org.springframework.graphql.execution.DataFetcherExceptionResolver;
import org.springframework.graphql.execution.GraphQlSource;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;
import org.springframework.graphql.execution.SubscriptionExceptionResolver;

@AutoConfiguration(before = GraphQlAutoConfiguration.class, after = GraphQLSchemaAutoConfiguration.class)
@ConditionalOnClass({ GraphQL.class, GraphQlSource.class, GraphQLSchemaConfigurer.class })
@ConditionalOnProperty(name = "spring.graphql.jpa.query.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(GraphQlProperties.class)
public class GraphQLJpaQueryGraphQlSourceAutoConfiguration {

    private static final Log logger = LogFactory.getLog(GraphQLJpaQueryGraphQlSourceAutoConfiguration.class);

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
    GraphQLSchemaConfigurer graphQlSourceSchemaConfigurer(
        ResourcePatternResolver resourcePatternResolver,
        GraphQlProperties properties,
        ObjectProvider<DataFetcherExceptionResolver> exceptionResolvers,
        ObjectProvider<SubscriptionExceptionResolver> subscriptionExceptionResolvers,
        ObjectProvider<Instrumentation> instrumentations,
        ObjectProvider<RuntimeWiringConfigurer> wiringConfigurers,
        ObjectProvider<GraphQlSourceBuilderCustomizer> sourceCustomizers
    ) {
        return registry -> {
            String[] schemaLocations = properties.getSchema().getLocations();
            List<Resource> schemaResources = new ArrayList<>();
            schemaResources.addAll(
                resolveSchemaResources(
                    resourcePatternResolver,
                    schemaLocations,
                    properties.getSchema().getFileExtensions()
                )
            );
            schemaResources.addAll(Arrays.asList(properties.getSchema().getAdditionalFiles()));

            GraphQlSource.SchemaResourceBuilder builder = GraphQlSource
                .schemaResourceBuilder()
                .schemaResources(schemaResources.toArray(new Resource[0]))
                .exceptionResolvers(exceptionResolvers.orderedStream().toList())
                .subscriptionExceptionResolvers(subscriptionExceptionResolvers.orderedStream().toList())
                .instrumentation(instrumentations.orderedStream().toList());
            if (properties.getSchema().getInspection().isEnabled()) {
                builder.inspectSchemaMappings(logger::info);
            }
            if (!properties.getSchema().getIntrospection().isEnabled()) {
                Introspection.enabledJvmWide(false);
            }
            builder.configureTypeDefinitions(new ConnectionTypeDefinitionConfigurer());
            wiringConfigurers.orderedStream().forEach(builder::configureRuntimeWiring);
            sourceCustomizers.orderedStream().forEach(customizer -> customizer.customize(builder));

            registry.register(builder.build().schema());
        };
    }

    private List<Resource> resolveSchemaResources(
        ResourcePatternResolver resolver,
        String[] locations,
        String[] extensions
    ) {
        List<Resource> resources = new ArrayList<>();
        for (String location : locations) {
            for (String extension : extensions) {
                resources.addAll(resolveSchemaResources(resolver, location + "*" + extension));
            }
        }
        return resources;
    }

    private List<Resource> resolveSchemaResources(ResourcePatternResolver resolver, String pattern) {
        try {
            return Arrays.asList(resolver.getResources(pattern));
        } catch (IOException ex) {
            logger.debug(LogMessage.format("Could not resolve schema location: '%s'", pattern), ex);
            return Collections.emptyList();
        }
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

    @Bean
    @ConditionalOnMissingBean
    public GraphQlSourceBuilderCustomizer javaScalarGraphQlSourceBuilderCustomizer() {
        return customizer -> {
            customizer.typeVisitorsToTransformSchema(List.of(new JavaScalarsWiringPostProcessor.Visitor()));
        };
    }
}
