package com.introproventures.graphql.jpa.query.autoconfigure;

@FunctionalInterface
public interface GraphQLSchemaConfigurer {

	void configure(GraphQLShemaRegistration registry);

}
