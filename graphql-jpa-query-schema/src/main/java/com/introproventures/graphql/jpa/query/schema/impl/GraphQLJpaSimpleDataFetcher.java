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
import javax.persistence.TypedQuery;
import javax.persistence.metamodel.EntityType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graphql.language.Argument;
import graphql.language.Field;
import graphql.language.ObjectValue;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

class GraphQLJpaSimpleDataFetcher extends GraphQLJpaBaseDataFetcher implements DataFetcher<Object> {
    private static final Logger logger = LoggerFactory.getLogger(GraphQLJpaSimpleDataFetcher.class);

    private GraphQLJpaSimpleDataFetcher(Builder builder) {
        super(builder.entityManager, 
              builder.entityType, 
              builder.toManyDefaultOptional);
    }

    @Override
    public Object get(DataFetchingEnvironment environment) {
        Field field = environment.getField();

        if(!field.getArguments().isEmpty()) {
            field = flattenEmbeddedIdArguments(field);
            
            try {
                return querySingleResult(environment, field);
            } catch (NoResultException ignored) {
                // do nothing
            }
        }

        return null;
    }
    
    protected Object querySingleResult(DataFetchingEnvironment environment, Field field) {
        TypedQuery<Object> query = getQuery(environment, field, true);
        
        if (logger.isDebugEnabled()) {
            logger.info("\nGraphQL JPQL Single Result Query String:\n    {}", getJPQLQueryString(query));
        }
        
        return query.getSingleResult();
    }

	private Field flattenEmbeddedIdArguments(Field field) {
		// manage object arguments (EmbeddedId)
		final List<Argument> argumentsWhereObjectsAreFlattened = field.getArguments()
				.stream()
				.flatMap(argument -> {
					if (!isWhereArgument(argument) && !isPageArgument(argument) &&
							argument.getValue() instanceof ObjectValue) {
						return ((ObjectValue) argument.getValue()).getObjectFields()
								.stream()
								.map(objectField -> new Argument(argument.getName() + "." + objectField.getName(), objectField.getValue()));
					} else {
						return Stream.of(argument);
					}
				})
				.collect(Collectors.toList());
		
		return field.transform(builder -> builder.arguments(argumentsWhereObjectsAreFlattened));
	}

    /**
     * Creates builder to build {@link GraphQLJpaSimpleDataFetcher}.
     * @return created builder
     */
    public static IEntityManagerStage builder() {
        return new Builder();
    }

    public interface IEntityManagerStage {

        public IEntityTypeStage withEntityManager(EntityManager entityManager);
    }

    public interface IEntityTypeStage {

        public IToManyDefaultOptionalStage withEntityType(EntityType<?> entityType);
    }

    public interface IToManyDefaultOptionalStage {

        public IBuildStage withToManyDefaultOptional(boolean toManyDefaultOptional);
    }

    public interface IBuildStage {

        public GraphQLJpaSimpleDataFetcher build();
    }

    /**
     * Builder to build {@link GraphQLJpaSimpleDataFetcher}.
     */
    public static final class Builder implements IEntityManagerStage, IEntityTypeStage, IToManyDefaultOptionalStage, IBuildStage {

        private EntityManager entityManager;
        private EntityType<?> entityType;
        private boolean toManyDefaultOptional;

        private Builder() {
        }

        @Override
        public IEntityTypeStage withEntityManager(EntityManager entityManager) {
            this.entityManager = entityManager;
            return this;
        }

        @Override
        public IToManyDefaultOptionalStage withEntityType(EntityType<?> entityType) {
            this.entityType = entityType;
            return this;
        }

        @Override
        public IBuildStage withToManyDefaultOptional(boolean toManyDefaultOptional) {
            this.toManyDefaultOptional = toManyDefaultOptional;
            return this;
        }

        @Override
        public GraphQLJpaSimpleDataFetcher build() {
            return new GraphQLJpaSimpleDataFetcher(this);
        }
    }
}
