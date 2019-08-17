package com.introproventures.graphql.jpa.query.schema.impl;

import java.util.Optional;

import graphql.execution.instrumentation.Instrumentation;
import graphql.execution.instrumentation.SimpleInstrumentation;
import graphql.schema.visibility.DefaultGraphqlFieldVisibility;
import graphql.schema.visibility.GraphqlFieldVisibility;

public interface GraphQLJpaExecutorContext {
    
    default GraphqlFieldVisibility graphQLFieldVisibility() {
        return new DefaultGraphqlFieldVisibility();
    };
    
    default Optional<Object> getExecutionContext() {
        return Optional.empty();
    }

    default Optional<Object> getExecutionRoot() {
        return Optional.empty();
    }
    
    default Instrumentation getInstrumentation() {
        return new SimpleInstrumentation();
    }
    
}
