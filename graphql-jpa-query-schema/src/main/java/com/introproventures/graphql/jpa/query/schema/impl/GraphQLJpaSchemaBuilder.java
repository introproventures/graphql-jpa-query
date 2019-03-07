/*
 * Copyright 2017 IntroPro Ventures Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.introproventures.graphql.jpa.query.schema.impl;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.Transient;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.introproventures.graphql.jpa.query.annotation.GraphQLDescription;
import com.introproventures.graphql.jpa.query.annotation.GraphQLIgnore;
import com.introproventures.graphql.jpa.query.schema.GraphQLSchemaBuilder;
import com.introproventures.graphql.jpa.query.schema.JavaScalars;
import com.introproventures.graphql.jpa.query.schema.NamingStrategy;
import com.introproventures.graphql.jpa.query.schema.impl.PredicateFilter.Criteria;

import graphql.Assert;
import graphql.Scalars;
import graphql.schema.Coercing;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;
import graphql.schema.PropertyDataFetcher;

/**
 * JPA specific schema builder implementation of {code #GraphQLSchemaBuilder} interface
 * 
 * @author Igor Dianov
 *
 */
public class GraphQLJpaSchemaBuilder implements GraphQLSchemaBuilder {

    public static final String PAGE_PARAM_NAME = "page";
    public static final String PAGE_TOTAL_PARAM_NAME = "total";
    public static final String PAGE_PAGES_PARAM_NAME = "pages";

    public static final String PAGE_START_PARAM_NAME = "start";
    public static final String PAGE_LIMIT_PARAM_NAME = "limit";
    
    public static final String QUERY_SELECT_PARAM_NAME = "select";
    public static final String QUERY_WHERE_PARAM_NAME = "where";
    public static final String QUERY_LOGICAL_PARAM_NAME = "logical";

    public static final String SELECT_DISTINCT_PARAM_NAME = "distinct";
    
    protected NamingStrategy namingStrategy = new NamingStrategy() {};
    
    public static final String ORDER_BY_PARAM_NAME = "orderBy";
    
    private Map<Class<?>, GraphQLType> classCache = new HashMap<>();
    private Map<EntityType<?>, GraphQLObjectType> entityCache = new HashMap<>();
    private Map<EmbeddableType<?>, GraphQLObjectType> embeddableOutputCache = new HashMap<>();
    private Map<EmbeddableType<?>, GraphQLInputObjectType> embeddableInputCache = new HashMap<>();
    
    private static final Logger log = LoggerFactory.getLogger(GraphQLJpaSchemaBuilder.class);

    private EntityManager entityManager;
     
    private String name = "GraphQL JPA Schema";
    
    private String description = "GraphQL Schema for all entities in this JPA application";

