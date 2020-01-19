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

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.introproventures.graphql.jpa.query.annotation.GraphQLDefaultOrderBy;
import com.introproventures.graphql.jpa.query.introspection.ReflectionUtil;
import com.introproventures.graphql.jpa.query.schema.impl.EntityIntrospector.EntityIntrospectionResult.AttributePropertyDescriptor;
import com.introproventures.graphql.jpa.query.schema.impl.PredicateFilter.Criteria;
import com.introproventures.graphql.jpa.query.support.GraphQLSupport;

import graphql.GraphQLException;
import graphql.execution.ValuesResolver;
import graphql.language.Argument;
import graphql.language.ArrayValue;
import graphql.language.AstValueHelper;
import graphql.language.BooleanValue;
import graphql.language.EnumValue;
import graphql.language.Field;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.NullValue;
import graphql.language.ObjectField;
import graphql.language.ObjectValue;
import graphql.language.SelectionSet;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.language.VariableReference;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;

/**
 * Provides base implemetation for GraphQL JPA Query Data Fetchers
 *
 * @author Igor Dianov
 *
 */
abstract class GraphQLJpaBaseDataFetcher {

    private final static Logger logger = LoggerFactory.getLogger(GraphQLJpaBaseDataFetcher.class);
    
    protected static final String WHERE = "where";
    protected static final String OPTIONAL = "optional";
    // "__typename" is part of the graphql introspection spec and has to be ignored
    private static final String TYPENAME = "__typename";

    protected final EntityManager entityManager;
    protected final EntityType<?> entityType;
    private final boolean toManyDefaultOptional;

    /**
     * Creates JPA entity DataFetcher instance
     *
     * @param entityManager
     * @param entityType
     */
    protected GraphQLJpaBaseDataFetcher(EntityManager entityManager, 
                                     EntityType<?> entityType, 
                                     boolean toManyDefaultOptional) {
        this.entityManager = entityManager;
        this.entityType = entityType;
        this.toManyDefaultOptional = toManyDefaultOptional;
    }

    protected <T> TypedQuery<T> getQuery(DataFetchingEnvironment environment, Field field, boolean isDistinct, Object... keys) {
        DataFetchingEnvironment queryEnvironment = DataFetchingEnvironmentBuilder.newDataFetchingEnvironment(environment)
                                                                                 .localContext(Boolean.TRUE) // Fetch mode
                                                                                 .build();
        
        CriteriaQuery<T> criteriaQuery = getCriteriaQuery(queryEnvironment, field, isDistinct, keys);

        return entityManager.createQuery(criteriaQuery);
    }
    
    protected TypedQuery<Long> getCountQuery(DataFetchingEnvironment environment, Field field) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<?> root = query.from(entityType);

        DataFetchingEnvironment queryEnvironment = DataFetchingEnvironmentBuilder.newDataFetchingEnvironment(environment)
                                                                                 .root(query)
                                                                                 .localContext(Boolean.FALSE) // Join mode
                                                                                 .build();
        root.alias("root");
        
        query.select(cb.count(root));
        
        List<Predicate> predicates = field.getArguments().stream()
            .map(it -> getPredicate(cb, root, null, queryEnvironment, it))
            .filter(it -> it != null)
            .collect(Collectors.toList());
        
