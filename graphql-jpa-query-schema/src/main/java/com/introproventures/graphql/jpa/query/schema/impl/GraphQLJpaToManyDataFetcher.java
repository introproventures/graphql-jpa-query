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

import javax.persistence.metamodel.PluralAttribute;

import graphql.schema.GraphQLNamedType;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderOptions;
import org.dataloader.DataLoaderRegistry;
import org.dataloader.MappedBatchLoaderWithContext;

import com.introproventures.graphql.jpa.query.support.GraphQLSupport;
import graphql.GraphQLContext;
import graphql.language.Argument;
import graphql.language.Field;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLType;

/**
 * One-To-Many DataFetcher that uses where argument to filter collection attributes
 *
 * @author Igor Dianov
 *
 */
class GraphQLJpaToManyDataFetcher implements DataFetcher<Object> {

    private final PluralAttribute<Object,Object,Object> attribute;
    private final GraphQLJpaQueryFactory queryFactory;

    public GraphQLJpaToManyDataFetcher(GraphQLJpaQueryFactory queryFactory,
                                          PluralAttribute<Object,Object,Object> attribute) {
        this.queryFactory = queryFactory;
        this.attribute = attribute;
    }

    @Override
    public Object get(DataFetchingEnvironment environment) {
        Field field = environment.getField();
        GraphQLNamedType parentType = (GraphQLNamedType) environment.getParentType();

        Object source = environment.getSource();
        Optional<Argument> whereArg = GraphQLSupport.getWhereArgument(field);

        // Resolve collection query if where argument is present or any field in selection has orderBy argument
        if (whereArg.isPresent() || queryFactory.hasAnySelectionOrderBy(field)) {
            Object parentIdValue = queryFactory.getParentIdAttributeValue(source);
            String dataLoaderKey = parentType.getName() + "." + Optional.ofNullable(field.getAlias())
                                                                        .orElseGet(attribute::getName);

            DataLoader<Object, List<Object>> dataLoader = getDataLoader(environment,
                                                                        dataLoaderKey);

            return dataLoader.load(parentIdValue, environment);
        }

        // Let hibernate resolve collection query
        return queryFactory.getAttributeValue(source,
                                              attribute);
    }

    protected DataLoader<Object, List<Object>> getDataLoader(DataFetchingEnvironment environment,
                                                             String dataLoaderKey) {
        GraphQLContext context = environment.getContext();
        DataLoaderRegistry dataLoaderRegistry = context.getOrDefault("dataLoaderRegistry",
                                                                     environment.getDataLoaderRegistry());

        if (!dataLoaderRegistry.getKeys()
                              .contains(dataLoaderKey)) {
            synchronized (dataLoaderRegistry) {
                MappedBatchLoaderWithContext<Object, List<Object>> mappedBatchLoader = new GraphQLJpaToManyMappedBatchLoader(queryFactory);

                DataLoaderOptions options = DataLoaderOptions.newOptions()
                                                             .setCachingEnabled(false);

                DataLoader<Object, List<Object>> dataLoader = DataLoader.newMappedDataLoader(mappedBatchLoader,
                                                                                             options);
                dataLoaderRegistry.register(dataLoaderKey, dataLoader);
            }
        }

        return  dataLoaderRegistry.getDataLoader(dataLoaderKey);
    }

}
