package com.introproventures.graphql.jpa.query.autoconfigure;

import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLSchema;

public interface GraphQLShemaRegistration {

	void register(GraphQLSchema graphQLSchema);

	void setCustomDataFetcher(GraphQLCodeRegistry codeRegistry);
}
