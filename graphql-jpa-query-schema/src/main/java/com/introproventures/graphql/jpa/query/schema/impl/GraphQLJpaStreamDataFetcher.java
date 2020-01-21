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

    protected static final String HIBERNATE_QUERY_PASS_DISTINCT_THROUGH = "hibernate.query.passDistinctThrough";
    protected static final String ORG_HIBERNATE_CACHEABLE = "org.hibernate.cacheable";
    protected static final String ORG_HIBERNATE_FETCH_SIZE = "org.hibernate.fetchSize";
    protected static final String ORG_HIBERNATE_READ_ONLY = "org.hibernate.readOnly";
    protected static final String JAVAX_PERSISTENCE_FETCHGRAPH = "javax.persistence.fetchgraph";
    
    private final boolean defaultDistinct;

    private GraphQLJpaStreamDataFetcher(Builder builder) {
        super(builder.entityManager, 
              builder.entityType, 
              builder.toManyDefaultOptional);
        
        this.defaultDistinct = builder.defaultDistinct;
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
    
    public boolean isDefaultDistinct() {
        return defaultDistinct;
    }

    /**
     * Creates builder to build {@link GraphQLJpaStreamDataFetcher}.
     * @return created builder
     */
    public static IEntityManagerStage builder() {
        return new Builder();
    }

    /**
     * Definition of a stage for staged builder.
     */
    public interface IEntityManagerStage {

        /**
        * Builder method for entityManager parameter.
        * @param entityManager field to set
        * @return builder
        */
        public IEntityTypeStage withEntityManager(EntityManager entityManager);
    }

    /**
     * Definition of a stage for staged builder.
     */
    public interface IEntityTypeStage {

        /**
        * Builder method for entityType parameter.
        * @param entityType field to set
        * @return builder
        */
        public IToManyDefaultOptionalStage withEntityType(EntityType<?> entityType);
    }

    /**
     * Definition of a stage for staged builder.
     */
    public interface IToManyDefaultOptionalStage {

        /**
        * Builder method for toManyDefaultOptional parameter.
        * @param toManyDefaultOptional field to set
        * @return builder
        */
        public IDefaultDistinctStage withToManyDefaultOptional(boolean toManyDefaultOptional);
    }

    /**
     * Definition of a stage for staged builder.
     */
    public interface IDefaultDistinctStage {

        /**
        * Builder method for defaultDistinct parameter.
        * @param defaultDistinct field to set
        * @return builder
        */
        public IBuildStage withDefaultDistinct(boolean defaultDistinct);
    }

    /**
     * Definition of a stage for staged builder.
     */
    public interface IBuildStage {

        /**
        * Builder method of the builder.
        * @return built class
        */
        public GraphQLJpaStreamDataFetcher build();
    }

    /**
     * Builder to build {@link GraphQLJpaStreamDataFetcher}.
     */
    public static final class Builder implements IEntityManagerStage, IEntityTypeStage, IToManyDefaultOptionalStage, IDefaultDistinctStage, IBuildStage {

        private EntityManager entityManager;
        private EntityType<?> entityType;
        private boolean toManyDefaultOptional;
        private boolean defaultDistinct;

        private Builder() {
        }

        @Override
        public IEntityTypeStage withEntityManager(EntityManager entityManager) {
            this.entityManager = entityManager;
            return this;
        }

        @Override
        public IToManyDefaultOptionalStage withEntityType(EntityType<?> entityType) {
            this.entityType = entityType;
            return this;
        }

        @Override
        public IDefaultDistinctStage withToManyDefaultOptional(boolean toManyDefaultOptional) {
            this.toManyDefaultOptional = toManyDefaultOptional;
            return this;
        }

        @Override
        public IBuildStage withDefaultDistinct(boolean defaultDistinct) {
            this.defaultDistinct = defaultDistinct;
            return this;
        }

        @Override
        public GraphQLJpaStreamDataFetcher build() {
            return new GraphQLJpaStreamDataFetcher(this);
        }
    }

}
