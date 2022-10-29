package com.introproventures.graphql.jpa.query.autoconfigure;

import com.introproventures.graphql.jpa.query.schema.GraphQLSchemaBuilder;

@FunctionalInterface
public interface GraphQLSchemaBuilderCustomizer {
    void customize(GraphQLSchemaBuilder builder);
}
