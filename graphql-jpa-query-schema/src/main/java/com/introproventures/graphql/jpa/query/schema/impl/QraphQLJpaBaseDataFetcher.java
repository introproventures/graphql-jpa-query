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

import static graphql.introspection.Introspection.SchemaMetaFieldDef;
import static graphql.introspection.Introspection.TypeMetaFieldDef;
import static graphql.introspection.Introspection.TypeNameMetaFieldDef;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.Subgraph;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import com.introproventures.graphql.jpa.query.annotation.GraphQLDefaultOrderBy;
import com.introproventures.graphql.jpa.query.schema.impl.PredicateFilter.Criteria;
import graphql.GraphQLException;
import graphql.execution.ValuesResolver;
import graphql.language.Argument;
import graphql.language.ArrayValue;
import graphql.language.BooleanValue;
import graphql.language.Comment;
import graphql.language.EnumValue;
import graphql.language.Field;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.Node;
import graphql.language.NodeVisitor;
import graphql.language.ObjectField;
import graphql.language.ObjectValue;
import graphql.language.SelectionSet;
import graphql.language.SourceLocation;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.language.VariableReference;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingEnvironmentBuilder;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import graphql.util.TraversalControl;
import graphql.util.TraverserContext;

/**
 * Provides base implemetation for GraphQL JPA Query Data Fetchers
 *
 * @author Igor Dianov
 *
 */
class QraphQLJpaBaseDataFetcher implements DataFetcher<Object> {

    // "__typename" is part of the graphql introspection spec and has to be ignored
    private static final String TYPENAME = "__typename";

    protected final EntityManager entityManager;
    protected final EntityType<?> entityType;

    /**
     * Creates JPA entity DataFetcher instance
     *
     * @param entityManager
     * @param entityType
     */
    public QraphQLJpaBaseDataFetcher(EntityManager entityManager, EntityType<?> entityType) {
        this.entityManager = entityManager;
        this.entityType = entityType;
    }

    @Override
    public Object get(DataFetchingEnvironment environment) {
        return getQuery(environment, environment.getFields().iterator().next(), true).getResultList();
    }

    protected TypedQuery<?> getQuery(DataFetchingEnvironment environment, Field field, boolean isDistinct) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<?> query = cb.createQuery((Class<?>) entityType.getJavaType());
        Root<?> from = query.from(entityType);

        from.alias(from.getModel().getName());

        // Build predicates from query arguments
        List<Predicate> predicates =  getFieldArguments(field, query, cb, from, environment)
            .stream()
            .map(it -> getPredicate(cb, from, from, environment, it))
            .filter(it -> it != null)
            .collect(Collectors.toList());

        // Use AND clause to filter results
        if(!predicates.isEmpty())
            query.where(predicates.toArray(new Predicate[predicates.size()]));

        // optionally add default ordering
        mayBeAddDefaultOrderBy(query, from, cb);

