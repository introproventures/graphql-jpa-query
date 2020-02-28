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

import graphql.ExecutionInput;
import graphql.GraphQL;
import graphql.GraphQLContext;
import graphql.execution.instrumentation.Instrumentation;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLSchema;
import graphql.schema.visibility.GraphqlFieldVisibility;

public class GraphQLJpaExecutorContext implements GraphQLExecutorContext {
    
    private final GraphQLSchema graphQLSchema;
    private final GraphQLExecutionInputFactory executionInputFactory;
    private final Supplier<GraphqlFieldVisibility> graphqlFieldVisibility;
    private final Supplier<Instrumentation> instrumentation;
    private final Supplier<GraphQLContext> graphqlContext;

    private GraphQLJpaExecutorContext(Builder builder) {
        this.graphQLSchema = builder.graphQLSchema;
        this.executionInputFactory = builder.executionInputFactory;
        this.graphqlFieldVisibility = builder.graphqlFieldVisibility;
        this.instrumentation = builder.instrumentation;
        this.graphqlContext = builder.graphqlContext;
    }
    
    @Override
    public ExecutionInput.Builder newExecutionInput() {
        return executionInputFactory.create()
                                    .context(graphqlContext.get());
    }

    @Override
    public GraphQL.Builder newGraphQL() {
        return GraphQL.newGraphQL(getGraphQLSchema())
                      .instrumentation(instrumentation.get());
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
        public GraphQLJpaExecutorContext build() {
            return new GraphQLJpaExecutorContext(this);
        }

    }
}
