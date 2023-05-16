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

import graphql.language.Field;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import jakarta.persistence.NoResultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class GraphQLJpaSimpleDataFetcher implements DataFetcher<Object> {

    private static final Logger logger = LoggerFactory.getLogger(GraphQLJpaSimpleDataFetcher.class);

    private final GraphQLJpaQueryFactory queryFactory;

    private GraphQLJpaSimpleDataFetcher(Builder builder) {
        this.queryFactory = builder.queryFactory;
    }

    @Override
    public Object get(DataFetchingEnvironment environment) {
        Field field = environment.getField();

        if (!field.getArguments().isEmpty()) {
            try {
                return queryFactory.querySingleResult(environment);
            } catch (NoResultException ignored) {
                // do nothing
            }
        }

        return null;
    }

    /**
     * Creates builder to build {@link GraphQLJpaSimpleDataFetcher}.
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
        public GraphQLJpaSimpleDataFetcher build();
    }

    /**
     * Builder to build {@link GraphQLJpaSimpleDataFetcher}.
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
        public GraphQLJpaSimpleDataFetcher build() {
            return new GraphQLJpaSimpleDataFetcher(this);
        }
    }
}
