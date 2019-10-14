package com.introproventures.graphql.jpa.query.mutations.fetcher;

import graphql.schema.DataFetcher;

public interface IGraphQLJpaEntityInputFetcher extends DataFetcher<Object> {
    Object executeMutation(Object entity, MutationContext mutationContext);
}
