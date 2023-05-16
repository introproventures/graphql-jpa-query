package com.introproventures.graphql.jpa.query.autoconfigure;

import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;

@FunctionalInterface
public interface GraphQLJPASchemaBuilderCustomizer {
    void customize(GraphQLJpaSchemaBuilder builder);
}
