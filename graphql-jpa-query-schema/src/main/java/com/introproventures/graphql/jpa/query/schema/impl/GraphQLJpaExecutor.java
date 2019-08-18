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

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import com.introproventures.graphql.jpa.query.schema.GraphQLExecutor;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;

/**
 * Jpa specific GraphQLExecutor implementation with support to execute GraphQL query within
 * existing transaction context
 * 
 * @author Igor Dianov
 *
 */
public class GraphQLJpaExecutor implements GraphQLExecutor {

    private final GraphQLSchema graphQLSchema;

    private final GraphQLJpaExecutorContextFactory contextFactory;
    
    /**
     * Creates instance using GraphQLSchema parameter with default GraphQLJpaExecutorContextFactory
     *  
     * @param graphQLSchema instance
     */
    public GraphQLJpaExecutor(GraphQLSchema graphQLSchema) {
        this(graphQLSchema, new GraphQLJpaExecutorContextFactory() {});
    }
    
    /**
     * Creates instance using GraphQLSchema and GraphQLJpaExecutorContextFactory
     *  
     * @param graphQLSchema instance
     * @param GraphQLJpaExecutorContextFactory contextFactory
     */
    public GraphQLJpaExecutor(GraphQLSchema graphQLSchema,
                              GraphQLJpaExecutorContextFactory contextFactory) {
        this.graphQLSchema = graphQLSchema;
        this.contextFactory = contextFactory;
    }
    

    /* (non-Javadoc)
     * @see org.activiti.services.query.qraphql.jpa.QraphQLExecutor#execute(java.lang.String)
     */
    @Override
    @Transactional(TxType.REQUIRED)
    public ExecutionResult execute(String query) {
        return execute(query, Collections.emptyMap());
    }

    /* (non-Javadoc)
     * @see org.activiti.services.query.qraphql.jpa.QraphQLExecutor#execute(java.lang.String, java.util.Map)
     */
    @Override
    @Transactional(TxType.REQUIRED)
    public ExecutionResult execute(String query, Map<String, Object> arguments) {

        GraphQLJpaExecutorContext executorContext = contextFactory.create();

        GraphQLSchema schema = executorContext.graphQLFieldVisibility()
                                              .map(graphQLFieldVisibility -> 
                                                   graphQLSchema.transform(builder -> 
                                                                           builder.fieldVisibility(graphQLFieldVisibility)))
                                              .orElse(graphQLSchema);
        
        GraphQL.Builder graphQL = GraphQL.newGraphQL(schema);
        
        executorContext.getInstrumentation()
                       .ifPresent(graphQL::instrumentation);
        
        ExecutionInput.Builder executionInput = ExecutionInput.newExecutionInput()
                                                              .query(query)
                                                              .variables(arguments);
        executorContext.getExecutionContext()
                       .ifPresent(executionInput::context);

        executorContext.getExecutionRoot()
                       .ifPresent(executionInput::root);
        
        return graphQL.build()
                      .execute(executionInput);
    }

}
