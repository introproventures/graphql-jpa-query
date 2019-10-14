package com.introproventures.graphql.jpa.query.mutations.fetcher.impl;

import com.introproventures.graphql.jpa.query.schema.ExceptionGraphQLRuntime;
import com.introproventures.graphql.jpa.query.mutations.annotation.GraphQLWriteType;
import com.introproventures.graphql.jpa.query.mutations.fetcher.MutationContext;
import com.introproventures.graphql.jpa.query.schema.impl.FetcherParams;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;
import java.util.List;

public class UpdateFetcher extends GraphQLJpaEntityInputFetcher {
	public UpdateFetcher(EntityManager entityManager, FetcherParams fetcherParams, EntityType<?> entityType) {
        super(entityManager, fetcherParams, entityType);
    }

	@Override
	public Object executeMutation(Object entity, MutationContext mutationContext) {
		List<String> updateFields = mutationContext.getObjectFields(entity);

		try {
			reloadChildEntities(entityType, entity, mutationContext);

			Object currentEntity = reloadEntity(entity);

			checkAccessWriteOperation(currentEntity.getClass(), GraphQLWriteType.INSERT);
			copyEntityFields(entity, currentEntity, updateFields);

			entityManager.flush();

			entityManager.refresh(currentEntity);

			return currentEntity;
		} catch (ExceptionGraphQLRuntime e) {
			throw e;
		} catch (Exception e) {
			throw new ExceptionGraphQLRuntime(e);
		}

	}


}
