package com.introproventures.graphql.jpa.query.schema.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.dataloader.DataLoader;
import org.dataloader.DataLoaderOptions;
import org.dataloader.DataLoaderRegistry;
import org.dataloader.MappedBatchLoaderWithContext;

public class BatchLoaderRegistry {
    private final static Map<String, MappedBatchLoaderWithContext<Object, List<Object>>> mappedToManyBatchLoaders = new LinkedHashMap<>();
    private final static Map<String, MappedBatchLoaderWithContext<Object, Object>> mappedToOneBatchLoaders = new LinkedHashMap<>();
    private static BatchLoaderRegistry instance = new BatchLoaderRegistry();

    public static BatchLoaderRegistry getInstance() {
        return instance;
    }

    public static void registerToMany(String batchLoaderKey, MappedBatchLoaderWithContext<Object, List<Object>> mappedBatchLoader) {
        mappedToManyBatchLoaders.putIfAbsent(batchLoaderKey, mappedBatchLoader);
    }

    public static void registerToOne(String batchLoaderKey, MappedBatchLoaderWithContext<Object, Object> mappedBatchLoader) {
        mappedToOneBatchLoaders.putIfAbsent(batchLoaderKey, mappedBatchLoader);
    }

    public static DataLoaderRegistry newDataLoaderRegistry(DataLoaderOptions dataLoaderOptions) {
        DataLoaderRegistry dataLoaderRegistry = new DataLoaderRegistry();

        mappedToManyBatchLoaders.entrySet()
                                .forEach(entry -> {
                                    DataLoader<Object, List<Object>> dataLoader = DataLoader.newMappedDataLoader(entry.getValue(),
                                                                                                                 dataLoaderOptions);
                                    dataLoaderRegistry.register(entry.getKey(), dataLoader);
                                });

        mappedToOneBatchLoaders.entrySet()
                               .forEach(entry -> {
                                   DataLoader<Object, Object> dataLoader = DataLoader.newMappedDataLoader(entry.getValue(),
                                                                                                          dataLoaderOptions);
                                   dataLoaderRegistry.register(entry.getKey(), dataLoader);
                               });

        return dataLoaderRegistry;

    }

}
