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

import java.util.List;
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
import graphql.schema.GraphQLObjectType;

/**
 * JPA Query DataFetcher implementation that fetches entities with page and where criteria expressions   
 * 
 * @author Igor Dianov
 *
 */
class GraphQLJpaQueryDataFetcher extends GraphQLJpaBaseDataFetcher implements DataFetcher<Object> {
    
    private final static Logger logger = LoggerFactory.getLogger(GraphQLJpaQueryDataFetcher.class);

    private final boolean defaultDistinct;
    private final int defaultMaxResults;
    private final int defaultFetchSize;
    private final int defaultPageLimitSize;
	
    protected static final String HIBERNATE_QUERY_PASS_DISTINCT_THROUGH = "hibernate.query.passDistinctThrough";
    protected static final String ORG_HIBERNATE_CACHEABLE = "org.hibernate.cacheable";
    protected static final String ORG_HIBERNATE_FETCH_SIZE = "org.hibernate.fetchSize";
    protected static final String ORG_HIBERNATE_READ_ONLY = "org.hibernate.readOnly";
    protected static final String JAVAX_PERSISTENCE_FETCHGRAPH = "javax.persistence.fetchgraph";

    private GraphQLJpaQueryDataFetcher(Builder builder) {
        super(builder.entityManager, 
              builder.entityType, 
              builder.toManyDefaultOptional);
        
        this.defaultDistinct = builder.defaultDistinct;
        this.defaultMaxResults = builder.defaultMaxResults;
        this.defaultFetchSize = builder.defaultFetchSize;
        this.defaultPageLimitSize = builder.defaultPageLimitSize;
    }

    public boolean isDefaultDistinct() {
        return defaultDistinct;
    }

    @Override
    public Object get(DataFetchingEnvironment environment) {
        final SelectResult.Builder<Object> result = SelectResult.builder();
        final Optional<Argument> pageArgument = getPageArgument(environment.getField());
        final Field field = removeArgument(environment.getField(), pageArgument);
        final Page page = extractPageArgument(environment, pageArgument, defaultPageLimitSize);

        // Let's see which fields we're requesting
        Optional<Field> pagesSelection = getSelectionField(field, GraphQLJpaSchemaBuilder.PAGE_PAGES_PARAM_NAME);
        Optional<Field> totalSelection = getSelectionField(field, GraphQLJpaSchemaBuilder.PAGE_TOTAL_PARAM_NAME);
        Optional<Field> recordsSelection = getSelectionField(field, GraphQLJpaSchemaBuilder.QUERY_SELECT_PARAM_NAME);

        Argument distinctArg = extractArgument(environment, field, GraphQLJpaSchemaBuilder.SELECT_DISTINCT_PARAM_NAME, new BooleanValue(defaultDistinct));
        boolean isDistinct = ((BooleanValue) distinctArg.getValue()).isValue();
        
        DataFetchingEnvironment queryEnvironment = environment;
        Field queryField = field;
        
        if (recordsSelection.isPresent()) {
            // Override query environment  
            String fieldName = recordsSelection.get().getName();
            
            queryEnvironment = 
                Optional.of(getFieldDefinition(environment.getGraphQLSchema(), (GraphQLObjectType)environment.getParentType(), field))
                    .map(it -> (GraphQLObjectType) it.getType())
                    .map(it -> it.getFieldDefinition(GraphQLJpaSchemaBuilder.QUERY_SELECT_PARAM_NAME))
                    .map(it -> DataFetchingEnvironmentBuilder.newDataFetchingEnvironment(environment)
                                                             .fieldType(it.getType())
                                                             .build()
                    ).orElse(environment);
            
            queryField = Field.newField(fieldName, recordsSelection.get().getSelectionSet())
                              .arguments(field.getArguments())
                              .directives(recordsSelection.get().getDirectives())
                              .build();
            
            final List<Object> keys = queryKeys(queryEnvironment, queryField, pageArgument);

            if(!keys.isEmpty()) {
                // Let's execute query and get result as stream
                Stream<Object> resultStream = queryResultStream(queryEnvironment, queryField, isDistinct, page, keys);
    
                // Let's wrap stream into lazy list to pass it downstream
                List<Object> resultList = ResultStreamWrapper.wrap(resultStream.peek(entityManager::detach), 
                                                                   page.getLimit());
                
                result.withSelect(resultList);
            }
        }
        
        if (totalSelection.isPresent() || pagesSelection.isPresent()) {
            final Long total = queryTotalCount(queryEnvironment, queryField, recordsSelection);
            final Long pages = ((Double) Math.ceil(total / (double) page.getLimit())).longValue();

            result.withPages(pages)
                  .withTotal(total);
        }

        return result.build();
    }

    protected List<Object> queryKeys(DataFetchingEnvironment queryEnvironment, Field queryField, Optional<Argument> pageArgument ) {
        Page page = extractPageArgument(queryEnvironment, pageArgument, defaultPageLimitSize);
        
        TypedQuery<Object> keysQuery = getKeysQuery(queryEnvironment, queryField);

        // Let's apply page only if present
        if(pageArgument.isPresent()) {
            keysQuery
                .setMaxResults(page.getLimit())
                .setFirstResult((page.getStart() - 1) * page.getLimit());
        } // Limit max results to avoid OoM 
        else {
            keysQuery.setMaxResults(defaultMaxResults); 
        }

        if (logger.isDebugEnabled()) {
            logger.info("\nGraphQL JPQL Keys Query String:\n    {}", getJPQLQueryString(keysQuery));
        }
        
        return keysQuery.getResultList();
    }
    
