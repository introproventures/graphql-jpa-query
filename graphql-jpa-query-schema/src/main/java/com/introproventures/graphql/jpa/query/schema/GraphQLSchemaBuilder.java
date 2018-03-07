/*
 * Copyright 2017 IntroPro Ventures Inc. and/or its affiliates.
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

import java.lang.reflect.AnnotatedElement;

import com.introproventures.graphql.jpa.query.annotation.GraphQLIgnore;
import graphql.schema.GraphQLSchema;

/**
 * GraphQL Schema Builder interface specification
 *
 * @author Igor Dianov
 *
 */
public interface GraphQLSchemaBuilder {

    /**
     * Configures the name of GraphQL Schema. Cannot be null or empty;
     *
     * @param value GraphQL schema name
     * @return this builder instance
     */
    GraphQLSchemaBuilder name(String value);

    /**
     * Configures the description of GraphQL Schema.
     *
     * @param value GraphQL schema description
     * @return this builder instance
     */
    GraphQLSchemaBuilder description(String value);

    /**
     * Add package path to scan for entities to be included in GraphQL Schema.
     * All Entities are included by default
     *
     * @param path GraphQL entitys package path
     * @return this builder instance
     */
    GraphQLSchemaBuilder entityPath(String path);

    /**
     * Add package path to scan for entities to be included in GraphQL Schema.
     *
     * @param instance GraphQL query naming strategy
     * @return this builder instance
     */
    GraphQLSchemaBuilder namingStrategy(NamingStrategy instance);

    /**
     * Builds {code #GraphQLSchema} instance
     *
     * @return GraphQLSchema instance
     */
    GraphQLSchema build();

    /**
     * Define the READ authorization strategy
     *
     * @param instance GraphQL query authorization strategy
     * @return this builder instance
     */
    GraphQLSchemaBuilder readAuthorizationStrategy(IQueryAuthorizationStrategy instance);

    /**
     * Determines whether an entity or attribute is ignored. This can be overridden to use an alternate
     * mechanism, such as JsonIgnore from Jackson, or a combination of the 2
     *
     * @param annotatedElement
     * @return true, if the entity or attribute is not ignored; false otherwise
     */
    default boolean isNotIgnored(AnnotatedElement annotatedElement) {
        if (annotatedElement != null) {
            GraphQLIgnore ignoredAnnotation = annotatedElement.getAnnotation(GraphQLIgnore.class);
            return ignoredAnnotation == null;
        }

        return false;
    }

    default boolean useAlternateDataFetchers() {
        return false;
    }
}