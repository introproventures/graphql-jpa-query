package com.introproventures.graphql.jpa.query.mutations.fetcher.impl;

import com.introproventures.graphql.jpa.query.schema.ExceptionGraphQLRuntime;
import com.introproventures.graphql.jpa.query.mutations.annotation.GraphQLWriteType;
import com.introproventures.graphql.jpa.query.mutations.fetcher.MutationContext;
import com.introproventures.graphql.jpa.query.schema.impl.FetcherParams;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;

public class InsertFecher extends GraphQLJpaEntityInputFetcher {
	public InsertFecher(EntityManager entityManager, FetcherParams fetcherParams, EntityType<?> entityType) {
        super(entityManager, fetcherParams, entityType);
    }

    @Override
	public Object executeMutation(Object entity, MutationContext mutationContext) {
		try {
			reloadChildEntities(entityType, entity, mutationContext);

			checkAccessWriteOperation(entity.getClass(), GraphQLWriteType.INSERT);
			entityManager.persist(entity);

			entityManager.flush();

			entityManager.refresh(entity);

			return entity;
		} catch (ExceptionGraphQLRuntime e) {
			throw e;
		} catch (Exception e) {
			throw new ExceptionGraphQLRuntime(e);
		}

	}


}
