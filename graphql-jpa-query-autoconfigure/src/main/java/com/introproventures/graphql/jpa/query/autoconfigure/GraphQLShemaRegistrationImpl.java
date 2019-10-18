package com.introproventures.graphql.jpa.query.autoconfigure;

import java.util.LinkedHashSet;
import java.util.Set;

import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLSchema;

public class GraphQLShemaRegistrationImpl implements GraphQLShemaRegistration {

	private Set<GraphQLSchema> managedGraphQLSchemas = new LinkedHashSet<GraphQLSchema>();

	private GraphQLCodeRegistry codeRegistry;

    @Override
    public void setCustomDataFetcher(GraphQLCodeRegistry codeRegistry) {
        this.codeRegistry = codeRegistry;
    }

    @Override
	public void register(GraphQLSchema graphQLSchema) {
		managedGraphQLSchemas.add(graphQLSchema);
	}

	public GraphQLSchema[] getManagedGraphQLSchemas() {
		return managedGraphQLSchemas.toArray(new GraphQLSchema[] {});
	}

    public GraphQLCodeRegistry getCodeRegistry() {
        return codeRegistry;
    }
}