    public GraphQLJpaSchemaBuilder(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /* (non-Javadoc)
     * @see org.activiti.services.query.qraphql.jpa.GraphQLSchemaBuilder#getGraphQLSchema()
     */
    @Override
    public GraphQLSchema build() {
        return GraphQLSchema.newSchema()
            .query(getQueryType())
            .build();
    }

    private GraphQLObjectType getQueryType() {
        GraphQLObjectType.Builder queryType = 
            GraphQLObjectType.newObject()
                .name(this.name)
                .description(this.description);

        queryType.fields(
            entityManager.getMetamodel()
                .getEntities().stream()
                .filter(this::isNotIgnored)
                .map(this::getQueryFieldByIdDefinition)
                .collect(Collectors.toList())
        );
        
        queryType.fields(
            entityManager.getMetamodel()
                .getEntities().stream()
                .filter(this::isNotIgnored)
                .map(this::getQueryFieldSelectDefinition)
                .collect(Collectors.toList())
        );

        return queryType.build();
    }

    private GraphQLFieldDefinition getQueryFieldByIdDefinition(EntityType<?> entityType) {
        return GraphQLFieldDefinition.newFieldDefinition()
                .name(entityType.getName())
                .description(getSchemaDescription( entityType.getJavaType()))
                .type(getObjectType(entityType))
                .dataFetcher(new GraphQLJpaSimpleDataFetcher(entityManager, entityType))
                .argument(entityType.getAttributes().stream()
                    .filter(this::isValidInput)
                    .filter(this::isNotIgnored)
                    .filter(this::isIdentity)
                    .map(this::getArgument)
                    .collect(Collectors.toList())
                )
                .build();
    }    

    private GraphQLFieldDefinition getQueryFieldSelectDefinition(EntityType<?> entityType) {
        
        GraphQLObjectType pageType = GraphQLObjectType.newObject()
                .name(namingStrategy.pluralize(entityType.getName()))
                .description("Query response wrapper object for " + entityType.getName() + ".  When page is requested, this object will be returned with query metadata.")
                .field(GraphQLFieldDefinition.newFieldDefinition()
                    .name(GraphQLJpaSchemaBuilder.PAGE_PAGES_PARAM_NAME)
                    .description("Total number of pages calculated on the database for this page size.")
                    .type(Scalars.GraphQLLong)
                    .build()
                )
                .field(GraphQLFieldDefinition.newFieldDefinition()
                    .name(GraphQLJpaSchemaBuilder.PAGE_TOTAL_PARAM_NAME)
                    .description("Total number of records in the database for this query.")
                    .type(Scalars.GraphQLLong)
                    .build()
                )
                .field(GraphQLFieldDefinition.newFieldDefinition()
                    .name(GraphQLJpaSchemaBuilder.QUERY_SELECT_PARAM_NAME)
                    .description("The queried records container")
                    .type(new GraphQLList(getObjectType(entityType)))
                    .build()
                )
                .build();
        
        return GraphQLFieldDefinition.newFieldDefinition()
                .name(namingStrategy.pluralize(entityType.getName()))
                .description("Query request wrapper for " + entityType.getName() + " to request paginated data. "
                    + "Use query request arguments to specify query filter criterias. "
                    + "Use the '"+QUERY_SELECT_PARAM_NAME+"' field to request actual fields. "
                    + "Use the '"+ORDER_BY_PARAM_NAME+"' on a field to specify sort order for each field. ")
                .type(pageType)
                .dataFetcher(new GraphQLJpaQueryDataFetcher(entityManager, entityType))
                .argument(paginationArgument)
                .argument(getWhereArgument(entityType))
                .build();
    }

    private Map<ManagedType<?>, GraphQLArgument> whereArgumentsMap = new HashMap<>();

    private GraphQLArgument getWhereArgument(ManagedType<?> managedType) {
    	String typeName="";
    	if (managedType instanceof EmbeddableType){
			typeName = managedType.getJavaType().getSimpleName()+"EmbeddableType";
		} else if (managedType instanceof EntityType) {
			typeName = ((EntityType)managedType).getName();
		}

		String type = namingStrategy.pluralize(typeName)+"CriteriaExpression";
        
        GraphQLArgument whereArgument = whereArgumentsMap.get(managedType);
        
        if(whereArgument != null)
            return whereArgument;
        
        GraphQLInputObjectType whereInputObject = GraphQLInputObjectType.newInputObject()
            .name(type)
            .description("Where logical AND specification of the provided list of criteria expressions")
            .field(GraphQLInputObjectField.newInputObjectField()
                .name("OR")
                .description("Logical operation for expressions")
                .type(new GraphQLTypeReference(type))
                .build()
            )
            .field(GraphQLInputObjectField.newInputObjectField()
                .name("AND")
                .description("Logical operation for expressions")
                .type(new GraphQLTypeReference(type))
                .build()
            )
            .fields(managedType.getAttributes().stream()
                .filter(this::isValidInput)
                .filter(this::isNotIgnored)
                .map(this::getWhereInputField)
                .collect(Collectors.toList())
            )
            .build();
        
        whereArgument = GraphQLArgument.newArgument()
            .name(QUERY_WHERE_PARAM_NAME)
            .description("Where logical specification")
            .type(whereInputObject)
            .build();
        
        whereArgumentsMap.put(managedType, whereArgument);
        
        return whereArgument;
        
    }
    
    private GraphQLInputObjectField getWhereInputField(Attribute<?,?> attribute) {
        GraphQLType type = getWhereAttributeType(attribute);
        String description = getSchemaDescription(attribute.getJavaMember());

        if (type instanceof GraphQLInputType) {
            return GraphQLInputObjectField.newInputObjectField()
                .name(attribute.getName())
                .description(description)
                .type((GraphQLInputType) type)
                .build(); 
        }

        throw new IllegalArgumentException("Attribute " + attribute.getName() + " cannot be mapped as an Input Argument");
    }

    private Map<String, GraphQLInputType> whereAttributesMap = new HashMap<>();
    
    private GraphQLInputType getWhereAttributeType(Attribute<?,?> attribute) {
        //String type =  namingStrategy.singularize(attribute.getName())+((EntityType<?>)attribute.getDeclaringType()).getName()+"Criteria";
    	String type =  namingStrategy.singularize(attribute.getName())+attribute.getDeclaringType().getJavaType().getSimpleName()+"Criteria";

    	if(whereAttributesMap.containsKey(type))
           return whereAttributesMap.get(type);
       
    	GraphQLInputObjectType.Builder builder = GraphQLInputObjectType.newInputObject()
            .name(type)
            .description("Criteria expression specification of "+namingStrategy.singularize(attribute.getName())+" attribute in entity " + attribute.getDeclaringType().getJavaType())
            .field(GraphQLInputObjectField.newInputObjectField()
                .name("OR")
                .description("Logical OR criteria expression")
                .type(new GraphQLTypeReference(type))
                .build()
            )
            .field(GraphQLInputObjectField.newInputObjectField()
                .name("AND")
                .description("Logical AND criteria expression")
                .type(new GraphQLTypeReference(type))
                .build()
            )
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(Criteria.EQ.name())
                .description("Equals criteria")
                .type(getAttributeInputType(attribute))
                .build()
            )
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(Criteria.NE.name())
                .description("Not Equals criteria")
                .type(getAttributeInputType(attribute))
                .build()
            );
       
