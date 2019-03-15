package com.introproventures.graphql.jpa.query.autoconfigure;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;

public class GraphQLSchemaFactoryBean extends AbstractFactoryBean<GraphQLSchema>{
	
	private static final String QUERY_NAME = "Query";
    private static final String QUERY_DESCRIPTION = "";
    private static final String SUBSCRIPTION_NAME = "Subscription";
    private static final String SUBSCRIPTION_DESCRIPTION = "";
    private static final String MUTATION_NAME = "Mutation";
    private static final String MUTATION_DESCRIPTION = "";

    
    private final GraphQLSchema[] managedGraphQLSchemas;
	
	private String queryName = QUERY_NAME;
	private String queryDescription = QUERY_DESCRIPTION;

    private String subscriptionName = SUBSCRIPTION_NAME;
    private String subscriptionDescription = SUBSCRIPTION_DESCRIPTION;

    private String mutationName = MUTATION_NAME;
    private String mutationDescription = MUTATION_DESCRIPTION;
    
	
	public GraphQLSchemaFactoryBean(GraphQLSchema[] managedGraphQLSchemas) {
		this.managedGraphQLSchemas = managedGraphQLSchemas;
	}

	@Override
	protected GraphQLSchema createInstance() throws Exception {
		
		GraphQLSchema.Builder schemaBuilder = GraphQLSchema.newSchema();
		
		List<GraphQLFieldDefinition> mutations = Stream.of(managedGraphQLSchemas)
			.map(GraphQLSchema::getMutationType)
			.filter(Objects::nonNull)
			.map(GraphQLObjectType::getFieldDefinitions)
			.flatMap(children -> children.stream())
			.collect(Collectors.toList());

		List<GraphQLFieldDefinition> queries = Stream.of(managedGraphQLSchemas)
			.map(GraphQLSchema::getQueryType)
			.filter(Objects::nonNull)
			.filter(it -> !it.getName().equals("null")) // filter out null placeholders
			.map(GraphQLObjectType::getFieldDefinitions)
			.flatMap(children -> children.stream())
			.collect(Collectors.toList());
		
		List<GraphQLFieldDefinition> subscriptions = Stream.of(managedGraphQLSchemas)
			.map(GraphQLSchema::getSubscriptionType)
			.filter(Objects::nonNull)
			.map(GraphQLObjectType::getFieldDefinitions)
			.flatMap(children -> children.stream())
			.collect(Collectors.toList());

		if(!mutations.isEmpty())
			schemaBuilder.mutation(GraphQLObjectType.newObject()
                                                    .name(this.mutationName)
                                                    .description(this.mutationDescription)
                                			        .fields(mutations));

		if(!queries.isEmpty())
			schemaBuilder.query(GraphQLObjectType.newObject()
					                             .name(this.queryName)
					                             .description(this.queryDescription)
					                             .fields(queries));

		if(!subscriptions.isEmpty())
			schemaBuilder.subscription(GraphQLObjectType.newObject()
                                                        .name(this.subscriptionName)
                                                        .description(this.subscriptionDescription)
                                    			        .fields(subscriptions));
		
		return schemaBuilder.build();
	}

	@Override
	public Class<?> getObjectType() {
		return GraphQLSchema.class;
	}

	public GraphQLSchemaFactoryBean setQueryName(String name) {
		this.queryName = name;
		
		return this;
	}

	public GraphQLSchemaFactoryBean setQueryDescription(String description) {
		this.queryDescription = description;
		
		return this;
	}

    public GraphQLSchemaFactoryBean setSubscriptionName(String subscriptionName) {
        this.subscriptionName = subscriptionName;

        return this;
    }

    public GraphQLSchemaFactoryBean setSubscriptionDescription(String subscriptionDescription) {
        this.subscriptionDescription = subscriptionDescription;

        return this;
    }

    public GraphQLSchemaFactoryBean setMutationName(String mutationName) {
        this.mutationName = mutationName;

        return this;
    }

    public GraphQLSchemaFactoryBean setMutationDescription(String mutationDescription) {
        this.mutationDescription = mutationDescription;

        return this;
    }

}
