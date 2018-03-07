/*
 * Copyright 2017 IntroPro Ventures Inc. and/or its affiliates.
 * Copyright IBM Corporation 2018
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import com.introproventures.graphql.jpa.query.schema.IQueryAuthorizationStrategy;
import com.introproventures.graphql.jpa.query.schema.exception.AuthorizationException;
import graphql.GraphQLException;
import static graphql.introspection.Introspection.SchemaMetaFieldDef;
import static graphql.introspection.Introspection.TypeMetaFieldDef;
import static graphql.introspection.Introspection.TypeNameMetaFieldDef;
import graphql.language.Argument;
import graphql.language.BooleanValue;
import graphql.language.EnumValue;
import graphql.language.Field;
import graphql.language.IntValue;
import graphql.language.ObjectValue;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingEnvironmentImpl;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;

/**
 * An alternate JPA Query DataFetcher for Eclipselink's BatchFetch support, used in conjunction with the alternate
 * attribute fetcher
 * <p>
 * This implementation fetches entities with page and where criteria expressions
 *
 * @author Ghada Obaid
 */
class GraphQLJpaQueryAlternateDataFetcher extends QraphQLJpaBaseDataFetcher {
    public GraphQLJpaQueryAlternateDataFetcher(EntityManager entityManager, EntityType<?> entityType, IQueryAuthorizationStrategy authorizationStrategy) {
        super(entityManager, entityType, authorizationStrategy);
    }

    @Override
    public Object get(DataFetchingEnvironment environment) {
        if (authorization != null && !authorization.isAuthorized(entityType))
            throw new AuthorizationException();

        Field field = environment.getFields().iterator().next();
        Map<String, Object> result = new LinkedHashMap<>();

        // See which fields we're requesting
        Optional<Field> pagesSelection = getSelectionField(field, GraphQLJpaSchemaBuilder.PAGE_PAGES_PARAM_NAME);
        Optional<Field> totalSelection = getSelectionField(field, GraphQLJpaSchemaBuilder.PAGE_TOTAL_PARAM_NAME);
        Optional<Field> recordsSelection = getSelectionField(field, GraphQLJpaSchemaBuilder.QUERY_SELECT_PARAM_NAME);

        Page page = extractPageArgument(environment, field);
        //Limit the number of rows to return per request
        if (page.size > 1000)
            page.size = 1000;

        Argument distinctArg = extractArgument(environment, field, GraphQLJpaSchemaBuilder.SELECT_DISTINCT_PARAM_NAME, new BooleanValue(true));

        boolean isDistinct = ((BooleanValue) distinctArg.getValue()).isValue();

        DataFetchingEnvironment queryEnvironment = environment;
        Field queryField = field;

        if (recordsSelection.isPresent()) {
            // Override query environment
            String fieldName = recordsSelection.get().getName();

            queryEnvironment = Optional.of(getFieldDef(environment.getGraphQLSchema(), (GraphQLObjectType) environment.getParentType(), field))
                    .map(it -> (GraphQLObjectType) it.getType()).map(it -> it.getFieldDefinition(GraphQLJpaSchemaBuilder.QUERY_SELECT_PARAM_NAME))
                    .map(it -> (DataFetchingEnvironment) new DataFetchingEnvironmentImpl(environment.getSource(), environment.getArguments(),
                            environment.getContext(), environment.getRoot(), environment.getFieldDefinition(), environment.getFields(), it.getType(),
                            environment.getParentType(), environment.getGraphQLSchema(), environment.getFragmentsByName(), environment.getExecutionId(),
                            environment.getSelectionSet(), environment.getFieldTypeInfo()))
                    .orElse(environment);

            queryField = new Field(fieldName, field.getArguments(), recordsSelection.get().getSelectionSet());

            TypedQuery<?> query = null;
            List<Object> entityIds = getIdsToRestrict(environment, queryField, page);
            query = entityManager.createQuery(getQuery(queryEnvironment, queryField, isDistinct, entityIds));

            result.put(GraphQLJpaSchemaBuilder.QUERY_SELECT_PARAM_NAME, query.getResultList());
        }

        if (totalSelection.isPresent() || pagesSelection.isPresent()) {
            final DataFetchingEnvironment countQueryEnvironment = queryEnvironment;
            final Field countQueryField = queryField;

            final Long total = recordsSelection.map(contentField -> getCountQuery(countQueryEnvironment, countQueryField).getSingleResult())
                    // if no "content" was selected an empty Field can be used
                    .orElseGet(() -> getCountQuery(environment, new Field()).getSingleResult());

            result.put(GraphQLJpaSchemaBuilder.PAGE_TOTAL_PARAM_NAME, total);
            result.put(GraphQLJpaSchemaBuilder.PAGE_PAGES_PARAM_NAME, ((Double) Math.ceil(total / (double) page.size)).longValue());
        }

        return result;
    }