        query.where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(query);
    }
    
    protected TypedQuery<Object> getKeysQuery(DataFetchingEnvironment environment, Field field) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object> query = cb.createQuery(Object.class);
        Root<?> from = query.from(entityType);
        
        from.alias("root");

        DataFetchingEnvironment queryEnvironment = DataFetchingEnvironmentBuilder.newDataFetchingEnvironment(environment)
                                                                                 .root(query)
                                                                                 .localContext(Boolean.FALSE)
                                                                                 .build();
        if(hasIdAttribute()) {
            query.select(from.get(idAttributeName()));
        } else if(hasIdClassAttribue()) {
            List<Selection<?>> selection = Stream.of(idClassAttributeNames())
                                                 .map(from::get)
                                                 .collect(Collectors.toList());
            query.multiselect(selection);
        }
        
        List<Predicate> predicates = field.getArguments().stream()
            .map(it -> getPredicate(cb, from, null, queryEnvironment, it))
            .filter(it -> it != null)
            .collect(Collectors.toList());
        
        query.where(predicates.toArray(new Predicate[0]));

        GraphQLSupport.fields(field.getSelectionSet())
                      .filter(it -> isPersistent(environment, it.getName()))
                      .forEach(selection -> {
                          Path<?> selectionPath = from.get(selection.getName());

                          // Process the orderBy clause
                          mayBeAddOrderBy(selection, query, cb, selectionPath, queryEnvironment);
                      });
        
        mayBeAddDefaultOrderBy(query, from, cb);
        
        return entityManager.createQuery(query);
    }
    
    @SuppressWarnings( { "rawtypes", "unchecked" } )
    protected TypedQuery<Object> getCollectionQuery(DataFetchingEnvironment environment, Field field, boolean isDistinct) {
        
        Object source = environment.getSource();
        
        SingularAttribute parentIdAttribute = entityType.getId(Object.class);
        
        Object parentIdValue = getAttributeValue(source, parentIdAttribute);
        
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object> query = cb.createQuery((Class<Object>) entityType.getJavaType());
        Root<?> from = query.from(entityType);
        
        from.alias("owner");
        
        // Must use inner join in parent context
        Join join = from.join(field.getName())
                        .on(cb.in(from.get(parentIdAttribute.getName()))
                              .value(parentIdValue));
        
        query.select(join.alias(field.getName()));
        
        List<Predicate> predicates = getFieldPredicates(field, query, cb, from, join, environment);

        query.where(
            predicates.toArray(new Predicate[0])
        );
        
        // optionally add default ordering 
        mayBeAddDefaultOrderBy(query, join, cb);
        
        return entityManager.createQuery(query.distinct(isDistinct));
    }
    
    @SuppressWarnings("unchecked")
    protected <T> CriteriaQuery<T> getCriteriaQuery(DataFetchingEnvironment environment, Field field, boolean isDistinct, Object... keys) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery((Class<T>) entityType.getJavaType());
        Root<?> from = query.from(entityType);

        DataFetchingEnvironment queryEnvironment = DataFetchingEnvironmentBuilder.newDataFetchingEnvironment(environment)
                                                                                 .root(query)
                                                                                 .build();
        from.alias(from.getModel().getName().toLowerCase());

        // Build predicates from query arguments
        List<Predicate> predicates =  getFieldPredicates(field, query, cb, from, from, queryEnvironment);

        if (keys.length > 0) {
            if(hasIdAttribute()) {
                predicates.add(from.get(idAttributeName()).in(keys));
            } // array of idClass attributes 
            else if (hasIdClassAttribue()) {
                String[] names = idClassAttributeNames();
                Map<String, List<Object>> idKeys = new HashMap<>();

                IntStream.range(0, keys.length)
                         .mapToObj(i -> (Object[]) keys[i])
                         .forEach(values -> {
                             IntStream.range(0, values.length)
                                      .forEach(i -> {
                                          idKeys.computeIfAbsent(names[i], key -> new ArrayList<>())
                                                .add(values[i]);
                                      });        
                         });
                
                List<Predicate> idPredicates = Stream.of(names)
                                                     .map(name -> {
                                                         return from.get(name)
                                                                    .in(idKeys.get(name).toArray(new Object[0]));
                                                     })
                                                     .collect(Collectors.toList());
                
                predicates.add(cb.and(idPredicates.toArray(new Predicate[0]))); 
            }
        }
        
        // Use AND clause to filter results
        if(!predicates.isEmpty())
            query.where(predicates.toArray(new Predicate[0]));

        // optionally add default ordering
        mayBeAddDefaultOrderBy(query, from, cb);

        return query.distinct(isDistinct);
    }
    
    <A,B,C>  Stream<C> zipped(List<A> lista, List<B> listb, BiFunction<A,B,C> zipper){
        int shortestLength = Math.min(lista.size(),listb.size());
        return IntStream.range(0,shortestLength).mapToObj( i -> {
             return zipper.apply(lista.get(i), listb.get(i));
        });        
   }  
    
    protected void mayBeAddOrderBy(Field selectedField, CriteriaQuery<?> query, CriteriaBuilder cb, Path<?> fieldPath, DataFetchingEnvironment environment) {
        // Singular attributes only
        if (fieldPath.getModel() instanceof SingularAttribute) {
            selectedField.getArguments()
                         .stream()
                         .filter(this::isOrderByArgument)
                         .findFirst()
                         .map(argument -> getOrderByValue(argument, environment))
                         .ifPresent(orderBy -> {
                             List<Order> orders = new ArrayList<>(query.getOrderList());
                             
                             if ("DESC".equals(orderBy.getName())) {
                                 orders.add(cb.desc(fieldPath));
                             } else {
                                 orders.add(cb.asc(fieldPath));
                             }
                             
                             query.orderBy(orders);
                         });
        }
    }
    
    protected final List<Predicate> getFieldPredicates(Field field, CriteriaQuery<?> query, CriteriaBuilder cb, Root<?> root, From<?,?> from, DataFetchingEnvironment environment) {

        List<Argument> arguments = new ArrayList<>();
        List<Predicate> predicates = new ArrayList<>();

        // Loop through all of the fields being requested
        GraphQLSupport.fields(field.getSelectionSet())
                      .filter(selection -> isPersistent(environment, selection.getName()))
                      .forEach(selection -> {
                          
            Path<?> fieldPath = from.get(selection.getName());
            From<?,?> fetch = null;
            Optional<Argument> optionalArgument = getArgument(selection, OPTIONAL);
            Optional<Argument> whereArgument = getArgument(selection, WHERE);
            Boolean isOptional = null;

            // Build predicate arguments for singular attributes only
            if(fieldPath.getModel() instanceof SingularAttribute) {
                // Process the orderBy clause
                mayBeAddOrderBy(selection, query, cb, fieldPath, environment);

                // Check if it's an object and the foreign side is One.  Then we can eagerly join causing an inner join instead of 2 queries
                SingularAttribute<?,?> attribute = (SingularAttribute<?,?>) fieldPath.getModel();
                if (attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.MANY_TO_ONE
                    || attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.ONE_TO_ONE
                ) {
                   // Let's do fugly conversion 
                   isOptional = optionalArgument.map(it -> getArgumentValue(environment, it, Boolean.class))
                                                        .orElse(attribute.isOptional());

                   // Let's apply left outer join to retrieve optional associations
                   fetch = reuseFetch(from, selection.getName(), isOptional);
                } else if(attribute.getPersistentAttributeType() == PersistentAttributeType.EMBEDDED) {
                    // Process where arguments clauses.
                    arguments.addAll(selection.getArguments()
                                                  .stream()
                                                  .filter(this::isPredicateArgument)
                                                  .map(it -> new Argument(selection.getName() + "." + it.getName(),
                                                                          it.getValue()))
                                                  .collect(Collectors.toList()));
                        
                }
            } else {
                // We must add plural attributes with explicit join fetch 
                // Let's do fugly conversion 
                // the many end is a collection, and it is always optional by default (empty collection)
                isOptional = optionalArgument.map(it -> getArgumentValue(environment, it, Boolean.class))
                                             .orElse(toManyDefaultOptional);

                GraphQLObjectType objectType = getObjectType(environment);
                EntityType<?> entityType = getEntityType(objectType);

                PluralAttribute<?, ?, ?> attribute = (PluralAttribute<?, ?, ?>) entityType.getAttribute(selection.getName());
                
                // Let's join fetch element collections to avoid filtering their values used where search criteria
                if(PersistentAttributeType.ELEMENT_COLLECTION == attribute.getPersistentAttributeType()) {
                    from.fetch(selection.getName(), JoinType.LEFT);
                } else {
                    // Let's apply fetch join to retrieve associated plural attributes
                    fetch = reuseFetch(from, selection.getName(), isOptional);
                }
            }
            // Let's build join fetch graph to avoid Hibernate error: 
            // "query specified join fetching, but the owner of the fetched association was not present in the select list"
            if(selection.getSelectionSet() != null && fetch != null) {
                Map<String, Object> variables = environment.getVariables();
                
                GraphQLFieldDefinition fieldDefinition = getFieldDefinition(environment.getGraphQLSchema(),
                                                                     this.getObjectType(environment),
                                                                     selection);  
                
                List<Argument> values = whereArgument.map(Collections::singletonList)
                                                     .orElse(Collections.emptyList());
                
                Map<String, Object> fieldArguments = new ValuesResolver().getArgumentValues(fieldDefinition.getArguments(),
                                                                                            values,
                                                                                            variables);
                
                DataFetchingEnvironment fieldEnvironment = wherePredicateEnvironment(environment, 
                                                                                     fieldDefinition, 
                                                                                     fieldArguments);
                
                predicates.addAll(getFieldPredicates(selection, query, cb, root, fetch, fieldEnvironment));
            }
        });
        
        arguments.addAll(field.getArguments());

        arguments.stream()
                 .filter(this::isPredicateArgument)
                 .map(it -> getPredicate(cb, root, from, environment, it))
                 .filter(it -> it != null)
                 .forEach(predicates::add);

        return predicates;
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
            Optional<AttributePropertyDescriptor> attributePropertyDescriptor = EntityIntrospector.introspect(entityType)
                                                                                                  .getPersistentPropertyDescriptors()
                                                                                                  .stream()
                                                                                                  .filter(AttributePropertyDescriptor::hasDefaultOrderBy)
                                                                                                  .findFirst();
            if (!attributePropertyDescriptor.isPresent()) {
                if (hasIdAttribute()) {
                    query.orderBy(cb.asc(from.get(idAttributeName())));
                } else if (hasIdClassAttribue()) {
                    List<Order> orders = Stream.of(idClassAttributeNames())
                                               .map(name -> cb.asc(from.get(name)))
                                               .collect(Collectors.toList());
                    query.orderBy(orders);
                }
            } else {
                AttributePropertyDescriptor attribute =  attributePropertyDescriptor.get();
                
                GraphQLDefaultOrderBy order = attribute.getDefaultOrderBy().get();
                if (order.asc()) {
                    query.orderBy(cb.asc(from.get(attribute.getName())));
                } else {
                    query.orderBy(cb.desc(from.get(attribute.getName())));
                }
            }
        }
    }

    protected boolean isPredicateArgument(Argument argument) {
        return !isOrderByArgument(argument) && !isOptionalArgument(argument);
    }
    
    protected boolean isOrderByArgument(Argument argument) {
        return GraphQLJpaSchemaBuilder.ORDER_BY_PARAM_NAME.equals(argument.getName());
    }

    protected boolean isOptionalArgument(Argument argument) {
        return OPTIONAL.equals(argument.getName());
    }
    
    protected Optional<Argument> getArgument(Field selectedField, String argumentName) {
        return selectedField.getArguments()
                             .stream()
                             .filter(it -> it.getName()
                                             .equals(argumentName))
                             .findFirst();        
    }
    
    protected <R extends Attribute<?,?>> R getAttribute(String attributeName) {
        return (R) entityType.getAttribute(attributeName);
    }
 
    @SuppressWarnings( "unchecked" )
    protected Predicate getPredicate(CriteriaBuilder cb, Root<?> from, From<?,?> path, DataFetchingEnvironment environment, Argument argument) {

        if(!argument.getName().contains(".")) {
            Attribute<?,?> argumentEntityAttribute = getAttribute(environment, argument);

            // If the argument is a list, let's assume we need to join and do an 'in' clause
            if (argumentEntityAttribute instanceof PluralAttribute) {
                // Apply left outer join to retrieve optional associations
                Boolean isFetch = environment.getLocalContext();
                
                return (isFetch ? reuseFetch(from, argument.getName(), false) : reuseJoin(from, argument.getName(), false))
                    .in(convertValue(environment, argument, argument.getValue()));
            }

            return cb.equal(path.get(argument.getName()), convertValue(environment, argument, argument.getValue()));
        } else {
            if(!argument.getName().endsWith(".where")) {
                Path<?> field = getCompoundJoinedPath(path, argument.getName(), false);

                return cb.equal(field, convertValue(environment, argument, argument.getValue()));
            } else {
                String fieldName = argument.getName().split("\\.")[0];

                From<?,?> join = getCompoundJoin(path, argument.getName(), true);
                Argument where = new Argument(WHERE,  argument.getValue());
                Map<String, Object> variables = environment.getVariables();

                GraphQLFieldDefinition fieldDef = getFieldDefinition(
                    environment.getGraphQLSchema(),
                    this.getObjectType(environment),
                    new Field(fieldName)
                );

                Map<String, Object> arguments = (Map<String, Object>) new ValuesResolver()
                    .getArgumentValues(fieldDef.getArguments(), Collections.singletonList(where), variables)
                    .get(WHERE);

                return getWherePredicate(cb, from, join, wherePredicateEnvironment(environment, fieldDef, arguments), where);
            }
        }
    }
    
    

    @SuppressWarnings( "unchecked" )
    private <R extends Value<?>> R getValue(Argument argument, DataFetchingEnvironment environment) {
        Value<?> value = argument.getValue();
        
        if(VariableReference.class.isInstance(value)) {
            Object variableValue = getVariableReferenceValue((VariableReference) value, environment);
            
            GraphQLArgument graphQLArgument = environment.getExecutionStepInfo()
                                                .getFieldDefinition()
                                                .getArgument(argument.getName());
            
            return (R) AstValueHelper.astFromValue(variableValue, graphQLArgument.getType());
        }
        
        return (R) value;
    }

    private EnumValue getOrderByValue(Argument argument, DataFetchingEnvironment environment) {
        Value<?> value = argument.getValue();

        if(VariableReference.class.isInstance(value)) {
            Object variableValue = getVariableReferenceValue((VariableReference) value, environment);
            return EnumValue.newEnumValue(variableValue.toString()).build();
        }
        return (EnumValue) value;
    }

    private Object getVariableReferenceValue(VariableReference variableReference, DataFetchingEnvironment env) {
        return env.getVariables()
                .get(variableReference.getName());
    }

    protected Predicate getWherePredicate(CriteriaBuilder cb, Root<?> root,  From<?,?> path, DataFetchingEnvironment environment, Argument argument) {
        ObjectValue whereValue = getValue(argument, environment);

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

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Predicate getArgumentPredicate(CriteriaBuilder cb, From<?,?> from,
        DataFetchingEnvironment environment, Argument argument) {
        ObjectValue whereValue = getValue(argument, environment);

        if (whereValue.getChildren().isEmpty())
            return cb.disjunction(); 
        
        Logical logical = extractLogical(argument);

        List<Predicate> predicates = new ArrayList<>();

        whereValue.getObjectFields().stream()
            .filter(it -> Logical.names().contains(it.getName()))
            .map(it -> { 
                Map<String, Object> arguments = getFieldArguments(environment, it, argument);
                
                if(it.getValue() instanceof ArrayValue) {
                    return getArgumentsPredicate(cb, from,
                                                argumentEnvironment(environment, arguments),
                                                new Argument(it.getName(), it.getValue()));
                }
                
                return getArgumentPredicate(cb, from,
                                            argumentEnvironment(environment, arguments),
                                            new Argument(it.getName(), it.getValue()));
            })
            .forEach(predicates::add);

        whereValue.getObjectFields()
                  .stream()
                  .filter(it -> !Logical.names().contains(it.getName()))
                  .map(it -> {
                      Map<String, Object> args = getFieldArguments(environment, it, argument);
                      Argument arg = new Argument(it.getName(), it.getValue());

                      return getObjectFieldPredicate(environment, cb, from, logical, it, arg, args);
                  })
                  .filter(predicate -> predicate != null)
                  .forEach(predicates::add);

        return getCompoundPredicate(cb, predicates, logical);
    }
    
    
    protected Predicate getObjectFieldPredicate(DataFetchingEnvironment environment, 
                                                CriteriaBuilder cb, 
                                                From<?,?> from, 
                                                Logical logical,
                                                ObjectField objectField, 
                                                Argument argument, 
                                                Map<String, Object> arguments
                                                 ) {
        if(isEntityType(environment)) {
            Attribute<?,?> attribute = getAttribute(environment, argument);
            
            if(attribute.isAssociation()) {
                GraphQLFieldDefinition fieldDefinition = getFieldDefinition(environment.getGraphQLSchema(),
                                                                     this.getObjectType(environment),
                                                                     new Field(objectField.getName()));
                
                if(Arrays.asList(Logical.EXISTS, Logical.NOT_EXISTS).contains(logical) ) {
                    AbstractQuery<?> query = environment.getRoot();
                    Subquery<?> subquery = query.subquery(attribute.getJavaType());
                    From<?,?> correlation = Root.class.isInstance(from) ? subquery.correlate((Root<?>) from)
                                                                        : subquery.correlate((Join<?,?>) from);
                    
                    Join<?,?> correlationJoin = correlation.join(objectField.getName());
                    
                    DataFetchingEnvironment existsEnvironment = DataFetchingEnvironmentBuilder.newDataFetchingEnvironment(environment)
                                                                                              .root(subquery)
                                                                                              .build();                                                                                                  
                    
                    Predicate restriction = getArgumentPredicate(cb,
                                                                 correlationJoin,
                                                                 wherePredicateEnvironment(existsEnvironment, fieldDefinition, arguments),
                                                                 argument);
                    
                    
                    Predicate exists = cb.exists(subquery.select((Join) correlationJoin)
                                                         .where(restriction));
                    
                    return logical == Logical.EXISTS ? exists : cb.not(exists);
                }
                
                AbstractQuery<?> query = environment.getRoot();
                Boolean isFetch = environment.getLocalContext();
                Boolean isOptional = isOptionalAttribute(attribute);
                
                From<?,?> context = (isSubquery(query) || isCountQuery(query) || !isFetch)
                                        ? reuseJoin(from, objectField.getName(), isOptional)
                                        : reuseFetch(from, objectField.getName(), isOptional);
                            
                return getArgumentPredicate(cb, 
                                            context,
                                            wherePredicateEnvironment(environment, fieldDefinition, arguments),
                                            argument);
            }
        }

        return getLogicalPredicate(objectField.getName(),
                                   cb,
                                   from,
                                   objectField,
                                   argumentEnvironment(environment, arguments),
                                   argument);
    }

    protected boolean isSubquery(AbstractQuery<?> query) {
        return Subquery.class.isInstance(query);
    }
    
    protected boolean isCountQuery(AbstractQuery<?> query) {
        return Optional.ofNullable(query.getSelection())
                       .map(Selection::getJavaType)
                       .map(Long.class::equals)
                       .orElse(false);
    }

    protected Predicate getArgumentsPredicate(CriteriaBuilder cb,
                                                  From<?, ?> from,
                                                  DataFetchingEnvironment environment,
                                                  Argument argument) {
        ArrayValue whereValue = getValue(argument, environment);

        if (whereValue.getValues().isEmpty())
            return cb.disjunction();

        Logical logical = extractLogical(argument);

        List<Predicate> predicates = new ArrayList<>();
        
        List<Map<String,Object>> arguments = environment.getArgument(logical.name());
        List<ObjectValue> values =  whereValue.getValues()
                .stream()
                .map(ObjectValue.class::cast).collect(Collectors.toList());
        
        List<SimpleEntry<ObjectValue, Map<String, Object>>> tuples = 
                IntStream.range(0, values.size())
                         .mapToObj(i -> new SimpleEntry<ObjectValue, Map<String, Object>>(values.get(i),
                                                                                          arguments.get(i)))
                         .collect(Collectors.toList());

        tuples.stream()
              .flatMap(e -> e.getKey()
                             .getObjectFields()
                             .stream()
                             .filter(it -> Logical.names().contains(it.getName()))
                             .map(it -> {
                                 Map<String, Object> args = e.getValue();
                                 Argument arg = new Argument(it.getName(), it.getValue());
                                 
                                 if(ArrayValue.class.isInstance(it.getValue())) {
                                     return getArgumentsPredicate(cb,
                                                                 from,
                                                                 argumentEnvironment(environment, args),
                                                                 arg);
                                 }
                                 
                                 return getArgumentPredicate(cb,
                                                             from,
                                                             argumentEnvironment(environment, args),
                                                             arg);
                                 
                             }))
              .forEach(predicates::add);

        tuples.stream()
              .flatMap(e -> e.getKey()
                             .getObjectFields()
                             .stream()
                             .filter(it -> !Logical.names().contains(it.getName()))
                             .map(it -> {
                                 Map<String, Object> args = e.getValue();
                                 Argument arg = new Argument(it.getName(), it.getValue());
                                 
                                 return getObjectFieldPredicate(environment, cb, from, logical, it, arg, args);
                             }))
              .filter(predicate -> predicate != null)
              .forEach(predicates::add);
        
        return getCompoundPredicate(cb, predicates, logical);
    }
    
    private Map<String, Object> getFieldArguments(DataFetchingEnvironment environment, ObjectField field, Argument argument) {
        Map<String, Object> arguments;
        
        if (environment.getArgument(argument.getName()) instanceof Collection) {
            Collection<Map<String,Object>> list = environment.getArgument(argument.getName());

            arguments = list.stream()
                            .filter(args -> args.get(field.getName()) != null)
                            .findFirst()
                            .orElse(list.stream().findFirst().get());
        } else {
            arguments = environment.getArgument(argument.getName());
        }
        
        return arguments;
    }
    
    private Logical extractLogical(Argument argument) {
        return Optional.of(argument.getName())
                .filter(it -> Logical.names().contains(it))
                .map(it -> Logical.valueOf(it))
                .orElse(Logical.AND);
    }

    private Predicate getLogicalPredicates(String fieldName,
                                             CriteriaBuilder cb,
                                             From<?, ?> path,
                                             ObjectField objectField,
                                             DataFetchingEnvironment environment,
                                             Argument argument) {
        ArrayValue value = ArrayValue.class.cast(objectField.getValue());

        Logical logical = extractLogical(argument);

        List<Predicate> predicates = new ArrayList<>();

        value.getValues()
             .stream()
             .map(ObjectValue.class::cast)
             .flatMap(it -> it.getObjectFields().stream())
             .map(it -> {
                 Map<String, Object> args = getFieldArguments(environment, it, argument);
                 Argument arg = new Argument(it.getName(), it.getValue());
                 
                 return getLogicalPredicate(it.getName(),
                                          cb,
                                          path,
                                          it,
                                          argumentEnvironment(environment, args),
                                          arg);
             })
             .forEach(predicates::add);

        return getCompoundPredicate(cb, predicates, logical);
    }    
    
    private Predicate getLogicalPredicate(String fieldName, CriteriaBuilder cb, From<?,?> path, ObjectField objectField, DataFetchingEnvironment environment, Argument argument) {
        ObjectValue expressionValue;
        
        if(objectField.getValue() instanceof ObjectValue)
            expressionValue = (ObjectValue) objectField.getValue();
        else 
            expressionValue = new ObjectValue(Arrays.asList(objectField));

        if(expressionValue.getChildren().isEmpty())
            return cb.disjunction();

        Logical logical = extractLogical(argument);

        List<Predicate> predicates = new ArrayList<>();

        // Let's parse logical expressions, i.e. AND, OR
        expressionValue.getObjectFields().stream()
            .filter(it -> Logical.names().contains(it.getName()))
            .map(it -> { 
                Map<String, Object> args = getFieldArguments(environment, it, argument);
                Argument arg = new Argument(it.getName(), it.getValue());
                
                if(it.getValue() instanceof ArrayValue) {
                    return getLogicalPredicates(fieldName, cb, path, it,
                                                  argumentEnvironment(environment, args),
                                                  arg);
                }
                
                return getLogicalPredicate(fieldName, cb, path, it,
                                         argumentEnvironment(environment, args),
                                         arg);
            })
            .forEach(predicates::add);
        
        // Let's parse relation criteria expressions if present, i.e. books, author, etc.
        if(expressionValue.getObjectFields()
                          .stream()
                          .anyMatch(it -> !Logical.names().contains(it.getName()) && !Criteria.names().contains(it.getName())))
        {
            GraphQLFieldDefinition fieldDefinition = getFieldDefinition(environment.getGraphQLSchema(),
                                                                 this.getObjectType(environment),
                                                                 new Field(fieldName));
            Map<String, Object> args = new LinkedHashMap<>();
            Argument arg = new Argument(logical.name(), expressionValue);
            boolean isOptional = false;
            
            if(Logical.names().contains(argument.getName())) {
                args.put(logical.name(), environment.getArgument(argument.getName()));
            } else {
                args.put(logical.name(), environment.getArgument(fieldName));

                isOptional = isOptionalAttribute(getAttribute(environment, argument));
            }
            
            return getArgumentPredicate(cb, reuseJoin(path, fieldName, isOptional),  
                                        wherePredicateEnvironment(environment, fieldDefinition, args),
                                        arg);
        }
        
        // Let's parse simple Criteria expressions, i.e. EQ, LIKE, etc. 
        JpaPredicateBuilder pb = new JpaPredicateBuilder(cb);

        expressionValue.getObjectFields()
            .stream()
            .filter(it -> Criteria.names().contains(it.getName()))
            .map(it -> getPredicateFilter(new ObjectField(fieldName, it.getValue()),
                            argumentEnvironment(environment, argument),
                            new Argument(it.getName(), it.getValue())))
            .sorted()
            .map(it -> pb.getPredicate(path, path.get(it.getField()), it))
            .filter(predicate -> predicate != null)
            .forEach(predicates::add);

        return getCompoundPredicate(cb, predicates, logical);

    }
    
    private Predicate getCompoundPredicate(CriteriaBuilder cb, List<Predicate> predicates, Logical logical) {
        if(predicates.isEmpty())
            return cb.disjunction();
        
        if(predicates.size() == 1) {
            return predicates.get(0);
        }
        
        return  (logical == Logical.OR)
                ? cb.or(predicates.toArray(new Predicate[0]))
                : cb.and(predicates.toArray(new Predicate[0]));
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

    protected final DataFetchingEnvironment argumentEnvironment(DataFetchingEnvironment environment,  Map<String, Object> arguments) {
        return DataFetchingEnvironmentBuilder.newDataFetchingEnvironment(environment)
                                             .arguments(arguments)
                                             .build();
    }
	
    protected final DataFetchingEnvironment argumentEnvironment(DataFetchingEnvironment environment, Argument argument) {
        Map<String, Object> arguments = environment.getArgument(argument.getName());
        
        return DataFetchingEnvironmentBuilder.newDataFetchingEnvironment(environment)
                                             .arguments(arguments)
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

        From<?,?> join;

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

        From<?,?> join;

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
    private From<?,?> reuseJoin(From<?, ?> from, String fieldName, boolean outer) {

        for (Join<?,?> join : from.getJoins()) {
            if (join.getAttribute().getName().equals(fieldName)) {
                return join;
            }
        }
        return outer ? from.join(fieldName, JoinType.LEFT) : from.join(fieldName);
    }
    
    // trying to find already existing fetch joins to reuse
    private From<?,?> reuseFetch(From<?, ?> from, String fieldName, boolean outer) {

        for (Fetch<?,?> fetch : from.getFetches()) {
            if (fetch.getAttribute().getName().equals(fieldName)) {
                return (From<?,?>) fetch;
            }
        }
        return outer ? (From<?,?>) from.fetch(fieldName, JoinType.LEFT) : (From<?,?>) from.fetch(fieldName);
    }    
    
    @SuppressWarnings( { "unchecked", "rawtypes" } )
    protected Object convertValue(DataFetchingEnvironment environment, Argument argument, Value value) {
        if (value instanceof NullValue) {
            return value;
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
            Object argumentValue = environment.getVariables()
                                              .get(VariableReference.class.cast(value).getName());
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
            Collection arrayValue =  environment.getArgument(argument.getName());

            if (arrayValue != null) {
                // Let's unwrap array of array values
                if(arrayValue.stream()
                             .allMatch(it->it instanceof Collection)) {
                    arrayValue = Collection.class.cast(arrayValue.iterator()
                                                                 .next());
                }
                
                // Let's convert enum types, i.e. array of strings or EnumValue into Java type
                if(getJavaType(environment, argument).isEnum()) {
                    Function<Object, Value> objectValue = (obj) -> Value.class.isInstance(obj)
                                                                              ? Value.class.cast(obj)
                                                                              : new EnumValue(obj.toString());
                    // Return real typed resolved array values converted into Java enums 
                    return arrayValue.stream()
                                     .map((it) -> convertValue(environment, 
                                                               argument, 
                                                               objectValue.apply(it)))
                                     .collect(Collectors.toList());
                } 
                // Let's try handle Ast Value types 
                else if(arrayValue.stream()
                                  .anyMatch(it->it instanceof Value)) {
                        return arrayValue.stream()
                                         .map(it -> convertValue(environment, 
                                                                 argument, 
                                                                 Value.class.cast(it)))
                                         .collect(Collectors.toList());
                } 
                // Return real typed resolved array value, i.e. Date, UUID, Long
                else {
                    return arrayValue;
                }
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
        } else if (value instanceof ObjectValue) {
            Class javaType = getJavaType(environment, argument);
            Map<String, Object> values = environment.getArgument(argument.getName());
            
            try {
                return getJavaBeanValue(javaType, values);
            } catch (Exception cause) {
                throw new RuntimeException(cause);
            }
        }

        return value;
    }
    
    private Object getJavaBeanValue(Class<?> javaType, Map<String, Object> values) throws Exception {
        Constructor<?> constructor = javaType.getConstructor();
        constructor.setAccessible(true);

        Object javaBean = constructor.newInstance();
        
        values.entrySet()
              .stream()
              .forEach(entry -> {
                  setPropertyValue(javaBean,
                                   entry.getKey(),
                                   entry.getValue());
              });
        
        return javaBean;
    }
    
    private void setPropertyValue(Object javaBean, String propertyName, Object propertyValue) { 
        try {
            BeanInfo bi = Introspector.getBeanInfo(javaBean.getClass());
            PropertyDescriptor pds[] = bi.getPropertyDescriptors();
            for (PropertyDescriptor pd : pds) {
                if (pd.getName().equals(propertyName)) {
                    Method setter = pd.getWriteMethod();
                    setter.setAccessible(true);
                    
                    if (setter != null) {
                        setter.invoke(javaBean, new Object[] {propertyValue} );
                    }
                }
            }
        } catch (Exception ignored) {
            // ignore
        }
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
        GraphQLObjectType objectType = getObjectType(environment);
        EntityType<?> entityType = getEntityType(objectType);

        return entityType.getAttribute(argument.getName());
    }

    private boolean isOptionalAttribute(Attribute<?,?> attribute) {
        if(SingularAttribute.class.isInstance(attribute)) {
            return SingularAttribute.class.cast(attribute).isOptional();
        }
        else if(PluralAttribute.class.isInstance(attribute)) {
            return true;
        }
        
        return false;
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
    
    private boolean isEntityType(DataFetchingEnvironment environment) {
        GraphQLObjectType objectType = getObjectType(environment);
        return entityManager.getMetamodel()
                            .getEntities().stream()
                                          .anyMatch(it -> it.getName().equals(objectType.getName()));
    }    

    /**
     * Resolve GraphQL object type from Argument output type.
     *
     * @param environment
     * @return resolved GraphQL object type or null if no output type is provided
     */
    private GraphQLObjectType getObjectType(DataFetchingEnvironment environment) {
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

    protected GraphQLFieldDefinition getFieldDefinition(GraphQLSchema schema, GraphQLObjectType parentType, Field field) {
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
        
        if (fieldDefinition != null) {
            return fieldDefinition;
        }
        
        throw new GraphQLException("unknown field " + field.getName());
    }
    
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
    protected final <R extends Value<?>> R getObjectFieldValue(ObjectValue objectValue, String fieldName) {
        return (R) getObjectField(objectValue, fieldName).map(ObjectField::getValue)
                                                         .orElse(NullValue.Null);
    }

    @SuppressWarnings( "unchecked" )
    protected final <T> T getArgumentValue(DataFetchingEnvironment environment, Argument argument, Class<T> type) {
        Value<?> value = argument.getValue();
        
        if(VariableReference.class.isInstance(value)) {
            return (T)
                environment.getVariables()
                           .get(VariableReference.class.cast(value).getName());
        }
        else if (BooleanValue.class.isInstance(value)) {
            return (T) Boolean.valueOf(BooleanValue.class.cast(value).isValue());
        }
        else if (IntValue.class.isInstance(value)) {
            return (T) IntValue.class.cast(value).getValue();
        }
        else if (StringValue.class.isInstance(value)) {
            return (T) StringValue.class.cast(value).getValue();
        }
        else if (FloatValue.class.isInstance(value)) {
            return (T) FloatValue.class.cast(value).getValue();
        }
        else if (NullValue.class.isInstance(value)) {
            return (T) null;
        }
        
        throw new IllegalArgumentException("Not supported");
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
    
    protected boolean isPersistent(DataFetchingEnvironment environment,
                                   String attributeName) {
        GraphQLObjectType objectType = getObjectType(environment);
        EntityType<?> entityType = getEntityType(objectType);
        
        return isPersistent(entityType, attributeName);
    }

    protected boolean isPersistent(EntityType<?> entityType,
                                   String attributeName) {
        try {
            return entityType.getAttribute(attributeName) != null;
        } catch (Exception ignored) { } 
        
        return false;
    }
    
    protected boolean isTransient(DataFetchingEnvironment environment,
                                  String attributeName) {
        return !isPersistent(environment, attributeName);
    }

    protected boolean isTransient(EntityType<?> entityType,
                                  String attributeName) {
        return !isPersistent(entityType, attributeName);
    }
    
    protected Optional<Argument> getPageArgument(Field field) {
        return field.getArguments()
            .stream()
            .filter(it -> GraphQLJpaSchemaBuilder.PAGE_PARAM_NAME.equals(it.getName()))
            .findFirst();
    }

    
    protected Page extractPageArgument(DataFetchingEnvironment environment, Optional<Argument> paginationRequest, int defaultPageLimitSize) {

        if (paginationRequest.isPresent()) {

            Map<String, Integer> pagex = environment.getArgument(GraphQLJpaSchemaBuilder.PAGE_PARAM_NAME);
            
            Integer start = pagex.getOrDefault(GraphQLJpaSchemaBuilder.PAGE_START_PARAM_NAME, 1);
            Integer limit = pagex.getOrDefault(GraphQLJpaSchemaBuilder.PAGE_LIMIT_PARAM_NAME, defaultPageLimitSize);

            return new Page(start, limit);
        }

        return new Page(1, defaultPageLimitSize);
    }
    
    static final class Page {
        private int start;
        private int limit;

        public Page(Integer start, Integer limit) {
            this.start = start;
            this.limit = limit;
        }

        
        public int getStart() {
            return start;
        }

        
        public int getLimit() {
            return limit;
        }
    }    

    protected Field removeArgument(Field field, Optional<Argument> argument) {

        if (!argument.isPresent()) {
          return field;
        }

        List<Argument> newArguments = field.getArguments().stream()
            .filter(a -> !a.equals(argument.get())).collect(Collectors.toList());

        return field.transform(builder -> builder.arguments(newArguments));

      }
    
    protected Boolean isWhereArgument(Argument argument) {
        return GraphQLJpaSchemaBuilder.QUERY_WHERE_PARAM_NAME.equals(argument.getName());
    }

    protected Boolean isPageArgument(Argument argument) {
        return GraphQLJpaSchemaBuilder.PAGE_PARAM_NAME.equals(argument.getName());
    }
    
    protected Boolean isLogicalArgument(Argument argument) {
        return GraphQLJpaSchemaBuilder.QUERY_LOGICAL_PARAM_NAME.equals(argument.getName());
    }

    protected Boolean isDistinctArgument(Argument argument) {
        return GraphQLJpaSchemaBuilder.SELECT_DISTINCT_PARAM_NAME.equals(argument.getName());
    }
    
    protected String getJPQLQueryString(TypedQuery<?> query) {
        try {
            Object queryImpl = query.unwrap(TypedQuery.class);
            
            java.lang.reflect.Field queryStringField = ReflectionUtil.getField(queryImpl.getClass(),
                                                                               "queryString");
                                                    
            ReflectionUtil.forceAccess(queryStringField);
            
            return queryStringField.get(queryImpl)
                                   .toString();
            
        } catch (Exception ignored) {
            logger.error("Error getting JPQL string", ignored);
        }
        
        return null;
    }

    protected boolean hasIdAttribute() {
        return entityType.getIdType() != null;
    }
    
    protected String idAttributeName() {
        return entityType.getId(entityType.getIdType()
                                          .getJavaType()).getName();
    }

    protected boolean hasIdClassAttribue() {
        return entityType.getIdClassAttributes() != null;
    }
    
    protected String[] idClassAttributeNames() {
        return entityType.getIdClassAttributes()
                         .stream()
                         .map(SingularAttribute::getName)
                         .sorted()
                         .collect(Collectors.toList())
                         .toArray(new String[0]);
    }
    
    /**
     * Fetches the value of the given SingularAttribute on the given
     * entity.
     *
     * @see http://stackoverflow.com/questions/7077464/how-to-get-singularattribute-mapped-value-of-a-persistent-object
     */
    @SuppressWarnings("unchecked")
    protected <EntityType, FieldType> FieldType getAttributeValue(EntityType entity, SingularAttribute<EntityType, FieldType> field) {
        try {
            Member member = field.getJavaMember();
            if (member instanceof Method) {
                // this should be a getter method:
                return (FieldType) ((Method)member).invoke(entity);
            } else if (member instanceof java.lang.reflect.Field) {
                return (FieldType) ((java.lang.reflect.Field)member).get(entity);
            } else {
                throw new IllegalArgumentException("Unexpected java member type. Expecting method or field, found: " + member);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }    

    /**
     * Fetches the value of the given SingularAttribute on the given
     * entity.
     *
     * @see http://stackoverflow.com/questions/7077464/how-to-get-singularattribute-mapped-value-of-a-persistent-object
     */
    @SuppressWarnings("unchecked")
    protected <EntityType, FieldType> FieldType getAttributeValue(EntityType entity, PluralAttribute<EntityType, ?, FieldType> field) {
        try {
            Member member = field.getJavaMember();
            if (member instanceof Method) {
                // this should be a getter method:
                return (FieldType) ((Method)member).invoke(entity);
            } else if (member instanceof java.lang.reflect.Field) {
                return (FieldType) ((java.lang.reflect.Field)member).get(entity);
            } else {
                throw new IllegalArgumentException("Unexpected java member type. Expecting method or field, found: " + member);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }     
    
}
