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

import static com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder.PAGE_PAGES_PARAM_NAME;
import static com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder.PAGE_TOTAL_PARAM_NAME;
import static com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder.QUERY_SELECT_PARAM_NAME;
import static com.introproventures.graphql.jpa.query.support.GraphQLSupport.extractPageArgument;
import static com.introproventures.graphql.jpa.query.support.GraphQLSupport.findArgument;
import static com.introproventures.graphql.jpa.query.support.GraphQLSupport.getAliasOrName;
import static com.introproventures.graphql.jpa.query.support.GraphQLSupport.getFields;
import static com.introproventures.graphql.jpa.query.support.GraphQLSupport.getPageArgument;
import static com.introproventures.graphql.jpa.query.support.GraphQLSupport.getSelectionField;
import static com.introproventures.graphql.jpa.query.support.GraphQLSupport.searchByFieldName;

import com.introproventures.graphql.jpa.query.schema.JavaScalars;
import graphql.GraphQLException;
import graphql.language.Argument;
import graphql.language.EnumValue;
import graphql.language.Field;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLScalarType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JPA Query DataFetcher implementation that fetches entities with page and where criteria expressions
 *
 * @author Igor Dianov
 *
 */
class GraphQLJpaQueryDataFetcher implements DataFetcher<PagedResult<Object>> {

    private static final Logger logger = LoggerFactory.getLogger(GraphQLJpaQueryDataFetcher.class);

    private final int defaultMaxResults;
    private final int defaultPageLimitSize;
    private final boolean enableDefaultMaxResults;
    private final GraphQLJpaQueryFactory queryFactory;

    private GraphQLJpaQueryDataFetcher(Builder builder) {
        this.queryFactory = builder.queryFactory;
        this.defaultMaxResults = builder.defaultMaxResults;
        this.defaultPageLimitSize = builder.defaultPageLimitSize;
        this.enableDefaultMaxResults = builder.enableDefaultMaxResults;
    }

