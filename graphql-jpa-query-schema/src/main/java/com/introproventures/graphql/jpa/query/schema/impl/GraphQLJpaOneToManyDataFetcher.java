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

import org.dataloader.DataLoader;

import com.introproventures.graphql.jpa.query.support.GraphQLSupport;
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
class GraphQLJpaOneToManyDataFetcher implements DataFetcher<Object> {

    private final PluralAttribute<Object,Object,Object> attribute;
    private final GraphQLJpaQueryFactory queryFactory;

    public GraphQLJpaOneToManyDataFetcher(GraphQLJpaQueryFactory queryFactory,
                                          PluralAttribute<Object,Object,Object> attribute) {
        this.queryFactory = queryFactory;
        this.attribute = attribute;
    }

    @Override
    public Object get(DataFetchingEnvironment environment) {
        Field field = environment.getField();
        GraphQLType parentType = environment.getParentType();

        Object source = environment.getSource();
        Optional<Argument> whereArg = GraphQLSupport.getWhereArgument(field);

        // Resolve collection query if where argument is present
        // TODO or any field in selection has orderBy argument
        if (whereArg.isPresent()) {
            Object parentIdValue = queryFactory.getParentIdAttributeValue(source);
            String dataLoaderKey = parentType.getName() + "." + attribute.getName();

            DataLoader<Object, List<Object>> dataLoader = environment.getDataLoader(dataLoaderKey);

            return dataLoader.load(parentIdValue, environment);
        }

        // Let hibernate resolve collection query
        return queryFactory.getAttributeValue(source,
                                              attribute);
    }

}
