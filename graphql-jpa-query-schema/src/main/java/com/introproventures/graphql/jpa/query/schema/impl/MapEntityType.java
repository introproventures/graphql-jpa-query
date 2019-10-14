package com.introproventures.graphql.jpa.query.schema.impl;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;
import java.util.HashMap;
import java.util.Map;

public class MapEntityType {
    private EntityManager entityManager;
    private Map<String, EntityType> entityTypeMap = new HashMap<>();

    public MapEntityType(EntityManager entityManager) {
        this.entityManager = entityManager;

        for (EntityType entityType : entityManager.getMetamodel().getEntities()) {
            entityTypeMap.put(entityType.getName(), entityType);
        }
    }

    public boolean existEntityType(String name) {
        return entityTypeMap.containsKey(name);
    }

    public EntityType getEntityType(String name) {
        return entityTypeMap.get(name);
    }

}
