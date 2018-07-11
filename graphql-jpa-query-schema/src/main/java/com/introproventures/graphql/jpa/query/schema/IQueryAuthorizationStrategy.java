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
package com.introproventures.graphql.jpa.query.schema;

import com.introproventures.graphql.jpa.query.schema.exception.AuthorizationException;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLDirective;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import java.util.List;

/**
 * Interface for defining authorization to various data returned through the defined schem
 * <p>
 * By default, there is no authorization enforced for the data
 *
 * @author Ghada Obaid
 */
public interface IQueryAuthorizationStrategy {
    static final String AUTHORIZATION = "authorization";

    default boolean isAuthorized(Object context, GraphQLDirective authDirective) {
        return true;
    }

    default void checkAuthorization(DataFetchingEnvironment environment) {
        GraphQLDirective authDirective = environment.getFieldDefinition().getDirective(IQueryAuthorizationStrategy.AUTHORIZATION);
        if (authDirective != null && !isAuthorized(environment.getContext(), authDirective))
            throw new AuthorizationException();
    }
}
