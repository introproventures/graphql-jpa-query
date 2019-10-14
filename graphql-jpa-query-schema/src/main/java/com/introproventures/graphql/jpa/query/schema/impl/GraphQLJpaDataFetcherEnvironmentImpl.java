package com.introproventures.graphql.jpa.query.schema.impl;

import graphql.execution.ExecutionContext;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingEnvironmentImpl;

/**
 *  Wrapper class for {@link graphql.schema.DataFetchingEnvironmentImpl}
 *
 */
public class GraphQLJpaDataFetcherEnvironmentImpl {

    public static DataFetchingEnvironmentImpl.Builder newDataFetchingEnvironment() {
        return new DataFetchingEnvironmentImpl.Builder();
    }

    public static DataFetchingEnvironmentImpl.Builder newDataFetchingEnvironment(DataFetchingEnvironment environment) {
        return new DataFetchingEnvironmentImpl.Builder((DataFetchingEnvironmentImpl) environment);
    }

    public static DataFetchingEnvironmentImpl.Builder newDataFetchingEnvironment(ExecutionContext executionContext) {
        return new DataFetchingEnvironmentImpl.Builder()
            .context(executionContext.getContext())
            .root(executionContext.getRoot())
            .graphQLSchema(executionContext.getGraphQLSchema())
            .fragmentsByName(executionContext.getFragmentsByName())
            .dataLoaderRegistry(executionContext.getDataLoaderRegistry())
            .cacheControl(executionContext.getCacheControl())
            .document(executionContext.getDocument())
            .operationDefinition(executionContext.getOperationDefinition())
            .variables(executionContext.getVariables())
            .executionId(executionContext.getExecutionId());
    }

}
