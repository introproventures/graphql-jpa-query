/*
 * Copyright 2017 IntroPro Ventures Inc. and/or its affiliates.
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

package com.introproventures.graphql.jpa.query.schema.impl;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import com.introproventures.graphql.jpa.query.schema.GraphQLExecutionInputFactory;
import com.introproventures.graphql.jpa.query.schema.GraphQLExecutorContext;
import graphql.ExecutionInput;
import graphql.GraphQL;
import graphql.GraphQLContext;
import graphql.execution.ExecutionStrategy;
import graphql.execution.instrumentation.ChainedInstrumentation;
import graphql.execution.instrumentation.Instrumentation;
import graphql.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentation;
import graphql.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentationOptions;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLSchema;
import graphql.schema.visibility.GraphqlFieldVisibility;
import org.dataloader.DataLoaderRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphQLJpaExecutorContext implements GraphQLExecutorContext {

    private final static Logger logger = LoggerFactory.getLogger(GraphQLJpaExecutorContext.class);

    private final GraphQLSchema graphQLSchema;
    private final GraphQLExecutionInputFactory executionInputFactory;
    private final Supplier<GraphqlFieldVisibility> graphqlFieldVisibility;
    private final Supplier<Instrumentation> instrumentation;
    private final Supplier<GraphQLContext> graphqlContext;
    private final Supplier<DataLoaderDispatcherInstrumentationOptions> dataLoaderDispatcherInstrumentationOptions;
    private final Supplier<DataLoaderRegistry> dataLoaderRegistry;
    private final Supplier<ExecutionStrategy> queryExecutionStrategy;
    private final Supplier<ExecutionStrategy> mutationExecutionStrategy;
    private final Supplier<ExecutionStrategy> subscriptionExecutionStrategy;

    private GraphQLJpaExecutorContext(Builder builder) {
        this.graphQLSchema = builder.graphQLSchema;
        this.executionInputFactory = builder.executionInputFactory;
        this.graphqlFieldVisibility = builder.graphqlFieldVisibility;
        this.instrumentation = builder.instrumentation;
        this.graphqlContext = builder.graphqlContext;
        this.dataLoaderDispatcherInstrumentationOptions = builder.dataLoaderDispatcherInstrumentationOptions;
        this.dataLoaderRegistry = builder.dataLoaderRegistry;
        this.queryExecutionStrategy = builder.queryExecutionStrategy;
        this.mutationExecutionStrategy = builder.mutationExecutionStrategy;
        this.subscriptionExecutionStrategy = builder.subscriptionExecutionStrategy;
    }

    @Override
    public ExecutionInput.Builder newExecutionInput() {
        DataLoaderRegistry dataLoaderRegistry = newDataLoaderRegistry();
        GraphQLContext context = newGraphQLContext();

        return executionInputFactory.create()
                                    .dataLoaderRegistry(dataLoaderRegistry)
                                    .context(context);
    }

    @Override
    public GraphQL.Builder newGraphQL() {
        Instrumentation instrumentation = newIstrumentation();

        return GraphQL.newGraphQL(getGraphQLSchema())
                      .instrumentation(instrumentation)
                      .queryExecutionStrategy(queryExecutionStrategy.get())
                      .mutationExecutionStrategy(mutationExecutionStrategy.get())
                      .subscriptionExecutionStrategy(subscriptionExecutionStrategy.get());
    }

    public GraphQLContext newGraphQLContext() {
        return graphqlContext.get();
    }

    public DataLoaderRegistry newDataLoaderRegistry() {
        return dataLoaderRegistry.get();
    }

    public Instrumentation newIstrumentation() {
        DataLoaderDispatcherInstrumentationOptions options = dataLoaderDispatcherInstrumentationOptions.get();

        DataLoaderDispatcherInstrumentation dispatcherInstrumentation = new DataLoaderDispatcherInstrumentation(options);

        List<Instrumentation> list = Arrays.asList(dispatcherInstrumentation,
                                                   instrumentation.get());

        return new ChainedInstrumentation(list);
    }

    @Override
    public GraphQLSchema getGraphQLSchema() {
        GraphQLCodeRegistry codeRegistry = graphQLSchema.getCodeRegistry()
                                                        .transform(builder -> builder.fieldVisibility(graphqlFieldVisibility.get()));

        return graphQLSchema.transform(builder -> builder.codeRegistry(codeRegistry));
    }

    /**
     * Creates builder to build {@link GraphQLJpaExecutorContext}.
     * @return created builder
     */
    public static IGraphQLSchemaStage builder() {
        return new Builder();
    }

    public interface IGraphQLSchemaStage {

        public IBuildStage graphQLSchema(GraphQLSchema graphQLSchema);
    }

    public interface IBuildStage {

        public IBuildStage executionInputFactory(GraphQLExecutionInputFactory executionInputFactory);

        public IBuildStage graphqlFieldVisibility(Supplier<GraphqlFieldVisibility> graphqlFieldVisibility);

        public IBuildStage instrumentation(Supplier<Instrumentation> instrumentation);

        public IBuildStage graphqlContext(Supplier<GraphQLContext> graphqlContext);

        public IBuildStage dataLoaderDispatcherInstrumentationOptions(Supplier<DataLoaderDispatcherInstrumentationOptions> dataLoaderDispatcherInstrumentationOptions);

        public IBuildStage dataLoaderRegistry(Supplier<DataLoaderRegistry> dataLoaderRegistry);

        public IBuildStage queryExecutionStrategy(Supplier<ExecutionStrategy> queryExecutionStrategy);

        public IBuildStage mutationExecutionStrategy(Supplier<ExecutionStrategy> mutationExecutionStrategy);

        public IBuildStage subscriptionExecutionStrategy(Supplier<ExecutionStrategy> subscriptionExecutionStrategy);

        public GraphQLJpaExecutorContext build();
    }

    /**
     * Builder to build {@link GraphQLJpaExecutorContext}.
     */
    public static final class Builder implements IGraphQLSchemaStage, IBuildStage {

        private GraphQLSchema graphQLSchema;
        private GraphQLExecutionInputFactory executionInputFactory;
        private Supplier<GraphqlFieldVisibility> graphqlFieldVisibility;
        private Supplier<Instrumentation> instrumentation;
        private Supplier<GraphQLContext> graphqlContext;
        private Supplier<DataLoaderDispatcherInstrumentationOptions> dataLoaderDispatcherInstrumentationOptions;
        private Supplier<DataLoaderRegistry> dataLoaderRegistry;
        private Supplier<ExecutionStrategy> queryExecutionStrategy;
        private Supplier<ExecutionStrategy> mutationExecutionStrategy;
        private Supplier<ExecutionStrategy> subscriptionExecutionStrategy;

        private Builder() {
        }

        @Override
        public IBuildStage graphQLSchema(GraphQLSchema graphQLSchema) {
            this.graphQLSchema = graphQLSchema;
            return this;
        }

        @Override
        public IBuildStage executionInputFactory(GraphQLExecutionInputFactory executionInputFactory) {
            this.executionInputFactory = executionInputFactory;
            return this;
        }

        @Override
        public IBuildStage graphqlFieldVisibility(Supplier<GraphqlFieldVisibility> graphqlFieldVisibility) {
            this.graphqlFieldVisibility = graphqlFieldVisibility;
            return this;
        }

        @Override
        public IBuildStage instrumentation(Supplier<Instrumentation> instrumentation) {
            this.instrumentation = instrumentation;
            return this;
        }

        @Override
        public IBuildStage graphqlContext(Supplier<GraphQLContext> graphqlContext) {
            this.graphqlContext = graphqlContext;
            return this;
        }

        @Override
        public IBuildStage dataLoaderDispatcherInstrumentationOptions(Supplier<DataLoaderDispatcherInstrumentationOptions> dataLoaderDispatcherInstrumentationOptions) {
            this.dataLoaderDispatcherInstrumentationOptions = dataLoaderDispatcherInstrumentationOptions;

            return this;
        }

        @Override
        public IBuildStage dataLoaderRegistry(Supplier<DataLoaderRegistry> dataLoaderRegistry) {
            this.dataLoaderRegistry = dataLoaderRegistry;

            return this;
        }

        @Override
        public IBuildStage queryExecutionStrategy(Supplier<ExecutionStrategy> queryExecutionStrategy) {
            this.queryExecutionStrategy = queryExecutionStrategy;

            return this;
        }

        @Override
        public IBuildStage mutationExecutionStrategy(Supplier<ExecutionStrategy> mutationExecutionStrategy) {
            this.mutationExecutionStrategy = mutationExecutionStrategy;

            return this;
        }

        @Override
        public IBuildStage subscriptionExecutionStrategy(Supplier<ExecutionStrategy> subscriptionExecutionStrategy) {
            this.subscriptionExecutionStrategy = subscriptionExecutionStrategy;

            return this;
        }

        @Override
        public GraphQLJpaExecutorContext build() {
            return new GraphQLJpaExecutorContext(this);
        }
    }
}
