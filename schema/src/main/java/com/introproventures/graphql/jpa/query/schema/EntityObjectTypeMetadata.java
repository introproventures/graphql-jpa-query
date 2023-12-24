package com.introproventures.graphql.jpa.query.schema;

import jakarta.persistence.metamodel.EmbeddableType;
import jakarta.persistence.metamodel.EntityType;

public interface EntityObjectTypeMetadata {
    EntityType<?> entity(String objectType);

    EmbeddableType<?> embeddable(String objectType);
}
