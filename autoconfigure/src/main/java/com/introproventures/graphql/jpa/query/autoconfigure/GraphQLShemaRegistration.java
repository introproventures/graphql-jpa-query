package com.introproventures.graphql.jpa.query.autoconfigure;

import graphql.schema.GraphQLSchema;

public interface GraphQLShemaRegistration {
    void register(GraphQLSchema graphQLSchema);

    GraphQLSchema[] getManagedGraphQLSchemas();
}
