package com.introproventures.graphql.jpa.query.mutations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.introproventures.graphql.jpa.query.mutations.fetcher.impl.*;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

import com.introproventures.graphql.jpa.query.schema.impl.FetcherParams;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;

import graphql.schema.*;


public class GraphQLJpaSchemaBuilderWithMutation extends GraphQLJpaSchemaBuilder {
	public class GqlMutationOper {
		private String operation;
		private String description;
		private Class<? extends GraphQLJpaEntityInputFetcher> fetcher;
		
		public GqlMutationOper(String oper, String description, Class<? extends GraphQLJpaEntityInputFetcher> fetcher) {
			this.operation = oper;
			this.description = description;
			this.fetcher = fetcher;
		}

		public String getOperation() {
			return operation;
		}

		public void setOperation(String operation) {
			this.operation = operation;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public Class<? extends GraphQLJpaEntityInputFetcher> getFetcher() {
			return fetcher;
		}

		public void setFetcher(Class<? extends GraphQLJpaEntityInputFetcher> fetcher) {
			this.fetcher = fetcher;
		}		
	}

	protected String suffixInputObjectType = "Input";
	private List<GqlMutationOper> operations = new ArrayList<>(); 
	private Map<EntityType<?>, GraphQLInputObjectType> entityInputCache = new HashMap<>();

	public GraphQLJpaSchemaBuilderWithMutation(EntityManager em) {
		super(em);

		operations.add(new GqlMutationOper("insert", "This insert entity", InsertFecher.class));
		operations.add(new GqlMutationOper("update", "This update entity", UpdateFetcher.class));
		operations.add(new GqlMutationOper("merge", "This update entity", MergeFetcher.class));
		operations.add(new GqlMutationOper("delete", "This delete entity", DeleteFetcher.class));
	}

	public GraphQLJpaSchemaBuilderWithMutation suffixInputObjectType(String suffixInputObjectType) {
		this.suffixInputObjectType = suffixInputObjectType;
		return this;
	}

	@Override
	public GraphQLSchema build() {
		createFetcherParams();
		return GraphQLSchema.newSchema()
				.query(getQueryType())
	            .mutation(getMutation())
	            .build();
	}
	
	protected GraphQLObjectType getMutation() {
		GraphQLObjectType.Builder queryType = 
	        GraphQLObjectType.newObject()
	                .name("editEntities")
	                .description(this.getDescription());
		
		List<GraphQLFieldDefinition> fields = new ArrayList<>();

		for (EntityType<?> entityType : entityManager.getMetamodel().getEntities()) {
			if (!isNotIgnored(entityType)) continue;

			for (GqlMutationOper oper : operations) {				
				fields.add(getMutationDefinition(entityType, oper));
			}
		}
	        
	    queryType.fields(fields);	        			            
	    return queryType.build();
	}
	
	private GraphQLFieldDefinition getMutationDefinition(EntityType<?> entityType, GqlMutationOper oper) {		
        return GraphQLFieldDefinition.newFieldDefinition()
                .name(oper.getOperation()+entityType.getName())
                .description(oper.getDescription())
                .type(getObjectType(entityType))
                .dataFetcher(getInputFetcher(entityType, oper))
                .argument(getArgumentInputEntity(entityType))                
                .build();
    } 
	
	private GraphQLJpaEntityInputFetcher getInputFetcher(EntityType<?> entityType, GqlMutationOper oper) {
		Class<? extends GraphQLJpaEntityInputFetcher> classFetch  = oper.getFetcher();
		try {
			return classFetch.getDeclaredConstructor(EntityManager.class, FetcherParams.class, EntityType.class)
					.newInstance(entityManager, fetcherParams, entityType);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}		
	}

	
	private GraphQLArgument getArgumentInputEntity(EntityType<?> entityType) {
		return GraphQLArgument.newArgument()
	        .name("entity")
	        .type(getObjectInputType(entityType))
	        .description("this description entity " + entityType.getName())
	        .build();
	}
	
    private GraphQLInputObjectType getObjectInputType(EntityType<?> entityType) {
        if (entityInputCache.containsKey(entityType))
            return entityInputCache.get(entityType);        
        
        GraphQLInputObjectType objectInputType = GraphQLInputObjectType.newInputObject()
                .name(entityType.getName()+suffixInputObjectType)
                .description("Input type GraphQL for entity "+entityType.getName())
                
                .fields(entityType.getAttributes().stream()
                    .filter(this::isNotIgnored)
                    .map(this::getObjectInputAttribute)
                    .collect(Collectors.toList())
                )
                .build();
        
        entityInputCache.putIfAbsent(entityType, objectInputType);
        
        return objectInputType;
    }
    
    public GraphQLInputObjectField getObjectInputAttribute(Attribute<?,?> attribute) {   
    	GraphQLType type = getAttributeInputType(attribute);
    	
    	if (!(type instanceof GraphQLInputType)) {
    		throw new IllegalArgumentException("Attribute " + attribute + " cannot be instanceof GraphQLInputType");
    	}
    	
    	GraphQLInputObjectField field = GraphQLInputObjectField.newInputObjectField()
    			.name(attribute.getName())
    			.type((GraphQLInputType)type)    			
    			.build();
    	
    	return field;
    }

    @SuppressWarnings( "rawtypes" )
	protected GraphQLType getAttributeInputType(Attribute<?,?> attribute) {
    	
        if (isBasic(attribute)) {
        	return getGraphQLTypeFromJavaType(attribute.getJavaType());        	
        } 
        else if (isEmbeddable(attribute)) {
        	EmbeddableType embeddableType = (EmbeddableType) ((SingularAttribute) attribute).getType();
        	return getEmbeddableType(embeddableType, true );
        } 
        else if (isToMany(attribute)) {
            EntityType foreignType = (EntityType) ((PluralAttribute) attribute).getElementType();
            return new GraphQLList(new GraphQLTypeReference(foreignType.getName()+suffixInputObjectType));
        } 
        else if (isToOne(attribute)) {
            EntityType foreignType = (EntityType) ((SingularAttribute) attribute).getType();
            return new GraphQLTypeReference(foreignType.getName()+suffixInputObjectType);
        } 
        else if (isElementCollection(attribute)) {
            Type foreignType = ((PluralAttribute) attribute).getElementType();
            
            if(foreignType.getPersistenceType() == Type.PersistenceType.BASIC) {
            	return new GraphQLList(getGraphQLTypeFromJavaType(foreignType.getJavaType()));        	
            }
        }

        final String declaringType = attribute.getDeclaringType().getJavaType().getName(); // fully qualified name of the entity class
        final String declaringMember = attribute.getJavaMember().getName(); // field name in the entity class

        throw new UnsupportedOperationException(
                "Attribute could not be mapped to GraphQL: field '" + declaringMember + "' of entity class '"+ declaringType +"'");
    }
}
