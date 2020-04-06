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

import java.util.function.Supplier;

import org.dataloader.DataLoaderOptions;
import org.dataloader.DataLoaderRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.introproventures.graphql.jpa.query.schema.GraphQLExecutionInputFactory;
import com.introproventures.graphql.jpa.query.schema.GraphQLExecutorContext;
import com.introproventures.graphql.jpa.query.schema.GraphQLExecutorContextFactory;
import graphql.GraphQLContext;
import graphql.execution.instrumentation.Instrumentation;
import graphql.execution.instrumentation.SimpleInstrumentation;
import graphql.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentationOptions;
import graphql.schema.GraphQLSchema;
import graphql.schema.visibility.DefaultGraphqlFieldVisibility;
import graphql.schema.visibility.GraphqlFieldVisibility;

public class GraphQLJpaExecutorContextFactory implements GraphQLExecutorContextFactory {
    private final static Logger logger = LoggerFactory.getLogger(GraphQLJpaExecutorContext.class);

    private GraphQLExecutionInputFactory executionInputFactory = new GraphQLExecutionInputFactory() {};
    private Supplier<GraphqlFieldVisibility> graphqlFieldVisibility = () -> DefaultGraphqlFieldVisibility.DEFAULT_FIELD_VISIBILITY;
    private Supplier<Instrumentation> instrumentation = () -> new SimpleInstrumentation();
    private Supplier<GraphQLContext> graphqlContext = () -> GraphQLContext.newContext().build();
    private Supplier<DataLoaderDispatcherInstrumentationOptions> dataLoaderDispatcherInstrumentationOptions = () -> {
        return DataLoaderDispatcherInstrumentationOptions.newOptions();
    };

    private Supplier<DataLoaderOptions> dataLoaderOptions = () -> DataLoaderOptions.newOptions();

    private Supplier<DataLoaderRegistry> dataLoaderRegistry = () -> {
        DataLoaderOptions options = dataLoaderOptions.get()
                                                     .setCachingEnabled(false);

        return BatchLoaderRegistry.newDataLoaderRegistry(options);
    };


    public GraphQLJpaExecutorContextFactory() {
    }

    @Override
    public GraphQLExecutorContext newExecutorContext(GraphQLSchema graphQLSchema) {
        return GraphQLJpaExecutorContext.builder()
                                        .graphQLSchema(graphQLSchema)
                                        .executionInputFactory(executionInputFactory)
                                        .graphqlFieldVisibility(graphqlFieldVisibility)
                                        .instrumentation(instrumentation)
                                        .graphqlContext(graphqlContext)
                                        .dataLoaderDispatcherInstrumentationOptions(dataLoaderDispatcherInstrumentationOptions)
                                        .dataLoaderRegistry(dataLoaderRegistry)
                                        .build();
    }

    public GraphQLJpaExecutorContextFactory withGraphqlFieldVisibility(Supplier<GraphqlFieldVisibility> graphqlFieldVisibility) {
        this.graphqlFieldVisibility = graphqlFieldVisibility;
        return this;
    }


    public GraphQLJpaExecutorContextFactory withInstrumentation(Supplier<Instrumentation> instrumentation) {
        this.instrumentation = instrumentation;
        return this;
    }


    public GraphQLJpaExecutorContextFactory withExecutionInputFactory(GraphQLExecutionInputFactory executionInputFactory) {
        this.executionInputFactory = executionInputFactory;
        return this;
    }

    public GraphQLJpaExecutorContextFactory withGraphqlContext(Supplier<GraphQLContext> graphqlContext) {
        this.graphqlContext = graphqlContext;
        return this;
    };

    public GraphQLJpaExecutorContextFactory withDataLoaderDispatcherInstrumentationOptions(Supplier<DataLoaderDispatcherInstrumentationOptions> dataLoaderDispatcherInstrumentationOptions) {
        this.dataLoaderDispatcherInstrumentationOptions = dataLoaderDispatcherInstrumentationOptions;
        return this;
    }

    public GraphQLExecutionInputFactory getExecutionInputFactory() {
        return executionInputFactory;
    }

    public Supplier<GraphqlFieldVisibility> getGraphqlFieldVisibility() {
        return graphqlFieldVisibility;
    }

    public Supplier<Instrumentation> getInstrumentation() {
        return instrumentation;
    }


    public Supplier<GraphQLContext> getGraphqlContext() {
        return graphqlContext;
    }

    public Supplier<DataLoaderDispatcherInstrumentationOptions> getDataLoaderDispatcherInstrumentationOptions() {
        return dataLoaderDispatcherInstrumentationOptions;
    }

}
