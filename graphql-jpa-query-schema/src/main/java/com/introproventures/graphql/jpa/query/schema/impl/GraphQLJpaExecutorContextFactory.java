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

import com.introproventures.graphql.jpa.query.schema.GraphQLExecutionInputFactory;
import com.introproventures.graphql.jpa.query.schema.GraphQLExecutorContext;
import com.introproventures.graphql.jpa.query.schema.GraphQLExecutorContextFactory;

import graphql.GraphQLContext;
import graphql.execution.instrumentation.Instrumentation;
import graphql.execution.instrumentation.SimpleInstrumentation;
import graphql.schema.GraphQLSchema;
import graphql.schema.visibility.DefaultGraphqlFieldVisibility;
import graphql.schema.visibility.GraphqlFieldVisibility;

public class GraphQLJpaExecutorContextFactory implements GraphQLExecutorContextFactory {
    
    private GraphQLExecutionInputFactory executionInputFactory = new GraphQLExecutionInputFactory() {};
    private GraphqlFieldVisibility graphqlFieldVisibility = DefaultGraphqlFieldVisibility.DEFAULT_FIELD_VISIBILITY;
    private Instrumentation instrumentation = new SimpleInstrumentation();
    private Supplier<GraphQLContext> graphqlContext = () -> GraphQLContext.newContext().build();
    
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
                                        .build();
    }

    public GraphQLJpaExecutorContextFactory withGraphqlFieldVisibility(GraphqlFieldVisibility graphqlFieldVisibility) {
        this.graphqlFieldVisibility = graphqlFieldVisibility;
        return this;
    }

    
    public GraphQLJpaExecutorContextFactory withInstrumentation(Instrumentation instrumentation) {
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
    
    public GraphQLExecutionInputFactory getExecutionInputFactory() {
        return executionInputFactory;
    }
    
    public GraphqlFieldVisibility getGraphqlFieldVisibility() {
        return graphqlFieldVisibility;
    }

    public Instrumentation getInstrumentation() {
        return instrumentation;
    }

    
    public Supplier<GraphQLContext> getGraphqlContext() {
        return graphqlContext;
    }

}
