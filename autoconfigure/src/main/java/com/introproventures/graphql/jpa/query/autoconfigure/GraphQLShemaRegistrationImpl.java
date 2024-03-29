package com.introproventures.graphql.jpa.query.autoconfigure;

import graphql.schema.GraphQLSchema;
import java.util.LinkedHashSet;
import java.util.Set;

public class GraphQLShemaRegistrationImpl implements GraphQLShemaRegistration {

    Set<GraphQLSchema> managedGraphQLSchemas = new LinkedHashSet<GraphQLSchema>();

    public void register(GraphQLSchema graphQLSchema) {
        managedGraphQLSchemas.add(graphQLSchema);
    }

    public GraphQLSchema[] getManagedGraphQLSchemas() {
        return managedGraphQLSchemas.toArray(new GraphQLSchema[] {});
    }
}
