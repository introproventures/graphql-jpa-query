package com.introproventures.graphql.jpa.query.schema.impl;

import jakarta.persistence.metamodel.EmbeddableType;
import jakarta.persistence.metamodel.EntityType;

public interface GraphQLObjectTypeMetadata {
    EntityType<?> entity(String objectType);

    EmbeddableType<?> embeddable(String objectType);
}
