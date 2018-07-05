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

import com.introproventures.graphql.jpa.query.schema.IQueryAuthorizationStrategy;
import com.introproventures.graphql.jpa.query.schema.exception.AuthorizationException;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import graphql.language.Argument;
import graphql.language.Field;
import graphql.language.Selection;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLDirective;

/**
 * One-To-Many DataFetcher that uses where argument to filter collection attributes
 *
 * @author Igor Dianov
 *
 */
class GraphQLJpaOneToManyDataFetcher extends GraphQLJpaQueryDataFetcher {

    protected final PluralAttribute<Object, Object, Object> attribute;

    public GraphQLJpaOneToManyDataFetcher(EntityManager entityManager, EntityType<?> entityType, PluralAttribute<Object, Object, Object> attribute,
                                          IQueryAuthorizationStrategy authorizationStrategy) {
        super(entityManager, entityType, authorizationStrategy);

        this.attribute = attribute;
    }

    @Override
    public Object get(DataFetchingEnvironment environment) {
        authorization.checkAuthorization(environment);

        Field field = environment.getFields().iterator().next();

        Object source = environment.getSource();
        Optional<Argument> whereArg = extractArgument(environment, field, GraphQLJpaSchemaBuilder.QUERY_WHERE_PARAM_NAME);

        // Resolve collection query if where argument is present or any field in selection has orderBy argument
        if (whereArg.isPresent() || hasSelectionAnyOrderBy(field)) {

            //EntityGraph<?> entityGraph = buildEntityGraph(new Field("select", new SelectionSet(Arrays.asList(field))));

            return getQuery(environment, field, true)
                    //.setHint("javax.persistence.fetchgraph", entityGraph) // TODO: fix runtime exception
                    .getResultList();
        }

        // Let hibernate resolve collection query
        return getAttributeValue(source, attribute);
    }

    private boolean hasSelectionAnyOrderBy(Field field) {

        if (!hasSelectionSet(field)) return false;

        // Loop through all of the fields being requested
        for (Selection selection : field.getSelectionSet().getSelections()) {
            if (selection instanceof Field) {
                Field selectedField = (Field) selection;

                // "__typename" is part of the graphql introspection spec and has to be ignored by jpa
                if (!TYPENAME.equals(selectedField.getName())) {

                    // Optional orderBy argument
                    Optional<Argument> orderBy = selectedField.getArguments().stream()
                            .filter(this::isOrderByArgument)
                            .findFirst();

                    if (orderBy.isPresent()) {
                        return true;
                    }
                }
            }
        }

        return false;

    }
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected TypedQuery<?> getQuery(DataFetchingEnvironment environment, Field field, boolean isDistinct) {

        Object source = environment.getSource();

        SingularAttribute parentIdAttribute = entityType.getId(Object.class);

        Object parentIdValue = getAttributeValue(source, parentIdAttribute);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object> query = cb.createQuery((Class<Object>) entityType.getJavaType());
        //CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<?> from = query.from(entityType);

        from.alias("owner");

        // Must use inner join in parent context
        Join join = from.join(attribute.getName())
                .on(cb.in(from.get(parentIdAttribute.getName())).value(parentIdValue));

        query.select(join.alias(attribute.getName()));
        //query.multiselect(from.alias("owner"), join.alias(attribute.getName()));

        List<Predicate> predicates = getFieldArguments(field, query, cb, join).stream()
                .map(it -> getPredicate(cb, from, join, environment, it))
                .filter(it -> it != null)
                .collect(Collectors.toList());

        query.where(
                predicates.toArray(new Predicate[predicates.size()])
        );

        // optionally add default ordering
        mayBeAddDefaultOrderBy(query, join, cb);

        return entityManager.createQuery(query.distinct(true));

    }
}
