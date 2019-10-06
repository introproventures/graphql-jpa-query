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

import java.util.Optional;
import java.util.function.Consumer;

import graphql.ExecutionInput;
import graphql.execution.instrumentation.Instrumentation;
import graphql.schema.GraphQLSchema;

public interface GraphQLExecutorContext {
    
    ExecutionInput.Builder newExecutionInput();

    default Optional<Consumer<GraphQLSchema.Builder>> schemaBuilder(GraphQLSchema schema) {
        return Optional.empty();
    }
    
    default Optional<Instrumentation> instrumentation() {
        return Optional.empty();
    }
    
}
