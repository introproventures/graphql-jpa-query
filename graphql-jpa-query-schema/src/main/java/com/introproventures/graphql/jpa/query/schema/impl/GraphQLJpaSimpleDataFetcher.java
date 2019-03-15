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

import graphql.language.Argument;
import graphql.language.Field;
import graphql.language.ObjectValue;
import graphql.schema.DataFetchingEnvironment;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class GraphQLJpaSimpleDataFetcher extends QraphQLJpaBaseDataFetcher {

    public GraphQLJpaSimpleDataFetcher(EntityManager entityManager, EntityType<?> entityType) {
        super(entityManager, entityType);
    }
    
    @Override
    public Object get(DataFetchingEnvironment environment) {
        
        Field field = environment.getFields().iterator().next();

        if(!field.getArguments().isEmpty()) {
            
            flattenEmbeddedIdArguments(field);
            
            try {
                // Create entity graph from selection
                EntityGraph<?> entityGraph = buildEntityGraph(field);
                
                return super.getQuery(environment, field, true)
                    .setHint("javax.persistence.fetchgraph", entityGraph)
                    .getSingleResult();
                
            } catch (NoResultException ignored) {
                // do nothing
            }
            
        }

        return null;
    }

	private void flattenEmbeddedIdArguments(Field field) {
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
		field.setArguments(argumentsWhereObjectsAreFlattened);
	}
}