            if(!attribute.getJavaType().isEnum()) {
                if(!attribute.getJavaType().equals(String.class)) {
                    builder.field(GraphQLInputObjectField.newInputObjectField()
                        .name(Criteria.LE.name())
                        .description("Less then or Equals criteria")
                        .type(getAttributeInputType(attribute))
                        .build()
                    )
                    .field(GraphQLInputObjectField.newInputObjectField()
                        .name(Criteria.GE.name())
                        .description("Greater or Equals criteria")
                        .type(getAttributeInputType(attribute))
                        .build()
                    )
                    .field(GraphQLInputObjectField.newInputObjectField()
                        .name(Criteria.GT.name())
                        .description("Greater Then criteria")
                        .type(getAttributeInputType(attribute))
                        .build()
                    )
                    .field(GraphQLInputObjectField.newInputObjectField()
                        .name(Criteria.LT.name())
                        .description("Less Then criteria")
                        .type(getAttributeInputType(attribute))
                        .build()
                    );
                }
           
                if(attribute.getJavaType().equals(String.class)) {
                    builder.field(GraphQLInputObjectField.newInputObjectField()
                        .name(Criteria.LIKE.name())
                        .description("Like criteria")
                        .type(getAttributeInputType(attribute))
                        .build()
                    )
                    .field(GraphQLInputObjectField.newInputObjectField()
                        .name(Criteria.CASE.name())
                        .description("Case sensitive match criteria")
                        .type(getAttributeInputType(attribute))
                        .build()
                    )
                    .field(GraphQLInputObjectField.newInputObjectField()
                        .name(Criteria.STARTS.name())
                        .description("Starts with criteria")
                        .type(getAttributeInputType(attribute))
                        .build()
                    )
                    .field(GraphQLInputObjectField.newInputObjectField()
                        .name(Criteria.ENDS.name())
                        .description("Ends with criteria")
                        .type(getAttributeInputType(attribute))
                        .build()
                    );
                }
            }
            
