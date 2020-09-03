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

import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import static graphql.schema.GraphQLInputObjectType.newInputObject;

import java.beans.Introspector;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.Convert;
import javax.persistence.EntityManager;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

import org.dataloader.MappedBatchLoaderWithContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.introproventures.graphql.jpa.query.annotation.GraphQLIgnore;
import com.introproventures.graphql.jpa.query.annotation.GraphQLIgnoreFilter;
import com.introproventures.graphql.jpa.query.annotation.GraphQLIgnoreOrder;
import com.introproventures.graphql.jpa.query.schema.GraphQLSchemaBuilder;
import com.introproventures.graphql.jpa.query.schema.JavaScalars;
import com.introproventures.graphql.jpa.query.schema.NamingStrategy;
import com.introproventures.graphql.jpa.query.schema.RestrictedKeysProvider;
import com.introproventures.graphql.jpa.query.schema.impl.EntityIntrospector.EntityIntrospectionResult.AttributePropertyDescriptor;
import com.introproventures.graphql.jpa.query.schema.impl.PredicateFilter.Criteria;
import com.introproventures.graphql.jpa.query.schema.relay.GraphQLJpaRelayDataFetcher;

import graphql.Assert;
import graphql.Directives;
import graphql.Scalars;
import graphql.relay.Relay;
import graphql.schema.Coercing;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputObjectType.Builder;
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

    private static final String NODE = "node";
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

    private Map<Class<?>, GraphQLOutputType> classCache = new HashMap<>();
    private Map<EntityType<?>, GraphQLObjectType> entityCache = new HashMap<>();
    private Map<ManagedType<?>, GraphQLInputObjectType> inputObjectCache = new HashMap<>();
    private Map<ManagedType<?>, GraphQLInputObjectType> subqueryInputObjectCache = new HashMap<>();
    private Map<Class<?>, GraphQLObjectType> embeddableOutputCache = new HashMap<>();
    private Map<Class<?>, GraphQLInputObjectType> embeddableInputCache = new HashMap<>();

    private static final Logger log = LoggerFactory.getLogger(GraphQLJpaSchemaBuilder.class);

    private EntityManager entityManager;

    private String name = "GraphQLJPA";

    private String description = "GraphQL Schema for all entities in this JPA application";

    private boolean isUseDistinctParameter = false;
    private boolean isDefaultDistinct = true;
    private boolean toManyDefaultOptional = true; // the many end is a collection, and it is always optional by default (empty collection)
    private boolean enableSubscription = false; // experimental
    private boolean enableDeferDirective = false; // experimental
    private boolean enableRelay = false; // experimental
    private int defaultMaxResults = 100;
    private int defaultFetchSize = 100;
    private int defaultPageLimitSize = 100;
    private boolean enableDefaultMaxResults = true;
    
    private RestrictedKeysProvider restrictedKeysProvider = (entityDescriptor) -> Optional.of(Collections.emptyList());

    private final Relay relay = new Relay();

    private final List<String> entityPaths = new ArrayList<>();

    private Supplier<BatchLoaderRegistry> batchLoadersRegistry = () -> {
        return BatchLoaderRegistry.getInstance();
    };

    public GraphQLJpaSchemaBuilder(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /* (non-Javadoc)
     * @see org.activiti.services.query.qraphql.jpa.GraphQLSchemaBuilder#getGraphQLSchema()
     */
    @Override
    public GraphQLSchema build() {
        GraphQLSchema.Builder schema = GraphQLSchema.newSchema()
                                                    .query(getQueryType());

        if(enableSubscription) {
            schema.subscription(getSubscriptionType());
        }

        if(enableDeferDirective) {
            schema.additionalDirective(Directives.DeferDirective);
        }

        if(enableRelay) {
            schema.additionalType(Relay.pageInfoType);
        }

        return schema.build();
    }

    private GraphQLObjectType getQueryType() {
        GraphQLObjectType.Builder queryType =
            GraphQLObjectType.newObject()
                .name(this.name  + "Query")
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

    private GraphQLObjectType getSubscriptionType() {
        GraphQLObjectType.Builder queryType =
            GraphQLObjectType.newObject()
                .name(this.name + "Subscription")
                .description(this.description);

        queryType.fields(
            entityManager.getMetamodel()
                .getEntities().stream()
                .filter(this::isNotIgnored)
                .map(this::getQueryFieldStreamDefinition)
                .collect(Collectors.toList())
        );

        return queryType.build();
    }

    private GraphQLFieldDefinition getQueryFieldByIdDefinition(EntityType<?> entityType) {
        GraphQLObjectType entityObjectType = getObjectType(entityType);

        GraphQLJpaQueryFactory queryFactory = GraphQLJpaQueryFactory.builder()
                                                                    .withEntityManager(entityManager)
                                                                    .withEntityType(entityType)
                                                                    .withEntityObjectType(entityObjectType)
                                                                    .withSelectNodeName(entityObjectType.getName())
                                                                    .withToManyDefaultOptional(toManyDefaultOptional)
                                                                    .withRestrictedKeysProvider(restrictedKeysProvider)
                                                                    .build();

        DataFetcher<Object> dataFetcher = GraphQLJpaSimpleDataFetcher.builder()
                                                                     .withQueryFactory(queryFactory)
                                                                     .build();
        String fieldName = entityType.getName();

        return GraphQLFieldDefinition.newFieldDefinition()
                .name(enableRelay ? Introspector.decapitalize(fieldName) : fieldName)
                .description(getSchemaDescription(entityType))
                .type(entityObjectType)
                .dataFetcher(dataFetcher)
                .arguments(entityType.getAttributes().stream()
                    .filter(this::isValidInput)
                    .filter(this::isNotIgnored)
                    .filter(this::isIdentity)
                    .map(this::getArgument)
                    .collect(Collectors.toList())
                )
                .build();
    }

    private GraphQLObjectType getConnectionType(GraphQLObjectType nodeType) {
        GraphQLObjectType edgeType = relay.edgeType(nodeType.getName(), nodeType, null, Collections.emptyList());

        return relay.connectionType(nodeType.getName(), edgeType, Collections.emptyList());
    }

    private GraphQLFieldDefinition getQueryFieldSelectDefinition(EntityType<?> entityType) {
        final GraphQLObjectType entityObjectType = getObjectType(entityType);
        final GraphQLObjectType outputType = enableRelay ? getConnectionType(entityObjectType) : getSelectType(entityType);

        final DataFetcher<? extends Object> dataFetcher;

        GraphQLJpaQueryFactory queryFactory = GraphQLJpaQueryFactory.builder()
                                                                    .withEntityManager(entityManager)
                                                                    .withEntityType(entityType)
                                                                    .withEntityObjectType(entityObjectType)
                                                                    .withSelectNodeName(enableRelay ? NODE : QUERY_SELECT_PARAM_NAME)
                                                                    .withToManyDefaultOptional(toManyDefaultOptional)
                                                                    .withDefaultDistinct(isDefaultDistinct)
                                                                    .withDefaultFetchSize(defaultFetchSize)
                                                                    .withRestrictedKeysProvider(restrictedKeysProvider)
                                                                    .build();

        if(enableRelay) {
            dataFetcher = GraphQLJpaRelayDataFetcher.builder()
                                                    .withQueryFactory(queryFactory)
                                                    .withDefaultMaxResults(defaultMaxResults)
                                                    .withEnableDefaultMaxResults(enableDefaultMaxResults)
                                                    .withDefaultFirstSize(defaultPageLimitSize)
                                                    .build();
        } else {
            dataFetcher = GraphQLJpaQueryDataFetcher.builder()
                                                    .withQueryFactory(queryFactory)
                                                    .withDefaultMaxResults(defaultMaxResults)
                                                    .withEnableDefaultMaxResults(enableDefaultMaxResults)
                                                    .withDefaultPageLimitSize(defaultPageLimitSize)
                                                    .build();
        }

        String fieldName = namingStrategy.pluralize(entityType.getName());

        GraphQLFieldDefinition.Builder fieldDefinition = GraphQLFieldDefinition.newFieldDefinition()
                .name(enableRelay ? Introspector.decapitalize(fieldName) : fieldName)
                .description("Query request wrapper for " + entityType.getName() + " to request paginated data. "
                    + "Use query request arguments to specify query filter criterias. "
                    + "Use the '"+QUERY_SELECT_PARAM_NAME+"' field to request actual fields. "
                    + "Use the '"+ORDER_BY_PARAM_NAME+"' on a field to specify sort order for each field. ")
                .type(outputType)
                .dataFetcher(dataFetcher)
                .argument(getWhereArgument(entityType))
                .arguments(enableRelay ? relay.getForwardPaginationConnectionFieldArguments() : Collections.singletonList(paginationArgument));

        if (isUseDistinctParameter) {
                fieldDefinition.argument(distinctArgument(entityType));
        }

        return fieldDefinition.build();
    }

    private GraphQLObjectType getSelectType(EntityType<?> entityType) {
        GraphQLObjectType selectObjectType = getObjectType(entityType);

        GraphQLObjectType selectPagedResultType = GraphQLObjectType.newObject()
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
                    .type(new GraphQLList(selectObjectType))
                    .build()
                )
                .build();

        return selectPagedResultType;
    }

    private GraphQLFieldDefinition getQueryFieldStreamDefinition(EntityType<?> entityType) {
        GraphQLObjectType entityObjectType = getObjectType(entityType);

        GraphQLJpaQueryFactory queryFactory = GraphQLJpaQueryFactory.builder()
                                                                    .withEntityManager(entityManager)
                                                                    .withEntityType(entityType)
                                                                    .withEntityObjectType(entityObjectType)
                                                                    .withSelectNodeName(SELECT_DISTINCT_PARAM_NAME)
                                                                    .withToManyDefaultOptional(toManyDefaultOptional)
                                                                    .withDefaultDistinct(isDefaultDistinct)
                                                                    .withRestrictedKeysProvider(restrictedKeysProvider)
                                                                    .build();

        DataFetcher<Object> dataFetcher = GraphQLJpaStreamDataFetcher.builder()
                                                                     .withQueryFactory(queryFactory)
                                                                     .build();

        GraphQLFieldDefinition.Builder fieldDefinition =  GraphQLFieldDefinition.newFieldDefinition()
                .name(namingStrategy.pluralize(entityType.getName()))
                .description("Query request wrapper for " + entityType.getName() + " to request paginated data. "
                    + "Use query request arguments to specify query filter criterias. "
                    + "Use the '"+ORDER_BY_PARAM_NAME+"' on a field to specify sort order for each field. ")
                .type(entityObjectType)
                .dataFetcher(dataFetcher)
                .argument(paginationArgument)
                .argument(getWhereArgument(entityType));

        if (isUseDistinctParameter) {
                fieldDefinition.argument(distinctArgument(entityType));
        }

        return fieldDefinition.build();
    }

    private Map<Class<?>, GraphQLArgument> whereArgumentsMap = new HashMap<>();

    private GraphQLArgument distinctArgument(EntityType<?> entityType) {
        return GraphQLArgument.newArgument()
                .name(SELECT_DISTINCT_PARAM_NAME)
                .description("Distinct logical specification")
                .type(Scalars.GraphQLBoolean)
                .defaultValue(isDefaultDistinct)
                .build();
    }

    private GraphQLArgument getWhereArgument(ManagedType<?> managedType) {
        return whereArgumentsMap.computeIfAbsent(managedType.getJavaType(), (javaType) -> computeWhereArgument(managedType));
    }

    private GraphQLArgument computeWhereArgument(ManagedType<?> managedType) {
    	String type=resolveWhereArgumentTypeName(managedType);

        GraphQLInputObjectType whereInputObject = GraphQLInputObjectType.newInputObject()
            .name(type)
            .description("Where logical AND specification of the provided list of criteria expressions")
            .field(GraphQLInputObjectField.newInputObjectField()
                                          .name(Logical.OR.name())
                                          .description("Logical operation for expressions")
                                          .type(new GraphQLList(new GraphQLTypeReference(type)))
                                          .build()
            )
            .field(GraphQLInputObjectField.newInputObjectField()
                                          .name(Logical.AND.name())
                                          .description("Logical operation for expressions")
                                          .type(new GraphQLList(new GraphQLTypeReference(type)))
                                          .build()
            )
            .field(GraphQLInputObjectField.newInputObjectField()
                                          .name(Logical.EXISTS.name())
                                          .description("Logical EXISTS subquery expression")
                                          .type(new GraphQLList(getSubqueryInputType(managedType)))
                                          .build()
            )
            .field(GraphQLInputObjectField.newInputObjectField()
                                          .name(Logical.NOT_EXISTS.name())
                                          .description("Logical NOT EXISTS subquery expression")
                                          .type(new GraphQLList(getSubqueryInputType(managedType)))
                                          .build()
            )
            .fields(managedType.getAttributes().stream()
                                               .filter(this::isValidInput)
                                               .filter(this::isNotIgnored)
                                               .filter(this::isNotIgnoredFilter)
                                               .map(this::getWhereInputField)
                                               .collect(Collectors.toList())
            )
            .fields(managedType.getAttributes().stream()
                                               .filter(Attribute::isAssociation)
                                               .filter(this::isNotIgnored)
                                               .filter(this::isNotIgnoredFilter)
                                               .map(this::getInputObjectField)
                                               .collect(Collectors.toList())
            )
            // TODO support embedded element collections
//            .fields(managedType.getAttributes().stream()
//                                               .filter(Attribute::isCollection)
//                                               .filter(this::isNotIgnored)
//                                               .filter(this::isNotIgnoredFilter)
//                                               .map(this::getInputObjectField)
//                                               .collect(Collectors.toList())
//            )
            .build();

        return GraphQLArgument.newArgument()
                              .name(QUERY_WHERE_PARAM_NAME)
                              .description("Where logical specification")
                              .type(whereInputObject)
                              .build();

    }

    private String resolveWhereArgumentTypeName(ManagedType<?> managedType) {
        String typeName=resolveTypeName(managedType);

        return namingStrategy.pluralize(typeName)+"CriteriaExpression";
    }

    private String resolveSubqueryArgumentTypeName(ManagedType<?> managedType) {
        String typeName=resolveTypeName(managedType);

        return namingStrategy.pluralize(typeName)+"SubqueryCriteriaExpression";
    }

    private GraphQLInputObjectType getSubqueryInputType(ManagedType<?> managedType) {
        return subqueryInputObjectCache.computeIfAbsent(managedType, this::computeSubqueryInputType);
    }

    private GraphQLInputObjectType computeSubqueryInputType(ManagedType<?> managedType) {
        String type=resolveSubqueryArgumentTypeName(managedType);

         Builder whereInputObject = GraphQLInputObjectType.newInputObject()
            .name(type)
            .description("Where logical AND specification of the provided list of criteria expressions")
            .field(GraphQLInputObjectField.newInputObjectField()
                                          .name(Logical.OR.name())
                                          .description("Logical operation for expressions")
                                          .type(new GraphQLList(new GraphQLTypeReference(type)))
                                          .build()
            )
            .field(GraphQLInputObjectField.newInputObjectField()
                                          .name(Logical.AND.name())
                                          .description("Logical operation for expressions")
                                          .type(new GraphQLList(new GraphQLTypeReference(type)))
                                          .build()
            )
            .field(GraphQLInputObjectField.newInputObjectField()
                                          .name(Logical.EXISTS.name())
                                          .description("Logical EXISTS subquery expression")
                                          .type(new GraphQLList(new GraphQLTypeReference(type)))
                                          .build()
            )
            .field(GraphQLInputObjectField.newInputObjectField()
                                          .name(Logical.NOT_EXISTS.name())
                                          .description("Logical NOT EXISTS subquery expression")
                                          .type(new GraphQLList(new GraphQLTypeReference(type)))
                                          .build()
            )
            .fields(managedType.getAttributes().stream()
                                               .filter(Attribute::isAssociation)
                                               .filter(this::isNotIgnored)
                                               .filter(this::isNotIgnoredFilter)
                                               .map(this::getWhereInputRelationField)
                                               .collect(Collectors.toList())
            );

        return whereInputObject.build();

    }

    private String resolveTypeName(ManagedType<?> managedType) {
        String typeName="";

        if (managedType instanceof EmbeddableType){
            typeName = managedType.getJavaType().getSimpleName()+"EmbeddableType";
        } else if (managedType instanceof EntityType) {
            typeName = ((EntityType<?>)managedType).getName();
        }

        return typeName;
    }

    private GraphQLInputObjectType getWhereInputType(ManagedType<?> managedType) {
        return inputObjectCache.computeIfAbsent(managedType, this::computeWhereInputType);
    }

    private String resolveWhereInputTypeName(ManagedType<?> managedType) {
        String typeName=resolveTypeName(managedType);

        return namingStrategy.pluralize(typeName)+"RelationCriteriaExpression";

    }

    private GraphQLInputObjectType computeWhereInputType(ManagedType<?> managedType) {
        String type=resolveWhereInputTypeName(managedType);

         Builder whereInputObject = GraphQLInputObjectType.newInputObject()
            .name(type)
            .description("Where logical AND specification of the provided list of criteria expressions")
            .field(GraphQLInputObjectField.newInputObjectField()
                                          .name(Logical.OR.name())
                                          .description("Logical operation for expressions")
                                          .type(new GraphQLList(new GraphQLTypeReference(type)))
                                          .build()
            )
            .field(GraphQLInputObjectField.newInputObjectField()
                                          .name(Logical.AND.name())
                                          .description("Logical operation for expressions")
                                          .type(new GraphQLList(new GraphQLTypeReference(type)))
                                          .build()
            )
            .field(GraphQLInputObjectField.newInputObjectField()
                                          .name(Logical.EXISTS.name())
                                          .description("Logical EXISTS subquery expression")
                                          .type(new GraphQLList(getSubqueryInputType(managedType)))
                                          .build()
            )
            .field(GraphQLInputObjectField.newInputObjectField()
                                          .name(Logical.NOT_EXISTS.name())
                                          .description("Logical NOT EXISTS subquery expression")
                                          .type(new GraphQLList(getSubqueryInputType(managedType)))
                                          .build()
            )
            .fields(managedType.getAttributes().stream()
                                               .filter(this::isValidInput)
                                               .filter(this::isNotIgnored)
                                               .filter(this::isNotIgnoredFilter)
                                               .map(this::getWhereInputField)
                                               .collect(Collectors.toList())
            )
            .fields(managedType.getAttributes().stream()
                                               .filter(Attribute::isAssociation)
                                               .filter(this::isNotIgnored)
                                               .filter(this::isNotIgnoredFilter)
                                               .map(this::getWhereInputRelationField)
                                               .collect(Collectors.toList())
            );


        return whereInputObject.build();

    }

    private GraphQLInputObjectField getWhereInputRelationField(Attribute<?,?> attribute) {
        ManagedType<?> foreignType = getForeignType(attribute);

        String type = resolveWhereInputTypeName(foreignType);
        String description = getSchemaDescription(attribute);

        return GraphQLInputObjectField.newInputObjectField()
                                      .name(attribute.getName())
                                      .description(description)
                                      .type(new GraphQLTypeReference(type))
                                      .build();
    }

    private GraphQLInputObjectField getWhereInputField(Attribute<?,?> attribute) {
        GraphQLInputType type = getWhereAttributeType(attribute);
        String description = getSchemaDescription(attribute);

        if (type instanceof GraphQLInputType) {
            return GraphQLInputObjectField.newInputObjectField()
                                          .name(attribute.getName())
                                          .description(description)
                                          .type(type)
                                          .build();
        }

        throw new IllegalArgumentException("Attribute " + attribute.getName() + " cannot be mapped as an Input Argument");
    }

    private Map<String, GraphQLInputType> whereAttributesMap = new HashMap<>();

    private GraphQLInputType getWhereAttributeType(Attribute<?,?> attribute) {
        String type =  namingStrategy.singularize(attribute.getName())+attribute.getDeclaringType().getJavaType().getSimpleName()+"Criteria";

        if(whereAttributesMap.containsKey(type))
           return whereAttributesMap.get(type);

        GraphQLInputObjectType.Builder builder = GraphQLInputObjectType.newInputObject()
            .name(type)
            .description("Criteria expression specification of "+namingStrategy.singularize(attribute.getName())+" attribute in entity " + attribute.getDeclaringType().getJavaType())
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(Logical.OR.name())
                .description("Logical OR criteria expression")
                .type(new GraphQLList(new GraphQLTypeReference(type)))
                .build()
            )
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(Logical.AND.name())
                .description("Logical AND criteria expression")
                .type(new GraphQLList(new GraphQLTypeReference(type)))
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
                        .description("Like criteria, case sensitive")
                        .type(getAttributeInputType(attribute))
                        .build()
                    )
                    .field(GraphQLInputObjectField.newInputObjectField()
                           .name(Criteria.LIKE_.name())
                           .description("Like criteria, case insensitive")
                           .type(getAttributeInputType(attribute))
                           .build()
                    )
                    .field(GraphQLInputObjectField.newInputObjectField()
                           .name(Criteria.LOWER.name())
                           .description("Case insensitive match criteria")
                           .type(getAttributeInputType(attribute))
                           .build()
                    )
                    .field(GraphQLInputObjectField.newInputObjectField()
                           .name(Criteria.EQ_.name())
                           .description("Case equals case insensitive match criteria")
                           .type(getAttributeInputType(attribute))
                           .build()
                    )
                    .field(GraphQLInputObjectField.newInputObjectField()
                           .name(Criteria.NE_.name())
                           .description("Not equals case insensitive match criteria")
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
                        .description("Starts with criteria, case sensitive")
                        .type(getAttributeInputType(attribute))
                        .build()
                    )
                    .field(GraphQLInputObjectField.newInputObjectField()
                           .name(Criteria.STARTS_.name())
                           .description("Starts with criteria, case insensitive")
                           .type(getAttributeInputType(attribute))
                           .build()
                    )
                    .field(GraphQLInputObjectField.newInputObjectField()
                        .name(Criteria.ENDS.name())
                        .description("Ends with criteria, case sensitive")
                        .type(getAttributeInputType(attribute))
                        .build()
                    )
                    .field(GraphQLInputObjectField.newInputObjectField()
                           .name(Criteria.ENDS_.name())
                           .description("Ends with criteria, case insensitive")
                           .type(getAttributeInputType(attribute))
                           .build()
                    );
                }
                else if (attribute.getJavaMember().getClass().isAssignableFrom(Field.class)
                        && Field.class.cast(attribute.getJavaMember())
                                      .isAnnotationPresent(Convert.class))
                {
                    builder.field(GraphQLInputObjectField.newInputObjectField()
                                                         .name(Criteria.LOCATE.name())
                                                         .description("Locate search criteria")
                                                         .type(getAttributeInputType(attribute))
                                                         .build());
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
        String description = getSchemaDescription(attribute);

        return GraphQLArgument.newArgument()
                .name(attribute.getName())
                .type(type)
                .description(description)
                .build();
    }

    private GraphQLType getEmbeddableType(EmbeddableType<?> embeddableType, boolean input) {
        if (input && embeddableInputCache.containsKey(embeddableType.getJavaType()))
            return embeddableInputCache.get(embeddableType.getJavaType());

        if (!input && embeddableOutputCache.containsKey(embeddableType.getJavaType()))
            return embeddableOutputCache.get(embeddableType.getJavaType());
        String embeddableTypeName = namingStrategy.singularize(embeddableType.getJavaType().getSimpleName())+ (input ? "Input" : "") +"EmbeddableType";
        GraphQLType graphQLType=null;
        if (input) {
            graphQLType = GraphQLInputObjectType.newInputObject()
                    .name(embeddableTypeName)
                    .description(getSchemaDescription(embeddableType))
                    .fields(embeddableType.getAttributes().stream()
                            .filter(this::isNotIgnored)
                            .map(this::getInputObjectField)
                            .collect(Collectors.toList())
                    )
                    .build();
        } else {
            graphQLType = GraphQLObjectType.newObject()
                    .name(embeddableTypeName)
                    .description(getSchemaDescription(embeddableType))
                    .fields(embeddableType.getAttributes().stream()
                            .filter(this::isNotIgnored)
                            .map(this::getObjectField)
                            .collect(Collectors.toList())
                    )
                    .build();
        }
        if (input) {
            embeddableInputCache.putIfAbsent(embeddableType.getJavaType(), (GraphQLInputObjectType) graphQLType);
        } else{
            embeddableOutputCache.putIfAbsent(embeddableType.getJavaType(), (GraphQLObjectType) graphQLType);
        }

        return graphQLType;
    }


    private GraphQLObjectType getObjectType(EntityType<?> entityType) {
        return entityCache.computeIfAbsent(entityType, this::computeObjectType);
    }


    private GraphQLObjectType computeObjectType(EntityType<?> entityType) {
    	return GraphQLObjectType.newObject()
				                .name(entityType.getName())
				                .description(getSchemaDescription(entityType))
				                .fields(getEntityAttributesFields(entityType))
				                .fields(getTransientFields(entityType))
				                .build();
    }

    private List<GraphQLFieldDefinition> getEntityAttributesFields(EntityType<?> entityType) {
        return entityType.getAttributes()
                         .stream()
                         .filter(attribute -> EntityIntrospector.introspect(entityType)
                                                                .isNotIgnored(attribute.getName()))
                         .map(it -> getObjectField(it, entityType))
                         .collect(Collectors.toList());
    }

    private List<GraphQLFieldDefinition> getTransientFields(ManagedType<?> managedType) {
        return EntityIntrospector.introspect(managedType)
                                 .getTransientPropertyDescriptors()
                                 .stream()
                                 .filter(AttributePropertyDescriptor::isNotIgnored)
                                 .map(this::getJavaFieldDefinition)
                                 .collect(Collectors.toList());
    }

    @SuppressWarnings( { "rawtypes" } )
    private GraphQLFieldDefinition getJavaFieldDefinition(AttributePropertyDescriptor propertyDescriptor) {
    	GraphQLOutputType type = getGraphQLTypeFromJavaType(propertyDescriptor.getPropertyType());
        DataFetcher dataFetcher = PropertyDataFetcher.fetching(propertyDescriptor.getName());

        String description = propertyDescriptor.getSchemaDescription().orElse(null);

        return GraphQLFieldDefinition.newFieldDefinition()
                                     .name(propertyDescriptor.getName())
                                     .description(description)
                                     .type(type)
                                     .dataFetcher(dataFetcher)
                                     .build();
    }

    private GraphQLFieldDefinition getObjectField(Attribute<?,?> attribute) {
        return getObjectField(attribute, null);
    }

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    private GraphQLFieldDefinition getObjectField(Attribute attribute, EntityType baseEntity) {
        GraphQLOutputType type = getAttributeOutputType(attribute);

        List<GraphQLArgument> arguments = new ArrayList<>();
        DataFetcher dataFetcher = PropertyDataFetcher.fetching(attribute.getName());

        // Only add the orderBy argument for basic attribute types
        if (attribute instanceof SingularAttribute
            && attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.BASIC
            && isNotIgnoredOrder(attribute) ) {
            arguments.add(GraphQLArgument.newArgument()
                                         .name(ORDER_BY_PARAM_NAME)
                                         .description("Specifies field sort direction in the query results.")
                                         .type(orderByDirectionEnum)
                                         .defaultValue("ASC")
                                         .build()
            );
        }


        // Get the fields that can be queried on (i.e. Simple Types, no Sub-Objects)
        if (attribute instanceof SingularAttribute
            && attribute.getPersistentAttributeType() != Attribute.PersistentAttributeType.BASIC) {
            ManagedType foreignType = getForeignType(attribute);
            SingularAttribute<?,?> singularAttribute = SingularAttribute.class.cast(attribute);

            // TODO fix page count query
            arguments.add(getWhereArgument(foreignType));

            // to-one end could be optional
            arguments.add(optionalArgument(singularAttribute.isOptional()));

            GraphQLObjectType entityObjectType = GraphQLObjectType.newObject()
                    .name(baseEntity.getName())
                    .build();

            GraphQLJpaQueryFactory graphQLJpaQueryFactory = GraphQLJpaQueryFactory.builder()
                                                                                  .withEntityManager(entityManager)
                                                                                  .withEntityType(baseEntity)
                                                                                  .withEntityObjectType(entityObjectType)
                                                                                  .withSelectNodeName(entityObjectType.getName())
                                                                                  .withDefaultDistinct(isDefaultDistinct)
                                                                                  .withRestrictedKeysProvider(restrictedKeysProvider)
                                                                                  .build();

            String dataLoaderKey = baseEntity.getName() + "." + attribute.getName();

            MappedBatchLoaderWithContext<Object, Object> mappedBatchLoader = new GraphQLJpaToOneMappedBatchLoader(graphQLJpaQueryFactory);

            batchLoadersRegistry.get()
                                .registerToOne(dataLoaderKey, mappedBatchLoader);

            dataFetcher = new GraphQLJpaToOneDataFetcher(graphQLJpaQueryFactory,
                                                         (SingularAttribute) attribute);


        } //  Get Sub-Objects fields queries via DataFetcher
        else if (attribute instanceof PluralAttribute
                && (attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.ONE_TO_MANY
                || attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.MANY_TO_MANY)) {
            Assert.assertNotNull(baseEntity, "For attribute "+attribute.getName() + " cannot find declaring type!");
            EntityType elementType =  (EntityType) ((PluralAttribute) attribute).getElementType();

            arguments.add(getWhereArgument(elementType));

            // make it configurable via builder api
            arguments.add(optionalArgument(toManyDefaultOptional));

            GraphQLObjectType entityObjectType = GraphQLObjectType.newObject()
                                                                  .name(baseEntity.getName())
                                                                  .build();

            GraphQLJpaQueryFactory graphQLJpaQueryFactory = GraphQLJpaQueryFactory.builder()
                                                                                  .withEntityManager(entityManager)
                                                                                  .withEntityType(baseEntity)
                                                                                  .withEntityObjectType(entityObjectType)
                                                                                  .withSelectNodeName(entityObjectType.getName())
                                                                                  .withDefaultDistinct(isDefaultDistinct)
                                                                                  .withRestrictedKeysProvider(restrictedKeysProvider)
                                                                                  .build();

            String dataLoaderKey = baseEntity.getName() + "." + attribute.getName();

            MappedBatchLoaderWithContext<Object, List<Object>> mappedBatchLoader = new GraphQLJpaToManyMappedBatchLoader(graphQLJpaQueryFactory);

            batchLoadersRegistry.get()
                                .registerToMany(dataLoaderKey, mappedBatchLoader);

            dataFetcher = new GraphQLJpaToManyDataFetcher(graphQLJpaQueryFactory,
                                                             (PluralAttribute) attribute);
        }

        return GraphQLFieldDefinition.newFieldDefinition()
                .name(attribute.getName())
                .description(getSchemaDescription(attribute))
                .type(type)
                .dataFetcher(dataFetcher)
                .arguments(arguments)
                .build();
    }

    private GraphQLArgument optionalArgument(Boolean defaultValue) {
        return GraphQLArgument.newArgument()
                .name("optional")
                .description("Optional association specification")
                .type(Scalars.GraphQLBoolean)
                .defaultValue(defaultValue)
                .build();
    }

    protected ManagedType<?> getForeignType(Attribute<?,?> attribute) {
        if(SingularAttribute.class.isInstance(attribute))
            return (ManagedType<?>) ((SingularAttribute<?,?>) attribute).getType();
        else
            return (EntityType<?>) ((PluralAttribute<?, ?, ?>) attribute).getElementType();
    }

    @SuppressWarnings( { "rawtypes" } )
    private GraphQLInputObjectField getInputObjectField(Attribute attribute) {
        GraphQLInputType type = getAttributeInputType(attribute);

        return GraphQLInputObjectField.newInputObjectField()
                .name(attribute.getName())
                .description(getSchemaDescription(attribute))
                .type(type)
                .build();
    }

    private Stream<Attribute<?,?>> findBasicAttributes(Collection<Attribute<?,?>> attributes) {
        return attributes.stream().filter(it -> it.getPersistentAttributeType() == Attribute.PersistentAttributeType.BASIC);
    }

    private GraphQLInputType getAttributeInputType(Attribute<?,?> attribute) {

        try {
            return (GraphQLInputType) getAttributeType(attribute, true);
        } catch (ClassCastException e){
            throw new IllegalArgumentException("Attribute " + attribute + " cannot be mapped as an Input Argument");
        }
    }

    private GraphQLOutputType getAttributeOutputType(Attribute<?,?> attribute) {
        try {
            return (GraphQLOutputType) getAttributeType(attribute, false);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Attribute " + attribute + " cannot be mapped as an Output Argument");
        }
    }

    @SuppressWarnings( "rawtypes" )
    protected GraphQLType getAttributeType(Attribute<?,?> attribute, boolean input) {

        if (isBasic(attribute)) {
        	return getGraphQLTypeFromJavaType(attribute.getJavaType());
        }
        else if (isEmbeddable(attribute)) {
        	EmbeddableType embeddableType = (EmbeddableType) ((SingularAttribute) attribute).getType();
        	return getEmbeddableType(embeddableType, input);
        }
        else if (isToMany(attribute)) {
            EntityType foreignType = (EntityType) ((PluralAttribute) attribute).getElementType();

            return input ? getWhereInputType(foreignType) : new GraphQLList(new GraphQLTypeReference(foreignType.getName()));
        }
        else if (isToOne(attribute)) {
            EntityType foreignType = (EntityType) ((SingularAttribute) attribute).getType();

            return input ? getWhereInputType(foreignType) : new GraphQLTypeReference(foreignType.getName());
        }
        else if (isElementCollection(attribute)) {
            Type foreignType = ((PluralAttribute) attribute).getElementType();

            if(foreignType.getPersistenceType() == Type.PersistenceType.BASIC) {
                GraphQLType graphQLType = getGraphQLTypeFromJavaType(foreignType.getJavaType());

            	return input ? graphQLType : new GraphQLList(graphQLType);
            }
            else if (foreignType.getPersistenceType() == Type.PersistenceType.EMBEDDABLE) {
                EmbeddableType embeddableType = EmbeddableType.class.cast(foreignType);
                GraphQLType graphQLType = getEmbeddableType(embeddableType, 
                                                            input);
                
                return input ?  graphQLType : new GraphQLList(graphQLType);
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

    protected final boolean isOneToMany(Attribute<?,?> attribute) {
        return attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.ONE_TO_MANY;
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

    private String getSchemaDescription(Attribute<?,?> attribute) {
        return EntityIntrospector.introspect(attribute.getDeclaringType())
                                 .getSchemaDescription(attribute.getName())
                                 .orElse(null);
    }

    private String getSchemaDescription(EntityType<?> entityType) {
        return EntityIntrospector.introspect(entityType)
                                 .getSchemaDescription()
                                 .orElse(null);
    }

    private String getSchemaDescription(EmbeddableType<?> embeddableType) {
        return EntityIntrospector.introspect(embeddableType)
                                 .getSchemaDescription()
                                 .orElse(null);
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
        return isNotIgnored(entityType.getJavaType()) && isNotIgnored(entityType.getJavaType().getName());
    }

    private boolean isNotIgnored(String name) {
        return entityPaths.isEmpty() || entityPaths.stream()
                                                   .anyMatch(prefix -> name.startsWith(prefix));
    }

    private boolean isNotIgnored(Member member) {
        return member instanceof AnnotatedElement && isNotIgnored((AnnotatedElement) member);
    }

    private boolean isNotIgnored(AnnotatedElement annotatedElement) {
        return annotatedElement != null && annotatedElement.getAnnotation(GraphQLIgnore.class) == null;
    }

    protected boolean isNotIgnoredFilter(Attribute<?,?> attribute) {
        return isNotIgnoredFilter(attribute.getJavaMember()) && isNotIgnoredFilter(attribute.getJavaType());
    }

    protected boolean isNotIgnoredFilter(EntityType<?> entityType) {
        return isNotIgnoredFilter(entityType.getJavaType());
    }

    protected boolean isNotIgnoredFilter(Member member) {
        return member instanceof AnnotatedElement && isNotIgnoredFilter((AnnotatedElement) member);
    }

    protected boolean isNotIgnoredFilter(AnnotatedElement annotatedElement) {
        if (annotatedElement != null) {
            GraphQLIgnoreFilter schemaDocumentation = annotatedElement.getAnnotation(GraphQLIgnoreFilter.class);
            return schemaDocumentation == null;
        }

        return false;
    }

    protected boolean isNotIgnoredOrder(Attribute<?,?> attribute) {
        AnnotatedElement annotatedElement = (AnnotatedElement)attribute.getJavaMember();
        if (annotatedElement != null) {
            GraphQLIgnoreOrder schemaDocumentation = annotatedElement.getAnnotation(GraphQLIgnoreOrder.class);
            return schemaDocumentation == null;
        }
        return false;
    }


    @SuppressWarnings( "unchecked" )
    private GraphQLOutputType getGraphQLTypeFromJavaType(Class<?> clazz) {
        if (clazz.isEnum()) {

            if (classCache.containsKey(clazz))
                return classCache.get(clazz);

            GraphQLEnumType.Builder enumBuilder = GraphQLEnumType.newEnum().name(clazz.getSimpleName());
            int ordinal = 0;
            for (Enum<?> enumValue : ((Class<Enum<?>>)clazz).getEnumConstants())
                enumBuilder.value(enumValue.name(), ordinal++);

            GraphQLEnumType enumType = enumBuilder.build();
            setNoOpCoercing(enumType);

            classCache.putIfAbsent(clazz, enumType);

            return enumType;
        } else if (clazz.isArray()) {
            return GraphQLList.list(JavaScalars.of(clazz.getComponentType()));
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
            newArgument().name(PAGE_PARAM_NAME)
                         .description("Page object for pageble requests, specifying the requested start page and limit size.")
                         .type(newInputObject().name("Page")
                                               .description("Page fields for pageble requests.")
                                               .field(newInputObjectField().name(PAGE_START_PARAM_NAME)
                                                                           .description("Start page that should be returned. Page numbers start with 1 (1-indexed)")
                                                                           .defaultValue(1)
                                                                           .type(Scalars.GraphQLInt)
                                                                           .build())
                                               .field(newInputObjectField().name(PAGE_LIMIT_PARAM_NAME)
                                                                           .description("Limit how many results should this page contain")
                                                                           .type(Scalars.GraphQLInt)
                                                                           .build())
                                               .build())
                         .build();

    private static final GraphQLEnumType orderByDirectionEnum =
            GraphQLEnumType.newEnum()
                    .name("OrderBy")
                    .description("Specifies the direction (Ascending / Descending) to sort a field.")
                    .value("ASC", "ASC", "Ascending")
                    .value("DESC", "DESC", "Descending")
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

    public GraphQLJpaSchemaBuilder useDistinctParameter(boolean isUseDistinctParameter) {
        this.isUseDistinctParameter = isUseDistinctParameter;

        return this;
    }

    public boolean isDistinctParameter() {
        return isUseDistinctParameter;
    }

    public boolean isDistinctFetcher() {
        return isDefaultDistinct;
    }

    @Deprecated
    public GraphQLJpaSchemaBuilder setDefaultDistinct(boolean distinctFetcher) {
        this.isDefaultDistinct = distinctFetcher;

        return this;
    }

    public GraphQLJpaSchemaBuilder defaultDistinct(boolean isDefaultDistinct) {
        this.isDefaultDistinct = isDefaultDistinct;

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
    public GraphQLJpaSchemaBuilder entityPath(String path) {
        Assert.assertNotNull(path, "path is null");

        entityPaths.add(path);

        return this;
    }

    @Override
    public GraphQLJpaSchemaBuilder namingStrategy(NamingStrategy instance) {
        Assert.assertNotNull(instance, "instance is null");

        this.namingStrategy = instance;

        return this;
    }

    public boolean isToManyDefaultOptional() {
        return toManyDefaultOptional;
    }

    @Deprecated
    public void setToManyDefaultOptional(boolean toManyDefaultOptional) {
        this.toManyDefaultOptional = toManyDefaultOptional;
    }

    public GraphQLJpaSchemaBuilder toManyDefaultOptional(boolean toManyDefaultOptional) {
        this.toManyDefaultOptional = toManyDefaultOptional;

        return this;
    }

    public boolean isEnableSubscription() {
        return enableSubscription;
    }

    public GraphQLJpaSchemaBuilder enableSubscription(boolean enableSubscription) {
        this.enableSubscription = enableSubscription;

        return this;
    }

    public boolean isEnableDeferDirective() {
        return enableDeferDirective;
    }

    public GraphQLJpaSchemaBuilder enableDeferDirective(boolean enableDeferDirective) {
        this.enableDeferDirective = enableDeferDirective;

        return this;
    }

    public boolean isEnableRelay() {
        return enableRelay;
    }

    public GraphQLJpaSchemaBuilder enableRelay(boolean enableRelay) {
        this.enableRelay = enableRelay;

        return this;
    }

    public int getDefaultMaxResults() {
        return defaultMaxResults;
    }

    public GraphQLJpaSchemaBuilder defaultMaxResults(int defaultMaxResults) {
        this.defaultMaxResults = defaultMaxResults;

        return this;
    }

    public int getDefaultPageLimitSize() {
        return defaultPageLimitSize;
    }

    public GraphQLJpaSchemaBuilder defaultPageLimitSize(int defaultPageLimitSize) {
        this.defaultPageLimitSize = defaultPageLimitSize;

        return this;
    }

    public int getDefaultFetchSize() {
        return defaultFetchSize;
    }

    public GraphQLJpaSchemaBuilder defaultFetchSize(int defaultFetchSize) {
        this.defaultFetchSize = defaultFetchSize;

        return this;
    }

    public boolean isEnableDefaultMaxResults() {
        return enableDefaultMaxResults;
    }

    public GraphQLJpaSchemaBuilder enableDefaultMaxResults(boolean enableDefaultMaxResults) {
        this.enableDefaultMaxResults = enableDefaultMaxResults;

        return this;
    }
    
    public GraphQLJpaSchemaBuilder restrictedKeysProvider(RestrictedKeysProvider restrictedKeysProvider) {
        this.restrictedKeysProvider = restrictedKeysProvider;
        
        return this;
    }

    public RestrictedKeysProvider  getRestrictedKeysProvider() {
        return restrictedKeysProvider;
    }    
    
}
