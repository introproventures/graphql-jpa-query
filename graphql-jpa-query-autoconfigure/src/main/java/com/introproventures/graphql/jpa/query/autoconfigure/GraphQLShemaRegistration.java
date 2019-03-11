package com.introproventures.graphql.jpa.query.autoconfigure;

import graphql.schema.GraphQLSchema;

public interface GraphQLShemaRegistration {

	public void register(GraphQLSchema graphQLSchema);


}
