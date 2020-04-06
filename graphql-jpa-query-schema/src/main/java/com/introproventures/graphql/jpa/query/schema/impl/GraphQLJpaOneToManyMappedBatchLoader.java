package com.introproventures.graphql.jpa.query.schema.impl;

import static java.util.stream.Collectors.groupingBy;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import javax.persistence.TypedQuery;

import org.dataloader.BatchLoaderEnvironment;
import org.dataloader.MappedBatchLoaderWithContext;

import com.introproventures.graphql.jpa.query.support.GraphQLSupport;
import graphql.language.Field;
import graphql.schema.DataFetchingEnvironment;

// a batch loader function that will be called with N or more keys for batch loading
class GraphQLJpaOneToManyMappedBatchLoader implements MappedBatchLoaderWithContext<Object, List<Object>> {

    private final GraphQLJpaQueryFactory queryFactory;

    public GraphQLJpaOneToManyMappedBatchLoader(GraphQLJpaQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public CompletionStage<Map<Object, List<Object>>> load(Set<Object> keys, BatchLoaderEnvironment environment) {
        Object key = keys.iterator().next();
        DataFetchingEnvironment context = (DataFetchingEnvironment) environment.getKeyContexts()
                                                                               .get(key);
        Field field = context.getField();

        return CompletableFuture.supplyAsync(() -> {
            TypedQuery<Object[]> query = queryFactory.getCollectionQuery(context, field, true, keys);

            List<Object[]> resultList = query.getResultList();

            Map<Object, List<Object>> batch = resultList.stream()
                                                        .collect(groupingBy(t -> t[0],
                                                                            Collectors.mapping(t -> t[1],
                                                                                               GraphQLSupport.toResultList())));
            Map<Object, List<Object>> resultMap = new LinkedHashMap<>();

            keys.forEach(it -> {
                List<Object> list = batch.getOrDefault(it, Collections.emptyList());

                if (!list.isEmpty()) {
                    list = list.stream()
                               .filter(GraphQLSupport.distinctByKey(GraphQLSupport::identityToString))
                               .collect(Collectors.toList());
                }

                resultMap.put(it, list);
            });

            return resultMap;
        });
    }
};
