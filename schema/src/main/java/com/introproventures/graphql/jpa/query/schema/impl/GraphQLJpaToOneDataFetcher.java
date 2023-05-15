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
import graphql.language.Argument;
import graphql.language.Field;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLNamedType;
import jakarta.persistence.metamodel.SingularAttribute;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderOptions;
import org.dataloader.DataLoaderRegistry;
import org.dataloader.MappedBatchLoaderWithContext;

import static jakarta.persistence.metamodel.Attribute.PersistentAttributeType.EMBEDDED;

/**
 * One-To-Many DataFetcher that uses where argument to filter collection attributes
 *
 * @author Igor Dianov
 *
 */
class GraphQLJpaToOneDataFetcher implements DataFetcher<Object> {

    private final SingularAttribute<Object,Object> attribute;
    private final GraphQLJpaQueryFactory queryFactory;

    public GraphQLJpaToOneDataFetcher(GraphQLJpaQueryFactory queryFactory,
                                      SingularAttribute<Object,Object> attribute) {
        this.queryFactory = queryFactory;
        this.attribute = attribute;
    }

    @Override
    public Object get(DataFetchingEnvironment environment) {
        Field field = environment.getField();
        GraphQLNamedType parentType = (GraphQLNamedType) environment.getParentType();

        Object source = environment.getSource();
        Optional<Argument> whereArgument = queryFactory.getArgument(field, "where");
        Boolean isOptional = queryFactory.getOptionalArgumentValue(environment,
                                                                   field,
                                                                   attribute);
        // Resolve collection query if where argument is present
        if ((whereArgument.isPresent() && isOptional) && !EMBEDDED.equals(attribute.getPersistentAttributeType())) {
            Object parentIdValue = queryFactory.getParentIdAttributeValue(source);
            String dataLoaderKey = parentType.getName() + "." + Optional.ofNullable(field.getAlias())
                                                                        .orElseGet(attribute::getName);

            DataLoader<Object, Object> dataLoader = getDataLoader(environment,
                                                                  dataLoaderKey);

            return dataLoader.load(parentIdValue, environment);
        }

        // Let hibernate resolve collection query
        return queryFactory.getAttributeValue(source,
                                              attribute);
    }

    protected DataLoader<Object, Object> getDataLoader(DataFetchingEnvironment environment,
                                                       String dataLoaderKey) {
        DataLoaderRegistry dataLoaderRegistry = environment.getDataLoaderRegistry();

        if (!dataLoaderRegistry.getKeys()
                               .contains(dataLoaderKey)) {
            synchronized (dataLoaderRegistry) {
                MappedBatchLoaderWithContext<Object, Object> mappedBatchLoader = new GraphQLJpaToOneMappedBatchLoader(queryFactory);

                DataLoaderOptions options = DataLoaderOptions.newOptions()
                                                             .setCachingEnabled(false);

                DataLoader<Object, Object> dataLoader = DataLoader.newMappedDataLoader(mappedBatchLoader,
                                                                                       options);
                dataLoaderRegistry.register(dataLoaderKey, dataLoader);
            }
        }

        return environment.getDataLoader(dataLoaderKey);
    }

}