    protected Stream<Object> queryResultStream(DataFetchingEnvironment queryEnvironment, Field queryField, Boolean isDistinct, Page page, List<Object> keys) {
        TypedQuery<Object> query = getQuery(queryEnvironment, queryField, isDistinct, keys.toArray()); 
        // Let' try reduce overhead and disable all caching
        query.setHint(ORG_HIBERNATE_READ_ONLY, true);
        query.setHint(ORG_HIBERNATE_FETCH_SIZE, Integer.min(page.getLimit(), defaultFetchSize)); 
        query.setHint(ORG_HIBERNATE_CACHEABLE, false);
        
        // Let's not pass distinct if enabled to have better performance
        if(isDistinct) {
            query.setHint(HIBERNATE_QUERY_PASS_DISTINCT_THROUGH, false);
        }

        if (logger.isDebugEnabled()) {
            logger.info("\nGraphQL JPQL Fetch Query String:\n    {}", getJPQLQueryString(query));
        }

        // Let's execute query and get wrap result into stream
        return query.getResultStream();                
    }
    
    protected Long queryTotalCount(DataFetchingEnvironment queryEnvironment, Field queryField, Optional<Field> recordsSelection) {

        TypedQuery<Long> countQuery = recordsSelection.map(contentField -> getCountQuery(queryEnvironment, queryField))
                                                      .orElseGet(() -> getCountQuery(queryEnvironment, new Field("total")));
        if (logger.isDebugEnabled()) {
            logger.info("\nGraphQL JPQL Count Query String:\n    {}", getJPQLQueryString(countQuery));
        }
        
        return countQuery.getSingleResult();
    }
    
    @Override
    protected Predicate getPredicate(CriteriaBuilder cb, Root<?> root, From<?,?> path, DataFetchingEnvironment environment, Argument argument) {
        if(isLogicalArgument(argument) || isDistinctArgument(argument))
            return null;
        
        if(isWhereArgument(argument)) 
            return getWherePredicate(cb, root, path, argumentEnvironment(environment, argument), argument);
        
        return super.getPredicate(cb, root, path, environment, argument);
    }
    
    public int getDefaultMaxResults() {
        return defaultMaxResults;
    }
    
    public int getDefaultFetchSize() {
        return defaultFetchSize;
    }

    public int getDefaultPageLimitSize() {
        return defaultPageLimitSize;
    }

    /**
     * Creates builder to build {@link GraphQLJpaQueryDataFetcher}.
     * @return created builder
     */
    public static IEntityManagerStage builder() {
        return new Builder();
    }

    public interface IEntityManagerStage {

        public IEntityTypeStage withEntityManager(EntityManager entityManager);
    }

    public interface IEntityTypeStage {

        public IToManyDefaultOptionalStage withEntityType(EntityType<?> entityType);
    }

    public interface IToManyDefaultOptionalStage {

        public IBuildStage withToManyDefaultOptional(boolean toManyDefaultOptional);
    }

    public interface IBuildStage {

        public IBuildStage withDefaultDistinct(boolean defaultDistinct);

        public IBuildStage withDefaultMaxResults(int defaultMaxResults);

        public IBuildStage withDefaultFetchSize(int defaultFetchSize);

        public IBuildStage withDefaultPageLimitSize(int defaultPageLimitSize);

        public GraphQLJpaQueryDataFetcher build();
    }

    /**
     * Builder to build {@link GraphQLJpaQueryDataFetcher}.
     */
    public static final class Builder implements IEntityManagerStage, IEntityTypeStage, IToManyDefaultOptionalStage, IBuildStage {

        private EntityManager entityManager;
        private EntityType<?> entityType;
        private boolean toManyDefaultOptional;
        private boolean defaultDistinct;
        private int defaultMaxResults;
        private int defaultFetchSize;
        private int defaultPageLimitSize;

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
        public IBuildStage withToManyDefaultOptional(boolean toManyDefaultOptional) {
            this.toManyDefaultOptional = toManyDefaultOptional;
            return this;
        }

        @Override
        public IBuildStage withDefaultDistinct(boolean defaultDistinct) {
            this.defaultDistinct = defaultDistinct;
            return this;
        }

        @Override
        public IBuildStage withDefaultMaxResults(int defaultMaxResults) {
            this.defaultMaxResults = defaultMaxResults;
            return this;
        }

        @Override
        public IBuildStage withDefaultFetchSize(int defaultFetchSize) {
            this.defaultFetchSize = defaultFetchSize;
            return this;
        }

        @Override
        public IBuildStage withDefaultPageLimitSize(int defaultPageLimitSize) {
            this.defaultPageLimitSize = defaultPageLimitSize;
            return this;
        }

        @Override
        public GraphQLJpaQueryDataFetcher build() {
            return new GraphQLJpaQueryDataFetcher(this);
        }
    }
    
}    