    @Override
    protected Predicate getPredicate(CriteriaBuilder cb, Root<?> root, From<?, ?> path, DataFetchingEnvironment environment, Argument argument) {
        if (isLogicalArgument(argument) || isDistinctArgument(argument))
            return null;

        if (isWhereArgument(argument))
            return getWherePredicate(cb, root, path, new ArgumentEnvironment(environment, argument.getName()), argument);

        return super.getPredicate(cb, root, path, environment, argument);
    }

    private List<Object> getIdsToRestrict(DataFetchingEnvironment environment, Field field, Page page) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object> query = cb.createQuery(Object.class);
        Root<?> from = query.from(entityType);

        getQuery(environment, field, true, null, cb, query, from);

        SingularAttribute<?, ?> idAttribute = entityType.getId(Object.class);
        query.select(from.get(idAttribute.getName()));

        List<Object> entityIds = entityManager.createQuery(query).setFirstResult((page.page - 1) * page.size).setMaxResults(page.size).getResultList();

        return entityIds;
    }

    private TypedQuery<Long> getCountQuery(DataFetchingEnvironment environment, Field field) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<?> root = query.from(entityType);

        getQuery(environment, field, true, null, cb, query, root);

        query.orderBy();

        SingularAttribute<?, ?> idAttribute = entityType.getId(Object.class);
        query.select(cb.countDistinct(root.get(idAttribute.getName())));

        return entityManager.createQuery(query);
    }

    private Page extractPageArgument(DataFetchingEnvironment environment, Field field) {
        Optional<Argument> paginationRequest = field.getArguments().stream().filter(it -> GraphQLJpaSchemaBuilder.PAGE_PARAM_NAME.equals(it.getName()))
                .findFirst();

        if (paginationRequest.isPresent()) {
            field.getArguments().remove(paginationRequest.get());

            ObjectValue paginationValues = (ObjectValue) paginationRequest.get().getValue();

            IntValue page = (IntValue) paginationValues.getObjectFields().stream()
                    .filter(it -> GraphQLJpaSchemaBuilder.PAGE_START_PARAM_NAME.equals(it.getName())).findFirst().get().getValue();

            IntValue size = (IntValue) paginationValues.getObjectFields().stream()
                    .filter(it -> GraphQLJpaSchemaBuilder.PAGE_LIMIT_PARAM_NAME.equals(it.getName())).findFirst().get().getValue();

            return new Page(page.getValue().intValue(), size.getValue().intValue());
        }

        return new Page(1, Integer.MAX_VALUE);
    }

    private Boolean isWhereArgument(Argument argument) {
        return GraphQLJpaSchemaBuilder.QUERY_WHERE_PARAM_NAME.equals(argument.getName());

    }

    private Boolean isLogicalArgument(Argument argument) {
        return GraphQLJpaSchemaBuilder.QUERY_LOGICAL_PARAM_NAME.equals(argument.getName());
    }

    private Boolean isDistinctArgument(Argument argument) {
        return GraphQLJpaSchemaBuilder.SELECT_DISTINCT_PARAM_NAME.equals(argument.getName());
    }

    private static final class Page {
        public Integer page;
        public Integer size;

        public Page(Integer page, Integer size) {
            this.page = page;
            this.size = size;
        }
    }

    protected CriteriaQuery<?> getQuery(DataFetchingEnvironment environment, Field field, boolean isDistinct, List<Object> restrictedIds) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<?> query = cb.createQuery(entityType.getJavaType());
        Root<?> from = query.from(entityType);

        return getQuery(environment, field, isDistinct, restrictedIds, cb, query, from);
    }

    protected CriteriaQuery<?> getQuery(DataFetchingEnvironment environment, Field field, boolean isDistinct, List<Object> restrictedIds, CriteriaBuilder cb,
                                        CriteriaQuery<?> query, Root<?> from) {

        from.alias(from.getModel().getName());

        //This is used to generate the proper joins for retricted IDs and count queries
        if (restrictedIds == null)
            buildQuery(field, query, from, restrictedIds != null);

        // Where clauses are to be applied for count and ID queries.  Once we have the IDs, there is no more need for the
        // where clauses
        List<Predicate> predicates = null;
        if (restrictedIds != null) {
            predicates = new ArrayList<>(1);
            if (!restrictedIds.isEmpty()) {
                //Ensure that the IN clause has values
                String idName = entityType.getId(Object.class).getName();
                CriteriaBuilder.In<Object> in = cb.in(from.get(idName));
                restrictedIds.forEach(entityId -> in.value(entityId));
                predicates.add(in);
            } else {
                //Ensure an empty result is returned in this case
                predicates.add(cb.equal(cb.literal(1), cb.literal(0)));
            }
        } else {
            predicates = getPredicates(environment, field, cb, query, from);
        }

        // Use AND clause to filter results
        if (!predicates.isEmpty())
            query.where(predicates.toArray(new Predicate[predicates.size()]));

        //Add orderBy clause
        List<Order> orderByList = new ArrayList<>(2);
        setOrderBy(field, query, cb, from, orderByList);

        // optionally add default ordering
        mayBeAddDefaultOrderBy(query, from, cb);

        return query.distinct(isDistinct);
    }

    private void buildQuery(Field field, CriteriaQuery<?> query, Root<?> root, boolean doSelect) {
        List<Selection<?>> selectionList = new ArrayList<Selection<?>>();
        selections(field).forEach(it -> {
            if (hasSelectionSet(it)) {
                Attribute<?, ?> attribute = root.getModel().getAttribute(it.getName());
                Join<?, ?> join;
                if (useOuterJoin(attribute, field))
                    join = root.join(it.getName(), JoinType.LEFT);
                else
                    join = root.join(it.getName(), JoinType.INNER);
                buildSubquery(it, selectionList, query, join, attribute);
            } else {
                Path<?> currentPath = root.get(it.getName());
                selectionList.add(currentPath);
            }

        });

        if (!selectionList.isEmpty() && doSelect)
            query.multiselect(selectionList);
    }

    /**
     * Determines whether the query should use an inner or outer join. An outer join is used if:
     * 1. there is no where clause and the attribute is nullable; or
     * 2. this is a one-to-many or many-to-many relationship
     *
     * @param attribute
     * @param field
     * @return
     */
    private boolean useOuterJoin(Attribute<?, ?> attribute, Field field) {
        Optional<Argument> whereArgument = field.getArguments().stream().filter(arg -> !isOrderByArgument(arg) && !isDistinctArgument(arg)).findFirst();
        return (!whereArgument.isPresent()
                && ((attribute instanceof SingularAttribute && ((SingularAttribute<?, ?>) attribute).isOptional()) || attribute instanceof PluralAttribute));
    }

    private void buildSubquery(Field field, List<Selection<?>> selectionList, CriteriaQuery<?> query, Join<?, ?> join, Attribute<?, ?> parentAttribute) {
        selections(field).forEach(it -> {
            if (hasSelectionSet(it)) {
                EntityType<?> entityType = entityManager.getMetamodel().entity(parentAttribute.getJavaType());
                Attribute<?, ?> attribute = entityType.getAttribute(it.getName());
                Optional<Argument> whereArgument = it.getArguments().stream().filter(arg -> !isOrderByArgument(arg)).findFirst();
                Join<?, ?> nextJoin;
                if (useOuterJoin(attribute, it)) {
                    nextJoin = join.join(it.getName(), JoinType.LEFT);
                } else {
                    nextJoin = join.join(it.getName(), JoinType.INNER);
                }
                buildSubquery(it, selectionList, query, nextJoin, attribute);
            }
        });

    }

    protected List<Predicate> getPredicates(DataFetchingEnvironment environment, Field field, CriteriaBuilder cb, CriteriaQuery<?> query, Root<?> from) {
        // Build predicates from query arguments
        List<Predicate> predicates = getFieldArgumentsWithoutProcessingAssociations(field, query, cb, from).stream().map(it -> getPredicate(cb, from, from, environment, it))
                .filter(it -> it != null).collect(Collectors.toList());

        return predicates;
    }

    @Override
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

        //When batch fetching, the child's where clause must be part of the parent query in order
        //to properly restrict the query and get correct counts
        GraphQLObjectType parentTypeToCheck = parentType;
        if (parentType.getFieldDefinition("select") != null)
            parentTypeToCheck = (GraphQLObjectType) ((GraphQLList) parentType.getFieldDefinition("select").getType()).getWrappedType();

        GraphQLFieldDefinition fieldDefinition = parentTypeToCheck.getFieldDefinition(field.getName());
        if (fieldDefinition == null) {
            throw new GraphQLException("unknown field " + field.getName());
        }
        return fieldDefinition;
    }

    private List<Argument> getFieldArgumentsWithoutProcessingAssociations(Field field, CriteriaQuery<?> query, CriteriaBuilder cb, From<?, ?> from) {

        List<Argument> arguments = new ArrayList<>();

        if (field.getSelectionSet() == null)
            return arguments;

        // Loop through all of the fields being requested
        field.getSelectionSet().getSelections().forEach(selection -> {
            if (selection instanceof Field) {
                Field selectedField = (Field) selection;

                // "__typename" is part of the graphql introspection spec and has to be ignored by jpa
                if (!TYPENAME.equals(selectedField.getName())) {

                    // Process where arguments clauses.
                    arguments.addAll(selectedField.getArguments().stream().filter(it -> !isOrderByArgument(it))
                            .map(it -> new Argument(selectedField.getName() + "." + it.getName(), it.getValue())).collect(Collectors.toList()));
                }
            }
        });

        arguments.addAll(field.getArguments());

        return arguments;
    }

    protected void setOrderBy(Field field, CriteriaQuery<?> query, CriteriaBuilder cb, From<?, ?> from, List<Order> orderList) {
        if (field.getSelectionSet() == null)
            return;

        // Loop through all of the fields being requested
        field.getSelectionSet().getSelections().forEach(selection -> {
            if (selection instanceof Field) {
                Field selectedField = (Field) selection;

                // "__typename" is part of the graphql introspection spec and has to be ignored by jpa
                if (!TYPENAME.equals(selectedField.getName())) {

                    // Process the orderBy clause
                    if (selectedField.getSelectionSet() != null) {
                        Join<?, ?> joinToReuse = null;
                        Attribute<?, ?> attribute = null;
                        if (from instanceof Root) {
                            attribute = ((Root<?>) from).getModel().getAttribute(selectedField.getName());
                        } else {
                            attribute = ((Join<?, ?>) from).getAttribute();
                        }
                        if (useOuterJoin(attribute, selectedField))
                            joinToReuse = reuseJoin(from, selectedField.getName(), true);
                        else
                            joinToReuse = reuseJoin(from, selectedField.getName(), false);

                        setOrderBy(selectedField, query, cb, joinToReuse, orderList);
                    } else {
                        Path<?> fieldPath = from.get(selectedField.getName());
                        selectedField.getArguments().stream().filter(this::isOrderByArgument).forEach(orderByArgument -> {
                            if ("DESC".equals(((EnumValue) orderByArgument.getValue()).getName()))
                                orderList.add(cb.desc(fieldPath));
                            else
                                orderList.add(cb.asc(fieldPath));

                        });
                    }

                }
            }
        });

        if (!orderList.isEmpty())
            query.orderBy(orderList);
    }

}
