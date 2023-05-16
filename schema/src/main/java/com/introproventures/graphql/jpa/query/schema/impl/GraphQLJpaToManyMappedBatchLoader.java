package com.introproventures.graphql.jpa.query.schema.impl;

import graphql.schema.DataFetchingEnvironment;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.dataloader.BatchLoaderEnvironment;
import org.dataloader.MappedBatchLoaderWithContext;

// a batch loader function that will be called with N or more keys for batch loading
class GraphQLJpaToManyMappedBatchLoader implements MappedBatchLoaderWithContext<Object, List<Object>> {

    private final GraphQLJpaQueryFactory queryFactory;

    public GraphQLJpaToManyMappedBatchLoader(GraphQLJpaQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public CompletionStage<Map<Object, List<Object>>> load(Set<Object> keys, BatchLoaderEnvironment environment) {
        Object key = keys.iterator().next();
        DataFetchingEnvironment context = (DataFetchingEnvironment) environment.getKeyContexts().get(key);

        return CompletableFuture.supplyAsync(() -> queryFactory.loadOneToMany(context, keys));
    }
}
