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

import static com.introproventures.graphql.jpa.query.support.GraphQLSupport.extractPageArgument;
import static com.introproventures.graphql.jpa.query.support.GraphQLSupport.getPageArgument;
import static com.introproventures.graphql.jpa.query.support.GraphQLSupport.removeArgument;

import graphql.language.Argument;
import graphql.language.Field;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

/**
 * JPA Query DataFetcher implementation that fetches entities with page and where criteria expressions
 *
 * @author Igor Dianov
 *
 */
class GraphQLJpaStreamDataFetcher implements DataFetcher<Object> {

    private static final Logger logger = LoggerFactory.getLogger(GraphQLJpaStreamDataFetcher.class);

    private final GraphQLJpaQueryFactory queryFactory;

    private GraphQLJpaStreamDataFetcher(Builder builder) {
        this.queryFactory = builder.queryFactory;
    }

    @Override
    public Object get(DataFetchingEnvironment environment) {
        Field field = environment.getField();

        Optional<Argument> pageArgument = getPageArgument(field);
        PageArgument page = extractPageArgument(environment, pageArgument, 100);
        field = removeArgument(field, pageArgument);

        // Let's execute query and get results via stream
        Stream<Object> resultStream = queryFactory.queryResultStream(environment, 100, Collections.emptyList());

        return Flux.fromIterable(ResultStreamWrapper.wrap(resultStream, page.getLimit()));
    }

    /**
     * Creates builder to build {@link GraphQLJpaStreamDataFetcher}.
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
        public IBuildStage withQueryFactory(GraphQLJpaQueryFactory queryFactory);
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
    public static final class Builder implements IQueryFactoryStage, IBuildStage {

        private GraphQLJpaQueryFactory queryFactory;

        private Builder() {}

        @Override
        public IBuildStage withQueryFactory(GraphQLJpaQueryFactory queryFactory) {
            this.queryFactory = queryFactory;
            return this;
        }

        @Override
        public GraphQLJpaStreamDataFetcher build() {
            return new GraphQLJpaStreamDataFetcher(this);
        }
    }
}
