package com.introproventures.graphql.jpa.query.schema.impl;

import graphql.schema.DataFetchingEnvironment;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.dataloader.BatchLoaderEnvironment;
import org.dataloader.MappedBatchLoaderWithContext;

// a batch loader function that will be called with N or more keys for batch loading
class GraphQLJpaToOneMappedBatchLoader implements MappedBatchLoaderWithContext<Object, Object> {

    private final GraphQLJpaQueryFactory queryFactory;

    public GraphQLJpaToOneMappedBatchLoader(GraphQLJpaQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public CompletionStage<Map<Object, Object>> load(Set<Object> keys, BatchLoaderEnvironment environment) {
        Object key = keys.iterator().next();
        DataFetchingEnvironment context = (DataFetchingEnvironment) environment.getKeyContexts().get(key);

        return CompletableFuture.completedStage(queryFactory.loadManyToOne(context, keys));
    }
}
