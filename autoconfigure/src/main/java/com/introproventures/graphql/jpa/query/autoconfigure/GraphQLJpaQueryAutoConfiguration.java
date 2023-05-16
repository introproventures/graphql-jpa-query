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

import com.introproventures.graphql.jpa.query.schema.GraphQLExecutionInputFactory;
import com.introproventures.graphql.jpa.query.schema.GraphQLExecutor;
import com.introproventures.graphql.jpa.query.schema.GraphQLExecutorContextFactory;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaExecutor;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaExecutorContextFactory;
import graphql.GraphQL;
import graphql.GraphQLContext;
import graphql.execution.ExecutionStrategy;
import graphql.execution.instrumentation.Instrumentation;
import graphql.schema.GraphQLSchema;
import graphql.schema.visibility.GraphqlFieldVisibility;
import java.util.function.Supplier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.graphql.ExecutionGraphQlService;

@AutoConfiguration(
    after = {
        HibernateJpaAutoConfiguration.class,
        GraphQLJpaQueryGraphQlExecutionAutoConfiguration.class,
        GraphQLSchemaAutoConfiguration.class,
    }
)
@ConditionalOnClass({ GraphQL.class, GraphQLExecutor.class })
@ConditionalOnProperty(
    prefix = "spring.graphql.jpa.query",
    name = { "enabled", "executor.enabled" },
    havingValue = "true",
    matchIfMissing = true
)
public class GraphQLJpaQueryAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public GraphQLExecutorContextFactory graphQLJpaExecutorContextFactory(
        ObjectProvider<GraphQLExecutionInputFactory> graphQLExecutionInputFactory,
        ObjectProvider<Supplier<GraphqlFieldVisibility>> graphqlFieldVisibility,
        ObjectProvider<Supplier<Instrumentation>> instrumentation,
        ObjectProvider<Supplier<GraphQLContext>> graphqlContext,
        ObjectProvider<Supplier<ExecutionStrategy>> queryExecutionStrategy,
        ObjectProvider<Supplier<ExecutionStrategy>> mutationExecutionStrategy,
        ObjectProvider<Supplier<ExecutionStrategy>> subscriptionExecutionStrategy
    ) {
        GraphQLJpaExecutorContextFactory bean = new GraphQLJpaExecutorContextFactory();

        graphQLExecutionInputFactory.ifAvailable(bean::withExecutionInputFactory);
        graphqlFieldVisibility.ifAvailable(bean::withGraphqlFieldVisibility);
        instrumentation.ifAvailable(bean::withInstrumentation);
        graphqlContext.ifAvailable(bean::withGraphqlContext);
        queryExecutionStrategy.ifAvailable(bean::withQueryExecutionStrategy);
        mutationExecutionStrategy.ifAvailable(bean::withMutationExecutionStrategy);
        subscriptionExecutionStrategy.ifAvailable(bean::withSubscriptionExecutionStrategy);

        return bean;
    }

    @Bean
    @ConditionalOnMissingBean(ExecutionGraphQlService.class)
    @ConditionalOnBean(GraphQLSchema.class)
    public GraphQLExecutor graphQLJpaExecutor(
        GraphQLSchema graphQLSchema,
        GraphQLExecutorContextFactory graphQLExecutorContextFactory
    ) {
        return new GraphQLJpaExecutor(graphQLSchema, graphQLExecutorContextFactory);
    }
}