        return entityManager.createQuery(query.distinct(isDistinct));
    }

    protected final List<Argument> getFieldArguments(Field field, CriteriaQuery<?> query, CriteriaBuilder cb, From<?,?> from, DataFetchingEnvironment environment) {

        List<Argument> arguments = new ArrayList<>();

        // Loop through all of the fields being requested
        field.getSelectionSet().getSelections().forEach(selection -> {
            if (selection instanceof Field) {
                Field selectedField = (Field) selection;

                // "__typename" is part of the graphql introspection spec and has to be ignored by jpa
                if(!TYPENAME.equals(selectedField.getName()) && !IntrospectionUtils.isTransient(from.getJavaType(), selectedField.getName())) {

                    Path<?> fieldPath = from.get(selectedField.getName());

                    // Build predicate arguments for singular attributes only
                    if(fieldPath.getModel() instanceof SingularAttribute) {
                        // Process the orderBy clause
                        Optional<Argument> orderByArgument = selectedField.getArguments().stream()
                            .filter(this::isOrderByArgument)
                            .findFirst();

                        if (orderByArgument.isPresent()) {
                            if ("DESC".equals(((EnumValue) orderByArgument.get().getValue()).getName()))
                                query.orderBy(cb.desc(fieldPath));
                            else
                                query.orderBy(cb.asc(fieldPath));
                        }

                        // Process where arguments clauses.
                        arguments.addAll(selectedField.getArguments()
                            .stream()
                            .filter(it -> !isOrderByArgument(it))
                            .map(it -> new Argument(selectedField.getName() + "." + it.getName(), it.getValue()))
                            .collect(Collectors.toList()));

                        // Check if it's an object and the foreign side is One.  Then we can eagerly fetch causing an inner join instead of 2 queries
                        if (fieldPath.getModel() instanceof SingularAttribute) {
                            SingularAttribute<?,?> attribute = (SingularAttribute<?,?>) fieldPath.getModel();
                            if (attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.MANY_TO_ONE
                                || attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.ONE_TO_ONE
                            ) {
                                reuseJoin(from, selectedField.getName(), false);
                            }
                        }
                    } else  {
                        // We must add plural attributes with explicit fetch to avoid Hibernate error: 
                        // "query specified join fetching, but the owner of the fetched association was not present in the select list"
                        // TODO Let's try detect optional relation and apply join type
                        reuseJoin(from, selectedField.getName(), false);
                    }
                }
            }
        });

        arguments.addAll(field.getArguments());

        return arguments;
    }

    /**
     * if query orders are empty, then apply default ascending ordering
     * by root id attribute to prevent paging inconsistencies
     *
     * @param query
     * @param from
     * @param cb
     */
    protected void mayBeAddDefaultOrderBy(CriteriaQuery<?> query, From<?,?> from, CriteriaBuilder cb) {
        if (query.getOrderList() == null || query.getOrderList().isEmpty()) {
            EntityType<?> fromEntityType = entityType;
            try {
                java.lang.reflect.Field sortField = getSortAnnotation(fromEntityType.getBindableJavaType());
                if (sortField == null)
                    query.orderBy(cb.asc(from.get(fromEntityType.getId(fromEntityType.getIdType().getJavaType()).getName())));
                else {
                    GraphQLDefaultOrderBy order = sortField.getAnnotation(GraphQLDefaultOrderBy.class);
                    if (order.asc()) {
                        query.orderBy(cb.asc(from.get(sortField.getName())));
                    } else {
                        query.orderBy(cb.desc(from.get(sortField.getName())));
                    }

                }
            } catch (Exception ex) {
                //log.warn("In" + this.getClass().getName(), ex);
            }
        }
    }

    /**
     * Get sorting field with annotation
     *
     * @param clazz
     * @return
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    private java.lang.reflect.Field getSortAnnotation(Class<?> clazz) throws IllegalArgumentException, IllegalAccessException {
        for (java.lang.reflect.Field f : clazz.getDeclaredFields()) {
            if (f.getAnnotation(GraphQLDefaultOrderBy.class) != null) {
                return f;
            }
        }
        //if not found, search in superclass. todo recursive search
        for (java.lang.reflect.Field f : clazz.getSuperclass().getDeclaredFields()) {
            if (f.getAnnotation(GraphQLDefaultOrderBy.class) != null) {
                return f;
            }
        }
        return null;
    }
    protected boolean isOrderByArgument(Argument argument) {
        return GraphQLJpaSchemaBuilder.ORDER_BY_PARAM_NAME.equals(argument.getName());
    }

    @SuppressWarnings( "unchecked" )
    protected Predicate getPredicate(CriteriaBuilder cb, Root<?> from, From<?,?> path, DataFetchingEnvironment environment, Argument argument) {

        if(!argument.getName().contains(".")) {
            Attribute<?,?> argumentEntityAttribute = getAttribute(environment, argument);

            // If the argument is a list, let's assume we need to join and do an 'in' clause
            if (argumentEntityAttribute instanceof PluralAttribute) {
                return reuseJoin(from, argument.getName(), false)
                    .in(convertValue(environment, argument, argument.getValue()));
            }

            return cb.equal(path.get(argument.getName()), convertValue(environment, argument, argument.getValue()));
        } else {
            if(!argument.getName().endsWith(".where")) {
                Path<?> field = getCompoundJoinedPath(path, argument.getName(), false);

                return cb.equal(field, convertValue(environment, argument, argument.getValue()));
            } else {
                String fieldName = argument.getName().split("\\.")[0];

                From<?,?> join = getCompoundJoin(path, argument.getName(), false);
                Argument where = new Argument("where",  argument.getValue());
                Map<String, Object> variables = Optional.ofNullable(environment.getContext())
                		.filter(it -> it instanceof Map)
                		.map(it -> (Map<String, Object>) it)
                		.map(it -> (Map<String, Object>) it.get("variables"))
                		.orElse(Collections.emptyMap());

                GraphQLFieldDefinition fieldDef = getFieldDef(
                    environment.getGraphQLSchema(),
                    this.getObjectType(environment, argument),
                    new Field(fieldName)
                );

                Map<String, Object> arguments = (Map<String, Object>) new ValuesResolver()
                    .getArgumentValues(fieldDef.getArguments(), Collections.singletonList(where), variables)
                    .get("where");

                return getWherePredicate(cb, from, join, wherePredicateEnvironment(environment, fieldDef, arguments), where);
            }
        }
    }

    @SuppressWarnings( "unchecked" )
    private <R extends Value> R getValue(Argument argument) {
        return (R) argument.getValue();
    }

    protected Predicate getWherePredicate(CriteriaBuilder cb, Root<?> root,  From<?,?> path, DataFetchingEnvironment environment, Argument argument) {
        ObjectValue whereValue = getValue(argument);

        if(whereValue.getChildren().isEmpty())
            return cb.conjunction();
        
        Logical logical = extractLogical(argument);
        
        Map<String, Object> predicateArguments = new LinkedHashMap<>();
        predicateArguments.put(logical.name(), environment.getArguments());
        
        DataFetchingEnvironment predicateDataFetchingEnvironment = DataFetchingEnvironmentBuilder.newDataFetchingEnvironment(environment)
                                                                                                 .arguments(predicateArguments)
                                                                                                 .build();
        Argument predicateArgument = new Argument(logical.name(), whereValue);

        return getArgumentPredicate(cb, (path != null) ? path : root, predicateDataFetchingEnvironment, predicateArgument);
    }

    protected Predicate getArgumentPredicate(CriteriaBuilder cb, From<?,?> path,
        DataFetchingEnvironment environment, Argument argument) {
        ObjectValue whereValue = getValue(argument);

        if (whereValue.getChildren().isEmpty())
            return cb.disjunction(); 
        
        Logical logical = extractLogical(argument);

        List<Predicate> predicates = new ArrayList<>();

        whereValue.getObjectFields().stream()
            .filter(it -> Logical.names().contains(it.getName()))
            .map(it -> getArgumentPredicate(cb, path,
                argumentEnvironment(environment, argument.getName()),
                new Argument(it.getName(), it.getValue())))
            .forEach(predicates::add);

        whereValue.getObjectFields().stream()
            .filter(it -> !Logical.names().contains(it.getName()))
            .map(it -> getFieldPredicate(it.getName(), cb, path, it,
                argumentEnvironment(environment, argument.getName()),
                new Argument(it.getName(), it.getValue())))
            .filter(predicate -> predicate != null)
            .forEach(predicates::add);

        if (predicates.isEmpty())
            predicates.add(cb.disjunction());

        return (logical == Logical.OR)
            ? cb.or(predicates.toArray(new Predicate[predicates.size()]))
            : cb.and(predicates.toArray(new Predicate[predicates.size()]));
    }
    
    private Logical extractLogical(Argument argument) {
        return Optional.of(argument.getName())
                .filter(it -> Logical.names().contains(it))
                .map(it -> Logical.valueOf(it))
                .orElse(Logical.AND);
    }

    private Predicate getFieldPredicate(String fieldName, CriteriaBuilder cb, From<?,?> path, ObjectField objectField, DataFetchingEnvironment environment, Argument argument) {
        ObjectValue expressionValue;
        
        if(objectField.getValue() instanceof ObjectValue)
            expressionValue = (ObjectValue) objectField.getValue();
        else 
            expressionValue = new ObjectValue(Arrays.asList(objectField));

        if(expressionValue.getChildren().isEmpty())
            return cb.disjunction();

        Logical logical = extractLogical(argument);

        List<Predicate> predicates = new ArrayList<>();

        expressionValue.getObjectFields().stream()
            .filter(it -> Logical.names().contains(it.getName()))
            .map(it -> getFieldPredicate(fieldName, cb, path, it,
                argumentEnvironment(environment, argument.getName()),
                new Argument(it.getName(), it.getValue()))
            )
            .forEach(predicates::add);
        
        // Let's parse relation criteria expressions if present
        expressionValue.getObjectFields().stream()
            .filter(it -> !Logical.names().contains(it.getName()) && !Criteria.names().contains(it.getName()))
            .map(it -> {
                GraphQLFieldDefinition fieldDefinition = getFieldDef(environment.getGraphQLSchema(),
                                                                     this.getObjectType(environment, argument),
                                                                     new Field(fieldName));

                Map<String, Object> arguments = new LinkedHashMap<>();

                arguments.put(logical.name(), environment.getArgument(fieldName));
                
                return getArgumentPredicate(cb, reuseJoin(path, fieldName, false),  
                                            wherePredicateEnvironment(environment, fieldDefinition, arguments),
                                            new Argument(logical.name(), expressionValue));
               }
            )
            .forEach(predicates::add);

        Optional<Predicate> relationPredicate = predicates.stream().findFirst();
        
        // Let's check if relation criteria predicate exists, to avoid adding duplicate predicates in the query
        if(relationPredicate.isPresent()) {
            return relationPredicate.get();
        }
        
        JpaPredicateBuilder pb = new JpaPredicateBuilder(cb, EnumSet.of(Logical.AND));

        expressionValue.getObjectFields().stream()
            .filter(it -> Criteria.names().contains(it.getName()))
            .map(it -> getPredicateFilter(new ObjectField(fieldName, it.getValue()),
                argumentEnvironment(environment, argument.getName()),
                new Argument(it.getName(), it.getValue()))
            )
            .sorted()
            .map(it -> pb.getPredicate(path, path.get(it.getField()), it))
            .filter(predicate -> predicate != null)
            .forEach(predicates::add);

        return  (logical == Logical.OR)
            ? cb.or(predicates.toArray(new Predicate[predicates.size()]))
            : cb.and(predicates.toArray(new Predicate[predicates.size()]));

    }

	private PredicateFilter getPredicateFilter(ObjectField objectField, DataFetchingEnvironment environment, Argument argument) {
        EnumSet<PredicateFilter.Criteria> options =
            EnumSet.of(PredicateFilter.Criteria.valueOf(argument.getName()));
        
        Map<String, Object> valueArguments = new LinkedHashMap<String,Object>();
        valueArguments.put(objectField.getName(), environment.getArgument(argument.getName()));
        
        DataFetchingEnvironment dataFetchingEnvironment = DataFetchingEnvironmentBuilder.newDataFetchingEnvironment(environment)
                                                                                        .arguments(valueArguments)
                                                                                        .build();
        
        Argument dataFetchingArgument = new Argument(objectField.getName(), argument.getValue());
        
        Object filterValue = convertValue( dataFetchingEnvironment, dataFetchingArgument, argument.getValue() );

        return new PredicateFilter(objectField.getName(), filterValue, options );
    }

    protected final DataFetchingEnvironment argumentEnvironment(DataFetchingEnvironment environment, String argumentName) {
        return DataFetchingEnvironmentBuilder.newDataFetchingEnvironment(environment)
                                             .arguments(environment.getArgument(argumentName))
                                             .build();
    }

    protected final DataFetchingEnvironment wherePredicateEnvironment(DataFetchingEnvironment environment, GraphQLFieldDefinition fieldDefinition, Map<String, Object> arguments) {
        return DataFetchingEnvironmentBuilder.newDataFetchingEnvironment(environment)
                                             .arguments(arguments)
                                             .fieldDefinition(fieldDefinition)
                                             .fieldType(fieldDefinition.getType())
                                             .build();
    }
    
    /**
     * @param fieldName
     * @return Path of compound field to the primitive type
     */
    private From<?,?> getCompoundJoin(From<?,?> rootPath, String fieldName, boolean outer) {
        String[] compoundField = fieldName.split("\\.");

        Join<?,?> join;

        if (compoundField.length == 1) {
            return rootPath;
        } else {
            join = reuseJoin(rootPath, compoundField[0], outer);
        }

        for (int i = 1; i < compoundField.length; i++) {
            if (i < (compoundField.length - 1)) {
                join = reuseJoin(join, compoundField[i], outer);
            } else {
                return join;
            }
        }

        return null;
    }

    /**
     * @param fieldName
     * @return Path of compound field to the primitive type
     */
    private Path<?> getCompoundJoinedPath(From<?,?> rootPath, String fieldName, boolean outer) {
        String[] compoundField = fieldName.split("\\.");

        Join<?,?> join;

        if (compoundField.length == 1) {
            return rootPath.get(compoundField[0]);
        } else {
            join = reuseJoin(rootPath, compoundField[0], outer);
        }

        for (int i = 1; i < compoundField.length; i++) {
            if (i < (compoundField.length - 1)) {
                join = reuseJoin(join, compoundField[i], outer);
            } else {
                return join.get(compoundField[i]);
            }
        }

        return null;
    }

    // trying to find already existing joins to reuse
    private Join<?,?> reuseJoin(From<?, ?> path, String fieldName, boolean outer) {

        for (Join<?,?> join : path.getJoins()) {
            if (join.getAttribute().getName().equals(fieldName)) {
                if ((join.getJoinType() == JoinType.LEFT) == outer) {
                    return join;
                }
            }
        }
        return outer ? path.join(fieldName, JoinType.LEFT) : path.join(fieldName);
    }
    
    @SuppressWarnings( { "unchecked", "rawtypes" } )
    protected Object convertValue(DataFetchingEnvironment environment, Argument argument, Value value) {
        if (value instanceof NullValue) {
            return null;
        } else if (value instanceof StringValue) {
            Object convertedValue =  environment.getArgument(argument.getName());
            if (convertedValue != null) {
                // Return real typed resolved value even if the Value is a StringValue
                return convertedValue;
            } else {
                // Return provided StringValue
                return ((StringValue) value).getValue();
            }
        }
        else if (value instanceof VariableReference) {
            Class javaType = getJavaType(environment, argument);
            Object argumentValue = environment.getArguments().get(argument.getName());
            if(javaType.isEnum()) {
                if(argumentValue instanceof Collection) {
                    List<Enum> values = new ArrayList<>();
                    
                    Collection.class.cast(argumentValue)
                                    .forEach(it -> values.add(Enum.valueOf(javaType, it.toString()))); 
                    return values;
                } else {
                    return Enum.valueOf(javaType, argumentValue.toString());
                }
            } 
            else {    
                // Get resolved variable in environment arguments
                return argumentValue;
            }
        } else if (value instanceof ArrayValue) {
            Object convertedValue =  environment.getArgument(argument.getName());
            if (convertedValue != null && !getJavaType(environment, argument).isEnum()) {
                // unwrap [[EnumValue{name='value'}]]
                if(convertedValue instanceof Collection
                    && ((Collection) convertedValue).stream().allMatch(it->it instanceof Collection)) {
                    convertedValue = ((Collection) convertedValue).iterator().next();
                }

                if(convertedValue instanceof Collection
                    && ((Collection) convertedValue).stream().anyMatch(it->it instanceof Value)) {
                    return ((Collection) convertedValue).stream()
                        .map((it) -> convertValue(environment, argument, (Value) it))
                        .collect(Collectors.toList());
                }
                // Return real typed resolved array value
                return convertedValue;
            } else {
                // Wrap converted values in ArrayList
                return ((ArrayValue) value).getValues().stream()
                    .map((it) -> convertValue(environment, argument, it))
                    .collect(Collectors.toList());
            }

        }
        else if (value instanceof EnumValue) {
            Class enumType = getJavaType(environment, argument);
            return Enum.valueOf(enumType, ((EnumValue) value).getName());
        } else if (value instanceof IntValue) {
            return ((IntValue) value).getValue();
        } else if (value instanceof BooleanValue) {
            return ((BooleanValue) value).isValue();
        } else if (value instanceof FloatValue) {
            return ((FloatValue) value).getValue();
        }

        //return value.toString();
        return value;
    }

    /**
     * Resolve Java type from associated query argument JPA model attribute
     *
     * @param environment
     * @param argument
     * @return Java class type
     */
    protected Class<?> getJavaType(DataFetchingEnvironment environment, Argument argument) {
        Attribute<?,?> argumentEntityAttribute = getAttribute(environment, argument);

        if (argumentEntityAttribute instanceof PluralAttribute)
            return ((PluralAttribute<?,?,?>) argumentEntityAttribute).getElementType().getJavaType();

        return argumentEntityAttribute.getJavaType();
    }

    /**
     * Resolve JPA model persistent attribute from query argument instance
     *
     * @param environment
     * @param argument
     * @return JPA model attribute
     */
    private Attribute<?,?> getAttribute(DataFetchingEnvironment environment, Argument argument) {
        GraphQLObjectType objectType = getObjectType(environment, argument);
        EntityType<?> entityType = getEntityType(objectType);

        return entityType.getAttribute(argument.getName());
    }

    /**
     * Resolve JPA model entity type from GraphQL objectType
     *
     * @param objectType
     * @return non-null JPA model entity type.
     * @throws
     *      NoSuchElementException - if there is no value present
     */
    private EntityType<?> getEntityType(GraphQLObjectType objectType) {
        return entityManager.getMetamodel()
                            .getEntities().stream()
                                          .filter(it -> it.getName().equals(objectType.getName()))
                                          .findFirst()
                                          .get();
    }

    /**
     * Resolve GraphQL object type from Argument output type.
     *
     * @param environment
     * @param argument
     * @return resolved GraphQL object type or null if no output type is provided
     */
    private GraphQLObjectType getObjectType(DataFetchingEnvironment environment, Argument argument) {
        GraphQLType outputType = environment.getFieldType();

        if (outputType instanceof GraphQLList)
            outputType = ((GraphQLList) outputType).getWrappedType();

        if (outputType instanceof GraphQLObjectType)
            return (GraphQLObjectType) outputType;

        return null;
    }

    protected Optional<Argument> extractArgument(DataFetchingEnvironment environment, Field field, String argumentName) {
        return field.getArguments().stream()
                                   .filter(it -> argumentName.equals(it.getName()))
                                   .findFirst();
    }


    protected Argument extractArgument(DataFetchingEnvironment environment, Field field, String argumentName, Value defaultValue) {
        return extractArgument(environment, field, argumentName)
                    .orElse(new Argument(argumentName, defaultValue));
    }

    protected GraphQLFieldDefinition getFieldDef(GraphQLSchema schema, GraphQLObjectType parentType, Field field) {
        if (schema.getQueryType() == parentType) {
            if (field.getName().equals(SchemaMetaFieldDef.getName())) {
                return SchemaMetaFieldDef;
            }
            if (field.getName().equals(TypeMetaFieldDef.getName())) {
                return TypeMetaFieldDef;
            }
        }
        if (field.getName().equals(TypeNameMetaFieldDef.getName())) {
            return TypeNameMetaFieldDef;
        }

        GraphQLFieldDefinition fieldDefinition = parentType.getFieldDefinition(field.getName());
        if (fieldDefinition == null) {
            throw new GraphQLException("unknown field " + field.getName());
        }
        return fieldDefinition;
    }

    protected Subgraph<?> buildSubgraph(Field field, Subgraph<?> subgraph) {

        selections(field).forEach(it ->{
            if(hasSelectionSet(it)) {
                Subgraph<?> sg = subgraph.addSubgraph(it.getName());
                buildSubgraph(it, sg);
            } else {
                if(!TYPENAME.equals(it.getName()))
                    subgraph.addAttributeNodes(it.getName());
            }
        });

        return subgraph;
    };


    protected EntityGraph<?> buildEntityGraph(Field root) {

        EntityGraph<?> entityGraph = this.entityManager.createEntityGraph(entityType.getJavaType());

        selections(root)
            .forEach(it -> {
                if(hasSelectionSet(it) 
                		&& hasNoArguments(it) 
                		&& isManagedType(entityType.getAttribute(it.getName()))
                ) {
                    Subgraph<?> sg = entityGraph.addSubgraph(it.getName());
                    buildSubgraph(it, sg);
                } else {
                    if(!TYPENAME.equals(it.getName()) && !IntrospectionUtils.isTransient(entityType.getJavaType(), it.getName()))
                        entityGraph.addAttributeNodes(it.getName());
                }
            });

        return entityGraph;
    };

    
    protected final boolean isManagedType(Attribute<?,?> attribute) {
    	return attribute.getPersistentAttributeType() != PersistentAttributeType.EMBEDDED 
    			&& attribute.getPersistentAttributeType() != PersistentAttributeType.BASIC
    			&& attribute.getPersistentAttributeType() != PersistentAttributeType.ELEMENT_COLLECTION;
    }

    protected final boolean hasNoArguments(Field field) {
        return !hasArguments(field);
    }

    protected final boolean hasArguments(Field field) {
        return field.getArguments() != null && !field.getArguments().isEmpty();
    }

    protected final boolean hasSelectionSet(Field field) {
        return field.getSelectionSet() != null;
    }

    protected final Stream<Field> selections(Field field) {
        SelectionSet selectionSet = field.getSelectionSet() != null
            ? field.getSelectionSet()
            : new SelectionSet(Collections.emptyList());

        return selectionSet.getSelections().stream()
                                           .filter(it -> it instanceof Field)
                                           .map(it -> (Field) it);
    }

    protected final Stream<Field> flatten(Field field) {
        SelectionSet selectionSet = field.getSelectionSet() != null
            ? field.getSelectionSet()
            : new SelectionSet(Collections.emptyList());

        return Stream.concat(
            Stream.of(field),
            selectionSet.getSelections().stream()
                                        .filter(it -> it instanceof Field)
                                        .flatMap(selection -> this.flatten((Field) selection))
        );
    }


    @SuppressWarnings( "unchecked" )
    protected final <R extends Value> R getObjectFieldValue(ObjectValue objectValue, String fieldName) {
        return (R) getObjectField(objectValue, fieldName).map(it-> it.getValue())
                                                         .orElse(new NullValue());
    }

    @SuppressWarnings( "unchecked" )
    protected final <R> R getArgumentValue(Argument argument) {
        return (R) argument.getValue();
    }

    protected final Optional<ObjectField> getObjectField(ObjectValue objectValue, String fieldName) {
        return objectValue.getObjectFields().stream()
                                            .filter(it -> fieldName.equals(it.getName()))
                                            .findFirst();
    }

    protected final Optional<Field> getSelectionField(Field field, String fieldName) {
        return field.getSelectionSet().getSelections().stream()
                                                      .filter(it -> it instanceof Field)
                                                      .map(it -> (Field) it)
                                                      .filter(it -> fieldName.equals(it.getName()))
                                                      .findFirst();
    }

    @SuppressWarnings("rawtypes")
    class NullValue implements Value {
        
        private static final long serialVersionUID = 1L;

        @Override
        public List<Node> getChildren() {
            return new ArrayList<>();
        }

        @Override
        public SourceLocation getSourceLocation() {
            return new SourceLocation(0, 0);
        }

        @Override
        public List<Comment> getComments() {
            return new ArrayList<>();
        }

        @Override
        public boolean isEqualTo(Node node) {
            return node instanceof NullValue;
        }

		@Override
		public TraversalControl accept(TraverserContext context, NodeVisitor visitor) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Value deepCopy() {
			// TODO Auto-generated method stub
			return null;
		}
    }


}
