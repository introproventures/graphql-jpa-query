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

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.metamodel.EntityType;

import com.introproventures.graphql.jpa.query.schema.IQueryAuthorizationStrategy;
import com.introproventures.graphql.jpa.query.schema.exception.AuthorizationException;
import graphql.language.Field;
import graphql.schema.DataFetchingEnvironment;

class GraphQLJpaSimpleDataFetcher extends QraphQLJpaBaseDataFetcher {

    public GraphQLJpaSimpleDataFetcher(EntityManager entityManager, EntityType<?> entityType, IQueryAuthorizationStrategy authorizationStrategy) {
        super(entityManager, entityType, authorizationStrategy);
    }

    @Override
    public Object get(DataFetchingEnvironment environment) {
        if (authorization != null && !authorization.isAuthorized(entityType))
            throw new AuthorizationException();

        Field field = environment.getFields().iterator().next();

        if (!field.getArguments().isEmpty()) {

            try {
                // Create entity graph from selection
                EntityGraph<?> entityGraph = buildEntityGraph(field);

                //There should not be a need for select DISTINCT when fetching by ID - turning it off as it will fail for entities with byte arrays that hold blobs
                return super.getQuery(environment, field, false)
                        .setHint("javax.persistence.fetchgraph", entityGraph)
                        .getSingleResult();

            } catch (NoResultException ignored) {
                // do nothing
            }

        }

        return null;
    }
}
