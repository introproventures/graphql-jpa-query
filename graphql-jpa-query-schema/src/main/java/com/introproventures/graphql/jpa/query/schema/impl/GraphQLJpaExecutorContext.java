package com.introproventures.graphql.jpa.query.schema.impl;

import java.util.Optional;

import graphql.execution.instrumentation.Instrumentation;
import graphql.schema.visibility.GraphqlFieldVisibility;

public interface GraphQLJpaExecutorContext {
    
    default Optional<GraphqlFieldVisibility> graphQLFieldVisibility() {
        return Optional.empty();
    };
    
    default Optional<Object> getExecutionContext() {
        return Optional.empty();
    }

    default Optional<Object> getExecutionRoot() {
        return Optional.empty();
    }
    
    default Optional<Instrumentation> getInstrumentation() {
        return Optional.empty();
    }
    
}