    @Override
    public PagedResult<Object> get(DataFetchingEnvironment environment) {
        final Field rootNode = environment.getField();
        final Optional<Argument> pageArgument = getPageArgument(environment.getField());
        final PageArgument page = extractPageArgument(environment, pageArgument, defaultPageLimitSize);

        // Let's see which fields we're requesting
        Optional<Field> pagesSelection = getSelectionField(rootNode, PAGE_PAGES_PARAM_NAME);
        Optional<Field> totalSelection = getSelectionField(rootNode, PAGE_TOTAL_PARAM_NAME);
        Optional<Field> recordsSelection = searchByFieldName(rootNode, QUERY_SELECT_PARAM_NAME);
        Optional<Field> aggregateSelection = getSelectionField(rootNode, "aggregate");

        final int firstResult = page.getOffset();
        final int maxResults = Integer.min(page.getLimit(), defaultMaxResults); // Limit max results to avoid OoM

        final PagedResult.Builder<Object> pagedResult = PagedResult
            .builder()
            .withOffset(firstResult)
            .withLimit(maxResults);
        Optional<List<Object>> restrictedKeys = queryFactory.getRestrictedKeys(environment);

        if (recordsSelection.isPresent()) {
            if (restrictedKeys.isPresent()) {
                final List<Object> queryKeys = new ArrayList<>();

                if (pageArgument.isPresent() || enableDefaultMaxResults) {
                    queryKeys.addAll(
                        queryFactory.queryKeys(environment, firstResult, maxResults, restrictedKeys.get())
                    );
                } else {
                    queryKeys.addAll(restrictedKeys.get());
                }

                final List<Object> resultList = queryFactory.queryResultList(environment, maxResults, queryKeys);
                pagedResult.withSelect(resultList);
            }
        }

        if (totalSelection.isPresent() || pagesSelection.isPresent()) {
            final Long total = queryFactory.queryTotalCount(environment, restrictedKeys);

            pagedResult.withTotal(total);
        }

        aggregateSelection.ifPresent(aggregateField -> {
            Map<String, Object> aggregate = new LinkedHashMap<>();

            getFields(aggregateField.getSelectionSet(), "count")
                .forEach(countField -> {
                    getCountOfArgument(countField)
                        .ifPresentOrElse(
                            argument ->
                                aggregate.put(
                                    getAliasOrName(countField),
                                    queryFactory.queryAggregateCount(argument, environment, restrictedKeys)
                                ),
                            () ->
                                aggregate.put(
                                    getAliasOrName(countField),
                                    queryFactory.queryTotalCount(environment, restrictedKeys)
                                )
                        );
                });

            getFields(aggregateField.getSelectionSet(), "group")
                .forEach(groupField -> {
                    var countField = getFields(groupField.getSelectionSet(), "count")
                        .stream()
                        .findFirst()
                        .orElseThrow(() -> new GraphQLException("Missing aggregate count for group: " + groupField));

                    var countOfArgumentValue = getCountOfArgument(groupField);

                    Map.Entry<String, String>[] groupings = getFields(groupField.getSelectionSet(), "by")
                        .stream()
                        .map(GraphQLJpaQueryDataFetcher::groupByFieldEntry)
                        .toArray(Map.Entry[]::new);

                    if (groupings.length == 0) {
                        throw new GraphQLException("At least one field is required for aggregate group: " + groupField);
                    }

                    var resultList = queryFactory
                        .queryAggregateGroupByCount(
                            getAliasOrName(countField),
                            countOfArgumentValue,
                            environment,
                            restrictedKeys,
                            groupings
                        )
                        .stream()
                        .peek(map ->
                            Stream
                                .of(groupings)
                                .forEach(group -> {
                                    var value = map.get(group.getKey());

                                    Optional
                                        .ofNullable(value)
                                        .map(Object::getClass)
                                        .map(JavaScalars::of)
                                        .map(GraphQLScalarType::getCoercing)
                                        .ifPresent(coercing -> map.put(group.getKey(), coercing.serialize(value)));
                                })
                        )
                        .toList();

                    aggregate.put(getAliasOrName(groupField), resultList);
                });

            aggregateField
                .getSelectionSet()
                .getSelections()
                .stream()
                .filter(Field.class::isInstance)
                .map(Field.class::cast)
                .filter(it -> !Arrays.asList("count", "group").contains(it.getName()))
                .forEach(groupField -> {
                    var countField = getFields(groupField.getSelectionSet(), "count")
                        .stream()
                        .findFirst()
                        .orElseThrow(() -> new GraphQLException("Missing aggregate count for group: " + groupField));

                    Map.Entry<String, String>[] groupings = getFields(groupField.getSelectionSet(), "by")
                        .stream()
                        .map(GraphQLJpaQueryDataFetcher::groupByFieldEntry)
                        .toArray(Map.Entry[]::new);

                    if (groupings.length == 0) {
                        throw new GraphQLException("At least one field is required for aggregate group: " + groupField);
                    }

                    var resultList = queryFactory
                        .queryAggregateGroupByAssociationCount(
                            getAliasOrName(countField),
                            groupField.getName(),
                            environment,
                            restrictedKeys,
                            groupings
                        )
                        .stream()
                        .peek(map ->
                            Stream
                                .of(groupings)
                                .forEach(group -> {
                                    var value = map.get(group.getKey());

                                    Optional
                                        .ofNullable(value)
                                        .map(Object::getClass)
                                        .map(JavaScalars::of)
                                        .map(GraphQLScalarType::getCoercing)
                                        .ifPresent(coercing -> map.put(group.getKey(), coercing.serialize(value)));
                                })
                        )
                        .toList();

                    aggregate.put(getAliasOrName(groupField), resultList);
                });

            pagedResult.withAggregate(aggregate);
        });

        return pagedResult.build();
    }

