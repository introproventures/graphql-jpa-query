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
package com.introproventures.graphql.jpa.query.schema;

import graphql.ExecutionResult;
import java.util.Map;

/**
 * GraphQLExecutor interface specification
 *
 * @author Igor Dianov
 *
 */
public interface GraphQLExecutor {
    /**
     * Execute GraphQL query provided in query argument
     *
     * @param query GraphQL query string
     * @return GraphQL ExecutionResult
     */
    ExecutionResult execute(String query);

    /**
     * Execute GraphQL query provided in query argument and variables
     *
     * @param query GraphQL query string
     * @param arguments GraphQL arguments key/value mapo
     * @return GraphQL ExecutionResult
     */
    ExecutionResult execute(String query, Map<String, Object> arguments);

    /**
     * Execute GraphQL query provided in query argument and variables
     *
     * @param query GraphQL query string
     * @param operationName GraphQL operationName string
     * @param arguments GraphQL arguments key/value mapo
     * @return GraphQL ExecutionResult
     */
    ExecutionResult execute(String query, String operationName, Map<String, Object> arguments);
}
