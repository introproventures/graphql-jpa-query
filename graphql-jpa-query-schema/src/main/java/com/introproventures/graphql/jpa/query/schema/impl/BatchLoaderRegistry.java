package com.introproventures.graphql.jpa.query.schema.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.dataloader.DataLoader;
import org.dataloader.DataLoaderOptions;
import org.dataloader.DataLoaderRegistry;
import org.dataloader.MappedBatchLoaderWithContext;

public class BatchLoaderRegistry {
    private final static Map<String, MappedBatchLoaderWithContext<Object, List<Object>>> mappedBatchLoaders = new LinkedHashMap<>();
    private static BatchLoaderRegistry instance = new BatchLoaderRegistry();

    public static BatchLoaderRegistry getInstance() {
        return instance;
    }

    public static void register(String batchLoaderKey, MappedBatchLoaderWithContext<Object, List<Object>> mappedBatchLoader) {
        mappedBatchLoaders.putIfAbsent(batchLoaderKey, mappedBatchLoader);
    }

    public static DataLoaderRegistry newDataLoaderRegistry(DataLoaderOptions dataLoaderOptions) {
        DataLoaderRegistry dataLoaderRegistry = new DataLoaderRegistry();

        mappedBatchLoaders.entrySet()
                          .forEach(entry -> {
                              DataLoader<Object, List<Object>> dataLoader = DataLoader.newMappedDataLoader(entry.getValue(),
                                                                                                           dataLoaderOptions);
                              dataLoaderRegistry.register(entry.getKey(), dataLoader);
                          });

        return dataLoaderRegistry;

    }

}
