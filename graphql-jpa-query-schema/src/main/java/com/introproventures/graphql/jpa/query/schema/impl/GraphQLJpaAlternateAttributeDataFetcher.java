/*
 * Copyright IBM Corporation 2018
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

import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.PluralAttribute;

import com.introproventures.graphql.jpa.query.schema.IQueryAuthorizationStrategy;
import com.introproventures.graphql.jpa.query.schema.exception.AuthorizationException;
import graphql.schema.DataFetchingEnvironment;

/**
 * Similar to PropertyDataFetcher, this uses getters to retrieve attribute data, allowing JPA to determine how to construct
 * the SQL based on annotations in the domain model. The attributes thus retrieved are subject to the authorization on the attribute,
 * if specified in the schema.
 * <p>
 * This is useful for retrieving associations that are marked with BatchFetch in Eclipselink
 *
 * @author Ghada Obaid
 */
class GraphQLJpaAlternateAttributeDataFetcher extends GraphQLJpaOneToManyDataFetcher {

    public GraphQLJpaAlternateAttributeDataFetcher(EntityManager entityManager, EntityType<?> entityType, PluralAttribute<Object, Object, Object> attribute,
                                                   IQueryAuthorizationStrategy authorizationStrategy) {
        super(entityManager, entityType, attribute, authorizationStrategy);
    }

    @Override
    public Object get(DataFetchingEnvironment environment) {
        authorization.checkAuthorization(environment);

        Object source = environment.getSource();

        return getAttributeValue(source, attribute);
    }
}
