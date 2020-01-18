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

import java.util.Optional;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graphql.language.Argument;
import graphql.language.BooleanValue;
import graphql.language.Field;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import reactor.core.publisher.Flux;

/**
 * JPA Query DataFetcher implementation that fetches entities with page and where criteria expressions   
 * 
 * @author Igor Dianov
 *
 */
class GraphQLJpaStreamDataFetcher extends GraphQLJpaBaseDataFetcher implements DataFetcher<Object> {
    
    private final static Logger logger = LoggerFactory.getLogger(GraphQLJpaStreamDataFetcher.class);

    private boolean defaultDistinct = true;
	
    protected static final String HIBERNATE_QUERY_PASS_DISTINCT_THROUGH = "hibernate.query.passDistinctThrough";
    protected static final String ORG_HIBERNATE_CACHEABLE = "org.hibernate.cacheable";
    protected static final String ORG_HIBERNATE_FETCH_SIZE = "org.hibernate.fetchSize";
    protected static final String ORG_HIBERNATE_READ_ONLY = "org.hibernate.readOnly";
    protected static final String JAVAX_PERSISTENCE_FETCHGRAPH = "javax.persistence.fetchgraph";
    

    private GraphQLJpaStreamDataFetcher(EntityManager entityManager, EntityType<?> entityType, boolean toManyDefaultOptional) {
        super(entityManager, entityType, toManyDefaultOptional);
    }

    public GraphQLJpaStreamDataFetcher(EntityManager entityManager, 
                                      EntityType<?> entityType, 
                                      boolean defaultDistinct,
                                      boolean toManyDefaultOptional) {
        super(entityManager, entityType, toManyDefaultOptional);
        this.defaultDistinct = defaultDistinct;
    }

    public boolean isDefaultDistinct() {
        return defaultDistinct;
    }

    public void setDefaultDistinct(boolean defaultDistinct) {
        this.defaultDistinct = defaultDistinct;
    }

    @Override
    public Object get(DataFetchingEnvironment environment) {
        Field field = environment.getField();

        Optional<Argument> pageArgument = getPageArgument(field);
        Page page = extractPageArgument(environment, pageArgument, 100);
        field = removeArgument(field, pageArgument);

        Argument distinctArg = extractArgument(environment, field, GraphQLJpaSchemaBuilder.SELECT_DISTINCT_PARAM_NAME, new BooleanValue(defaultDistinct));
        
        boolean isDistinct = ((BooleanValue) distinctArg.getValue()).isValue();
        
        DataFetchingEnvironment queryEnvironment = environment;
        
        TypedQuery<Object> query = getQuery(queryEnvironment, field, isDistinct);
        
        // Let' try reduce overhead and disable all caching
        query.setHint(ORG_HIBERNATE_READ_ONLY, true);
        query.setHint(ORG_HIBERNATE_FETCH_SIZE, Integer.min(page.getLimit(), 100)); 
        query.setHint(ORG_HIBERNATE_CACHEABLE, false);
        
        // Let's not pass distinct if enabled to have better performance
        if(isDistinct) {
            query.setHint(HIBERNATE_QUERY_PASS_DISTINCT_THROUGH, false);
        }
        
        if (logger.isDebugEnabled()) {
            logger.info("\nGraphQL JPQL Query String:\n    {}", getJPQLQueryString(query));
        }

        // Let's execute query and get results via stream 
        Stream<Object> resultStream = query.getResultStream()
                                           .peek(entityManager::detach);
        
        return Flux.fromIterable(ResultStreamWrapper.wrap(resultStream, 
                                                          page.getLimit()));
    }
    
    @Override
    protected Predicate getPredicate(CriteriaBuilder cb, Root<?> root, From<?,?> path, DataFetchingEnvironment environment, Argument argument) {
        if(isLogicalArgument(argument) || isDistinctArgument(argument))
            return null;
        
        if(isWhereArgument(argument)) 
            return getWherePredicate(cb, root, path, argumentEnvironment(environment, argument), argument);
        
        return super.getPredicate(cb, root, path, environment, argument);
    }

}
