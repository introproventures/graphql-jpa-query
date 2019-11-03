package com.introproventures.graphql.jpa.query.autoconfigure;

import static graphql.Assert.assertTrue;
import static graphql.schema.FieldCoordinates.coordinates;
import static graphql.util.TraversalControl.CONTINUE;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import graphql.Internal;
import graphql.schema.DataFetcher;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeVisitorStub;
import graphql.schema.GraphQLUnionType;
import graphql.schema.PropertyDataFetcher;
import graphql.schema.TypeResolver;
import graphql.schema.TypeTraverser;
import graphql.util.TraversalControl;
import graphql.util.TraverserContext;

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
        GraphQLCodeRegistry.Builder codeRegistryBuilder = GraphQLCodeRegistry.newCodeRegistry();
        TypeTraverser typeTraverser = new TypeTraverser();
		
		List<GraphQLFieldDefinition> mutations = Stream.of(managedGraphQLSchemas)
            .filter(it -> it.getMutationType() != null)
            .peek(schema -> {
                schema.getCodeRegistry().transform(builderConsumer -> {
                    typeTraverser.depthFirst(new CodeRegistryVisitor(builderConsumer, 
                                                                     codeRegistryBuilder, 
                                                                     mutationName), 
                                             schema.getMutationType());
                });
            })
	        .map(GraphQLSchema::getMutationType)
			.filter(Objects::nonNull)
			.map(GraphQLObjectType::getFieldDefinitions)
			.flatMap(Collection::stream)
			.collect(Collectors.toList());
		
		List<GraphQLFieldDefinition> queries = Stream.of(managedGraphQLSchemas)
            .filter(it -> Optional.ofNullable(it.getQueryType())
                                  .map(GraphQLType::getName)
                                  .isPresent())
            .peek(schema -> {
                schema.getCodeRegistry().transform(builderConsumer -> {
                     typeTraverser.depthFirst(new CodeRegistryVisitor(builderConsumer,
                                                                      codeRegistryBuilder,
                                                                      queryName),
                                              schema.getQueryType());
                 });
            })
			.map(GraphQLSchema::getQueryType)
			.filter(it -> !it.getName().equals("null")) // filter out null placeholders
			.map(GraphQLObjectType::getFieldDefinitions)
			.flatMap(Collection::stream)
			.collect(Collectors.toList());

		List<GraphQLFieldDefinition> subscriptions = Stream.of(managedGraphQLSchemas)
            .filter(it -> it.getSubscriptionType() != null)
		    .peek(schema -> {
                schema.getCodeRegistry().transform(builderConsumer -> {
                    typeTraverser.depthFirst(new CodeRegistryVisitor(builderConsumer,
                                                                     codeRegistryBuilder,
                                                                     subscriptionName),
                                             schema.getSubscriptionType());
                });
		    })
			.map(GraphQLSchema::getSubscriptionType)
			.map(GraphQLObjectType::getFieldDefinitions)
			.flatMap(Collection::stream)
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
		
        return schemaBuilder.codeRegistry(codeRegistryBuilder.build())
                            .build();
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
    
    /**
     * This ensure that all fields have data fetchers and that unions and interfaces have type resolvers
     */
    @Internal
    class CodeRegistryVisitor extends GraphQLTypeVisitorStub {
        private final GraphQLCodeRegistry.Builder source;
        private final GraphQLCodeRegistry.Builder codeRegistry;
        private final String root;

        CodeRegistryVisitor(GraphQLCodeRegistry.Builder source,
                            GraphQLCodeRegistry.Builder codeRegistry, 
                            String root) {
            this.source = source;
            this.codeRegistry = codeRegistry;
            this.root = root;
        }

        @Override
        public TraversalControl visitGraphQLFieldDefinition(GraphQLFieldDefinition node, TraverserContext<GraphQLType> context) {
            GraphQLFieldsContainer parentContainerType = (GraphQLFieldsContainer) context.getParentContext().thisNode();
            DataFetcher<?> dataFetcher = source.getDataFetcher(parentContainerType, node);
            if (dataFetcher == null) {
                dataFetcher = new PropertyDataFetcher<>(node.getName());
            }
            FieldCoordinates coordinates = coordinates(root, node.getName());
            codeRegistry.dataFetcherIfAbsent(coordinates, dataFetcher);
            return CONTINUE;
        }

        @Override
        public TraversalControl visitGraphQLInterfaceType(GraphQLInterfaceType node, TraverserContext<GraphQLType> context) {
            TypeResolver typeResolver = codeRegistry.getTypeResolver(node);

            if (typeResolver != null) {
                codeRegistry.typeResolverIfAbsent(node, typeResolver);
            }
            assertTrue(codeRegistry.getTypeResolver(node) != null, "You MUST provide a type resolver for the interface type '" + node.getName() + "'");
            return CONTINUE;
        }

        @Override
        public TraversalControl visitGraphQLUnionType(GraphQLUnionType node, TraverserContext<GraphQLType> context) {
            TypeResolver typeResolver = codeRegistry.getTypeResolver(node);
            if (typeResolver != null) {
                codeRegistry.typeResolverIfAbsent(node, typeResolver);
            }
            assertTrue(codeRegistry.getTypeResolver(node) != null, "You MUST provide a type resolver for the union type '" + node.getName() + "'");
            return CONTINUE;
        }
    }
    
    
}