    static Map.Entry<String, String> groupByFieldEntry(Field selectedField) {
        String key = Optional.ofNullable(selectedField.getAlias()).orElse(selectedField.getName());

        String value = findArgument(selectedField, "field")
            .map(Argument::getValue)
            .map(EnumValue.class::cast)
            .map(EnumValue::getName)
            .orElseThrow(() -> new GraphQLException("group by argument is required."));

        return Map.entry(key, value);
    }

    static Map.Entry<String, String> countFieldEntry(Field selectedField) {
        String key = Optional.ofNullable(selectedField.getAlias()).orElse(selectedField.getName());

        String value = getCountOfArgument(selectedField).orElse(selectedField.getName());

        return Map.entry(key, value);
    }

    static Optional<String> getCountOfArgument(Field selectedField) {
        return findArgument(selectedField, "of")
            .map(Argument::getValue)
            .map(EnumValue.class::cast)
            .map(EnumValue::getName);
    }

    public int getDefaultMaxResults() {
        return defaultMaxResults;
    }

    public int getDefaultPageLimitSize() {
        return defaultPageLimitSize;
    }

    /**
     * Creates builder to build {@link GraphQLJpaQueryDataFetcher}.
     * @return created builder
     */
    public static IQueryFactoryStage builder() {
        return new Builder();
    }

    /**
     * Definition of a stage for staged builder.
     */
    public interface IQueryFactoryStage {
        /**
         * Builder method for queryFactory parameter.
         * @param queryFactory field to set
         * @return builder
         */
        public IDefaultMaxResultsStage withQueryFactory(GraphQLJpaQueryFactory queryFactory);
    }

    /**
     * Definition of a stage for staged builder.
     */
    public interface IDefaultMaxResultsStage {
        /**
         * Builder method for defaultMaxResults parameter.
         * @param defaultMaxResults field to set
         * @return builder
         */
        public IDefaultMaxResultsStage withDefaultMaxResults(int defaultMaxResults);

        /**
         * Builder method for enableDefaultMaxResults parameter.
         * @param enableDefaultMaxResults field to set
         * @return builder
         */
        public IDefaultPageLimitSizeStage withEnableDefaultMaxResults(boolean enableDefaultMaxResults);
    }

    /**
     * Definition of a stage for staged builder.
     */
    public interface IDefaultPageLimitSizeStage {
        /**
         * Builder method for defaultPageLimitSize parameter.
         * @param defaultPageLimitSize field to set
         * @return builder
         */
        public IBuildStage withDefaultPageLimitSize(int defaultPageLimitSize);
    }

    /**
     * Definition of a stage for staged builder.
     */
    public interface IBuildStage {
        /**
         * Builder method of the builder.
         * @return built class
         */
        public GraphQLJpaQueryDataFetcher build();
    }

    /**
     * Builder to build {@link GraphQLJpaQueryDataFetcher}.
     */
    public static final class Builder
        implements IQueryFactoryStage, IDefaultMaxResultsStage, IDefaultPageLimitSizeStage, IBuildStage {

        private GraphQLJpaQueryFactory queryFactory;
        private int defaultMaxResults;
        private int defaultPageLimitSize;
        private boolean enableDefaultMaxResults;

        private Builder() {}

        @Override
        public IDefaultMaxResultsStage withQueryFactory(GraphQLJpaQueryFactory queryFactory) {
            this.queryFactory = queryFactory;
            return this;
        }

        @Override
        public IDefaultMaxResultsStage withDefaultMaxResults(int defaultMaxResults) {
            this.defaultMaxResults = defaultMaxResults;
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

        @Override
        public IDefaultPageLimitSizeStage withEnableDefaultMaxResults(boolean enableDefaultMaxResults) {
            this.enableDefaultMaxResults = enableDefaultMaxResults;
            return this;
        }
    }
}
