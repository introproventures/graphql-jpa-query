package com.introproventures.graphql.jpa.query.schema.impl;

import java.util.function.Predicate;

public class FetcherParams {
    private MapEntityType mapEntityType;
    private Predicate<String[]> predicateRole;

    public FetcherParams(MapEntityType mapEntityType, Predicate<String[]> predicateRole) {
        this.mapEntityType = mapEntityType;
        this.predicateRole = predicateRole;
    }

    public MapEntityType getMapEntityType() {
        return mapEntityType;
    }

    public Predicate<String[]> getPredicateRole() {
        return predicateRole;
    }
}