            builder.field(GraphQLInputObjectField.newInputObjectField()
                .name(Criteria.IS_NULL.name())
                .description("Is Null criteria")
                .type(Scalars.GraphQLBoolean)
                .build()
            )
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(Criteria.NOT_NULL.name())
                .description("Is Not Null criteria")
                .type(Scalars.GraphQLBoolean)
                .build()
            )
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(Criteria.IN.name())
                .description("In criteria")
                .type(new GraphQLList(getAttributeInputType(attribute)))
                .build()
            )
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(Criteria.NIN.name())
                .description("Not In criteria")
                .type(new GraphQLList(getAttributeInputType(attribute)))
                .build()
            )
           .field(GraphQLInputObjectField.newInputObjectField()
                .name(Criteria.BETWEEN.name())
                .description("Between criteria")
                .type(new GraphQLList(getAttributeInputType(attribute)))
                .build()
           )
           .field(GraphQLInputObjectField.newInputObjectField()
                .name(Criteria.NOT_BETWEEN.name())
                .description("Not Between criteria")
                .type(new GraphQLList(getAttributeInputType(attribute)))
                .build()
           );

       GraphQLInputType answer = builder.build();
       
       whereAttributesMap.putIfAbsent(type, answer);
       
       return answer;
       
    }
    
    private GraphQLArgument getArgument(Attribute<?,?> attribute) {
        GraphQLInputType type = getAttributeInputType(attribute);
        String description = getSchemaDescription(attribute.getJavaMember());

        return GraphQLArgument.newArgument()
                .name(attribute.getName())
                .type((GraphQLInputType) type)
                .description(description)
                .build();
    }
    
    private GraphQLType getEmbeddableType(EmbeddableType<?> embeddableType, boolean input) {
        if (input && embeddableInputCache.containsKey(embeddableType))
            return embeddableInputCache.get(embeddableType);

        if (!input && embeddableOutputCache.containsKey(embeddableType))
            return embeddableOutputCache.get(embeddableType);
        String embeddableTypeName = namingStrategy.singularize(embeddableType.getJavaType().getSimpleName())+ (input ? "Input" : "") +"EmbeddableType";
        GraphQLType graphQLType=null;
        if (input) {
            graphQLType = GraphQLInputObjectType.newInputObject()
                    .name(embeddableTypeName)
                    .description(getSchemaDescription(embeddableType.getJavaType()))
                    .fields(embeddableType.getAttributes().stream()
                            .filter(this::isNotIgnored)
                            .map(this::getInputObjectField)
                            .collect(Collectors.toList())
                    )
                    .build();
        } else {
            graphQLType = GraphQLObjectType.newObject()
                    .name(embeddableTypeName)
                    .description(getSchemaDescription(embeddableType.getJavaType()))
                    .fields(embeddableType.getAttributes().stream()
                            .filter(this::isNotIgnored)
                            .map(this::getObjectField)
                            .collect(Collectors.toList())
                    )
                    .build();
        }
        if (input) {
            embeddableInputCache.putIfAbsent(embeddableType, (GraphQLInputObjectType) graphQLType);
        } else{
            embeddableOutputCache.putIfAbsent(embeddableType, (GraphQLObjectType) graphQLType);
        }
        
        return graphQLType;
    }
    

    private GraphQLObjectType getObjectType(EntityType<?> entityType) {
        if (entityCache.containsKey(entityType))
            return entityCache.get(entityType);        
        
        
        GraphQLObjectType objectType = GraphQLObjectType.newObject()
                .name(entityType.getName())
                .description(getSchemaDescription( entityType.getJavaType()))
                .fields(entityType.getAttributes().stream()
                    .filter(this::isNotIgnored)
                    .map(this::getObjectField)
                    .collect(Collectors.toList())
                )
                .fields(getObjectCalcFields(entityType.getJavaType()))
                .fields(getObjectCalcMethods(entityType.getJavaType()))
                .build();
        
        entityCache.putIfAbsent(entityType, objectType);
        
        return objectType;
    }

    private List<GraphQLFieldDefinition> getObjectCalcFields(Class cls) {
        return
            Arrays.stream(cls.getDeclaredFields())
                .filter(
                        f ->
                                f instanceof Member &&
                                f.isAnnotationPresent(Transient.class) &&
                                isNotIgnored((Member) f) &&
                                isNotIgnored(f.getType())
                )
                .map(f -> getObjectCalcField(f))
                .collect(Collectors.toList());
    }

    private List<GraphQLFieldDefinition> getObjectCalcMethods(Class cls) {
        return
            Arrays.stream(cls.getDeclaredMethods())
                .filter(
                        m ->
                                m instanceof Member &&
                                m.isAnnotationPresent(Transient.class) &&
                                isNotIgnored((Member) m) &&
                                isNotIgnored(m.getReturnType())
                )
                .map(m -> getObjectCalcMethtod(m))
                .collect(Collectors.toList());
    }

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    private GraphQLFieldDefinition getObjectCalcField(Field field) {
        GraphQLType type = getGraphQLTypeFromJavaType(field.getType());
        DataFetcher dataFetcher = PropertyDataFetcher.fetching(field.getName());

        return GraphQLFieldDefinition.newFieldDefinition()
                .name(field.getName())
                .description(getSchemaDescription((AnnotatedElement) field))
                .type((GraphQLOutputType) type)
                .dataFetcher(dataFetcher)
                .build();
    }

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    private GraphQLFieldDefinition getObjectCalcMethtod(Method method) {
        String nm = method.getName();
        if (nm.startsWith("is")) {
            nm = Introspector.decapitalize(nm.substring(2));
        }
        if (nm.startsWith("get")) {
            nm = Introspector.decapitalize(nm.substring(3));
        }

        GraphQLType type = getGraphQLTypeFromJavaType(method.getReturnType());
        DataFetcher dataFetcher = PropertyDataFetcher.fetching(nm);

        return GraphQLFieldDefinition.newFieldDefinition()
                .name(nm)
                .description(getSchemaDescription((AnnotatedElement) method))
                .type((GraphQLOutputType) type)
                .dataFetcher(dataFetcher)
                .build();
    }

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    private GraphQLFieldDefinition getObjectField(Attribute attribute) {
        GraphQLOutputType type = getAttributeOutputType(attribute);

        List<GraphQLArgument> arguments = new ArrayList<>();
        DataFetcher dataFetcher = PropertyDataFetcher.fetching(attribute.getName());

        // Only add the orderBy argument for basic attribute types
        if (attribute instanceof SingularAttribute
            && attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.BASIC) {
            arguments.add(GraphQLArgument.newArgument()
                .name(ORDER_BY_PARAM_NAME)
                .description("Specifies field sort direction in the query results.")
                .type(orderByDirectionEnum)
                .build()
            );
        }

        // Get the fields that can be queried on (i.e. Simple Types, no Sub-Objects)
        if (attribute instanceof SingularAttribute
            && attribute.getPersistentAttributeType() != Attribute.PersistentAttributeType.BASIC) {
            ManagedType foreignType = (ManagedType) ((SingularAttribute) attribute).getType();

            // TODO fix page count query
            arguments.add(getWhereArgument(foreignType));

        } //  Get Sub-Objects fields queries via DataFetcher
        else if (attribute instanceof PluralAttribute
            && (attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.ONE_TO_MANY
                || attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.MANY_TO_MANY)) {
            EntityType declaringType = (EntityType) ((PluralAttribute) attribute).getDeclaringType();
            EntityType elementType =  (EntityType) ((PluralAttribute) attribute).getElementType();

            arguments.add(getWhereArgument(elementType));
            dataFetcher = new GraphQLJpaOneToManyDataFetcher(entityManager, declaringType, (PluralAttribute) attribute);
        }

        return GraphQLFieldDefinition.newFieldDefinition()
                .name(attribute.getName())
                .description(getSchemaDescription(attribute.getJavaMember()))
                .type(type)
                .dataFetcher(dataFetcher)
                .argument(arguments)
                .build();
    }

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    private GraphQLInputObjectField getInputObjectField(Attribute attribute) {
        GraphQLInputType type = getAttributeInputType(attribute);

        return GraphQLInputObjectField.newInputObjectField()
                .name(attribute.getName())
                .description(getSchemaDescription(attribute.getJavaMember()))
                .type(type)
                .build();
    }

    private Stream<Attribute<?,?>> findBasicAttributes(Collection<Attribute<?,?>> attributes) {
        return attributes.stream().filter(it -> it.getPersistentAttributeType() == Attribute.PersistentAttributeType.BASIC);
    }

    @SuppressWarnings( "rawtypes" )
    private GraphQLInputType getAttributeInputType(Attribute<?,?> attribute) {
        try{
            return (GraphQLInputType) getAttributeType(attribute, true);
        } catch (ClassCastException e){
            throw new IllegalArgumentException("Attribute " + attribute + " cannot be mapped as an Input Argument");
        }
    }

    @SuppressWarnings( "rawtypes" )
    private GraphQLOutputType getAttributeOutputType(Attribute<?,?> attribute) {
        try {
            return (GraphQLOutputType) getAttributeType(attribute, false);
        } catch (ClassCastException e){
            throw new IllegalArgumentException("Attribute " + attribute + " cannot be mapped as an Output Argument");
        }
    }

    @SuppressWarnings( "rawtypes" )
    private GraphQLType getAttributeType(Attribute<?,?> attribute, boolean input) {

        if (isBasic(attribute)) {
        	return getGraphQLTypeFromJavaType(attribute.getJavaType());
        } 
        else if (isEmbeddable(attribute)) {
        	EmbeddableType embeddableType = (EmbeddableType) ((SingularAttribute) attribute).getType();
        	return getEmbeddableType(embeddableType, input);
        } 
        else if (isToMany(attribute)) {
            EntityType foreignType = (EntityType) ((PluralAttribute) attribute).getElementType();
            return new GraphQLList(new GraphQLTypeReference(foreignType.getName()));
        } 
        else if (isToOne(attribute)) {
            EntityType foreignType = (EntityType) ((SingularAttribute) attribute).getType();
            return new GraphQLTypeReference(foreignType.getName());
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

    protected final boolean isEmbeddable(Attribute<?,?> attribute) {
    	return attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.EMBEDDED;
    }
    
    protected final boolean isBasic(Attribute<?,?> attribute) {
    	return attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.BASIC;
    }
    
    protected final boolean isElementCollection(Attribute<?,?> attribute) {
    	return  attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.ELEMENT_COLLECTION;
    }
    
    protected final boolean isToMany(Attribute<?,?> attribute) {
    	return attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.ONE_TO_MANY
        		|| attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.MANY_TO_MANY;
    }

    protected final boolean isToOne(Attribute<?,?> attribute) {
    	return attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.MANY_TO_ONE
        		|| attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.ONE_TO_ONE;
    }
    

    protected final boolean isValidInput(Attribute<?,?> attribute) {
        return attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.BASIC ||
                attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.ELEMENT_COLLECTION ||
                attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.EMBEDDED;
    }

    private String getSchemaDescription(Member member) {
        if (member instanceof AnnotatedElement) {
            String desc = getSchemaDescription((AnnotatedElement) member);
            if (desc != null) {
                return(desc);
            }
        }

        //The given Member has no @GraphQLDescription set.
        //If the Member is a Method it might be a getter/setter, see if the property it represents
        //is annotated with @GraphQLDescription
        //Alternatively if the Member is a Field its getter might be annotated, see if its getter
        //is annotated with @GraphQLDescription
        if (member instanceof Method) {
            Field fieldMember = getFieldByAccessor((Method)member);
            if (fieldMember != null) {
                return(getSchemaDescription((AnnotatedElement) fieldMember));
            }
        } else if (member instanceof Field) {
            Method fieldGetter = getGetterOfField((Field)member);
            if (fieldGetter != null) {
                return(getSchemaDescription((AnnotatedElement) fieldGetter));
            }
        }

        return null;
    }

    private Method getGetterOfField(Field field) {
        try {
            Class<?> clazz = field.getDeclaringClass();
            BeanInfo info = Introspector.getBeanInfo(clazz);
            PropertyDescriptor[] props = info.getPropertyDescriptors();
            for (PropertyDescriptor pd : props) { 
                if (pd.getName().equals(field.getName())) {
                    return(pd.getReadMethod());
                }
            }
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }

        return(null);
    }

    //from https://stackoverflow.com/questions/13192734/getting-a-property-field-name-using-getter-method-of-a-pojo-java-bean/13514566
    private static Field getFieldByAccessor(Method method) {
        try {
            Class<?> clazz = method.getDeclaringClass();
            BeanInfo info = Introspector.getBeanInfo(clazz);  
            PropertyDescriptor[] props = info.getPropertyDescriptors();  
            for (PropertyDescriptor pd : props) {  
                if(method.equals(pd.getWriteMethod()) || method.equals(pd.getReadMethod())) {
                    String fieldName = pd.getName();
                    try {
                        return(clazz.getDeclaredField(fieldName));
                    } catch (Throwable t) {
                        log.error("class '" + clazz.getName() + "' contains method '" + method.getName() + "' which is an accessor for a Field named '" + fieldName + "', error getting the field:", t);
                        return(null);
                    }
                }
            }
        } catch (Throwable t)  {
            log.error("error finding Field for accessor with name '" + method.getName() + "'", t);
        }

        return null;
    }

    private String getSchemaDescription(AnnotatedElement annotatedElement) {
        if (annotatedElement != null) {
            GraphQLDescription schemaDocumentation = annotatedElement.getAnnotation(GraphQLDescription.class);
            return schemaDocumentation != null ? schemaDocumentation.value() : null;
        }

        return null;
    }

    private boolean isNotIgnored(EmbeddableType<?> attribute) {
        return isNotIgnored(attribute.getJavaType());
    }
    
    private boolean isNotIgnored(Attribute<?,?> attribute) {
        return isNotIgnored(attribute.getJavaMember()) && isNotIgnored(attribute.getJavaType());
    }

    private boolean isIdentity(Attribute<?,?> attribute) {
        return attribute instanceof SingularAttribute && ((SingularAttribute<?,?>)attribute).isId();
    }
    
    private boolean isNotIgnored(EntityType<?> entityType) {
        return isNotIgnored(entityType.getJavaType());
    }

    private boolean isNotIgnored(Member member) {
        return member instanceof AnnotatedElement && isNotIgnored((AnnotatedElement) member);
    }

    private boolean isNotIgnored(AnnotatedElement annotatedElement) {
        if (annotatedElement != null) {
            GraphQLIgnore schemaDocumentation = annotatedElement.getAnnotation(GraphQLIgnore.class);
            return schemaDocumentation == null;
        }

        return false;
    }
    
    @SuppressWarnings( "unchecked" )
    private GraphQLType getGraphQLTypeFromJavaType(Class<?> clazz) {
        if (clazz.isEnum()) {
            
            if (classCache.containsKey(clazz))
                return classCache.get(clazz);
            
            GraphQLEnumType.Builder enumBuilder = GraphQLEnumType.newEnum().name(clazz.getSimpleName());
            int ordinal = 0;
            for (Enum<?> enumValue : ((Class<Enum<?>>)clazz).getEnumConstants())
                enumBuilder.value(enumValue.name(), ordinal++);

            GraphQLType enumType = enumBuilder.build();
            setNoOpCoercing(enumType);

            classCache.putIfAbsent(clazz, enumType);
            
            return enumType;
        }

        return JavaScalars.of(clazz);
    }

    protected GraphQLInputType getFieldsEnumType(EntityType<?> entityType) {
            
        GraphQLEnumType.Builder enumBuilder = GraphQLEnumType.newEnum().name(entityType.getName()+"FieldsEnum");
        final AtomicInteger ordinal = new AtomicInteger();
        
        entityType.getAttributes().stream()
            .filter(this::isValidInput)
            .filter(this::isNotIgnored)
            .forEach(it -> enumBuilder.value(it.getName(), ordinal.incrementAndGet()));
        
        GraphQLInputType answer = enumBuilder.build();
        setNoOpCoercing(answer);

        return answer;
    }
    
    /**
     * JPA will deserialize Enum's for us...we don't want GraphQL doing it.
     *
     * @param type
     */
    private void setNoOpCoercing(GraphQLType type) {
        try {
            Field coercing = type.getClass().getDeclaredField("coercing");
            coercing.setAccessible(true);
            coercing.set(type, new NoOpCoercing());
        } catch (Exception e) {
            log.error("Unable to set coercing for " + type, e);
        }
    }

    private static final GraphQLArgument paginationArgument =
            GraphQLArgument.newArgument()
                    .name(PAGE_PARAM_NAME)
                    .description("Page object for pageble requests, specifying the requested start page and limit size.")
                    .type(GraphQLInputObjectType.newInputObject()
                        .name("Page")
                        .description("Page fields for pageble requests.")
                        .field(GraphQLInputObjectField.newInputObjectField()
                            .name(PAGE_START_PARAM_NAME)
                            .description("Start page that should be returned. Page numbers start with 1 (1-indexed)")
                            .type(Scalars.GraphQLInt).build()
                        )
                        .field(GraphQLInputObjectField.newInputObjectField()
                            .name(PAGE_LIMIT_PARAM_NAME).description("Limit how many results should this page contain")
                            .type(Scalars.GraphQLInt)
                            .build()
                        )
                        .build()
                    ).build();

    private static final GraphQLEnumType orderByDirectionEnum =
            GraphQLEnumType.newEnum()
                    .name("OrderBy")
                    .description("Specifies the direction (Ascending / Descending) to sort a field.")
                    .value("ASC", 0, "Ascending")
                    .value("DESC", 1, "Descending")
                    .build();

    
    /**
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @param name the name to set
     */
    @Override
    public GraphQLJpaSchemaBuilder name(String name) {
        this.name = name;
        
        return this;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @param description the description to set
     */
    @Override
    public GraphQLJpaSchemaBuilder description(String description) {
        this.description = description;

        return this;
    }

    /**
     * @param namingStrategy the namingStrategy to set
     */
    public void setNamingStrategy(NamingStrategy namingStrategy) {
        this.namingStrategy = namingStrategy;
    }

    
    static class NoOpCoercing implements Coercing<Object, Object> {

        @Override
        public Object serialize(Object input) {
            return input;
        }

        @Override
        public Object parseValue(Object input) {
            return input;
        }

        @Override
        public Object parseLiteral(Object input) {
            return input;
        }

    }


    @Override
    public GraphQLSchemaBuilder entityPath(String path) {
        Assert.assertNotNull(path, "path is null");
        return this;
    }

    @Override
    public GraphQLSchemaBuilder namingStrategy(NamingStrategy instance) {
        Assert.assertNotNull(instance, "instance is null");
        
        this.namingStrategy = instance;
        
        return this;
    }
    
}
