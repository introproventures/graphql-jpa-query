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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.metamodel.EntityType;

import graphql.language.Argument;
import graphql.language.Field;
import graphql.language.ObjectValue;
import graphql.schema.DataFetchingEnvironment;

class GraphQLJpaSimpleDataFetcher extends QraphQLJpaBaseDataFetcher {

    public GraphQLJpaSimpleDataFetcher(EntityManager entityManager, 
                                       EntityType<?> entityType,
                                       boolean toManyDefaultOptional) {
        super(entityManager, entityType, toManyDefaultOptional);
    }
    
    @Override
    public Object get(DataFetchingEnvironment environment) {
        
        Field field = environment.getField();

        if(!field.getArguments().isEmpty()) {
            
            field = flattenEmbeddedIdArguments(field);
            
            try {
                return getQuery(environment, field, true).getSingleResult();
                
            } catch (NoResultException ignored) {
                // do nothing
            }
            
        }

        return null;
    }

	private Field flattenEmbeddedIdArguments(Field field) {
		// manage object arguments (EmbeddedId)
		final List<Argument> argumentsWhereObjectsAreFlattened = field.getArguments()
				.stream()
				.flatMap(argument ->
				{
					if (!argument.getName().equals("where") && !argument.getName().equals("page") &&
							argument.getValue() instanceof ObjectValue) {
						return ((ObjectValue) argument.getValue()).getObjectFields()
								.stream()
								.map(objectField -> new Argument(argument.getName() + "." + objectField.getName(), objectField.getValue()));
					} else {
						return Stream.of(argument);
					}
				})
				.collect(Collectors.toList());
		return field.transform(builder ->
        builder.arguments(argumentsWhereObjectsAreFlattened));
	}
}
