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

/**
 * One-To-Many DataFetcher that uses where argument to filter collection attributes 
 * 
 * @author Igor Dianov
 *
 */
class GraphQLJpaOneToManyDataFetcher extends GraphQLJpaQueryDataFetcher {
    
    private final PluralAttribute<Object,Object,Object> attribute;

    public GraphQLJpaOneToManyDataFetcher(EntityManager entityManager, EntityType<?> entityType, PluralAttribute<Object,Object,Object> attribute) {
        super(entityManager, entityType);
        
        this.attribute = attribute;
    }
    
    @Override
    public Object get(DataFetchingEnvironment environment) {
        Field field = environment.getFields().iterator().next();

        Object source = environment.getSource();
        Optional<Argument> whereArg = extractArgument(environment, field, GraphQLJpaSchemaBuilder.QUERY_WHERE_PARAM_NAME);
        
        // Resolve collection query if where argument is present or any field in selection has orderBy argument
        if(whereArg.isPresent() || hasSelectionAnyOrderBy(field)) {

            //EntityGraph<?> entityGraph = buildEntityGraph(new Field("select", new SelectionSet(Arrays.asList(field))));
            
            // Let's clear session persistent context to avoid getting stale objects cached in the same session 
            // between requests with different search criteria. This looks like a Hibernate bug... 
            entityManager.clear();
            
            return getQuery(environment, field, true)
                //.setHint("javax.persistence.fetchgraph", entityGraph) // TODO: fix runtime exception
                .getResultList();
        }

        // Let hibernate resolve collection query
        return getAttributeValue(source, attribute);
    }
    
    private boolean hasSelectionAnyOrderBy(Field field) {
    	
    	if(!hasSelectionSet(field)) return false;
    	
        // Loop through all of the fields being requested
        for(Selection selection : field.getSelectionSet().getSelections()) {
            if (selection instanceof Field) {
                Field selectedField = (Field) selection;

                // "__typename" is part of the graphql introspection spec and has to be ignored by jpa
                if(!"__typename".equals(selectedField.getName())) {

	                // Optional orderBy argument
	                Optional<Argument> orderBy = selectedField.getArguments().stream()
	                    .filter(this::isOrderByArgument)
	                    .findFirst();
	                
	                if(orderBy.isPresent()) {
                    	return true;
	                }
                }
            }
        }

        return false;
    	
    }    
    @SuppressWarnings( { "rawtypes", "unchecked" } )
    @Override
    protected TypedQuery<?> getQuery(DataFetchingEnvironment environment, Field field, boolean isDistinct) {
        
        Object source = environment.getSource();
        
        SingularAttribute parentIdAttribute = entityType.getId(Object.class);
        
        Object parentIdValue = getAttributeValue(source, parentIdAttribute);
        
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object> query = cb.createQuery((Class<Object>) entityType.getJavaType());
        Root<?> from = query.from(entityType);
        
        from.alias("owner");
        
        // Must use inner join in parent context
        Join join = from.join(attribute.getName())
                        .on(cb.in(from.get(parentIdAttribute.getName()))
                              .value(parentIdValue));
        
        query.select(join.alias(attribute.getName()));
        
        List<Predicate> predicates = getFieldArguments(field, query, cb, join, environment).stream()
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
    
    /**
     * Fetches the value of the given SingularAttribute on the given
     * entity.
     *
     * @see http://stackoverflow.com/questions/7077464/how-to-get-singularattribute-mapped-value-of-a-persistent-object
     */
    @SuppressWarnings("unchecked")
    public <EntityType, FieldType> FieldType getAttributeValue(EntityType entity, SingularAttribute<EntityType, FieldType> field) {
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
    public <EntityType, FieldType> FieldType getAttributeValue(EntityType entity, PluralAttribute<EntityType, ?, FieldType> field) {
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
