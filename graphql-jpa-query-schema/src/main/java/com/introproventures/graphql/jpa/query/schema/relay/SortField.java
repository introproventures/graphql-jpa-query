package com.introproventures.graphql.jpa.query.schema.relay;

public class SortField {

    public enum Direction {
        ASC, DESC
    }

    private String name;
    private Direction direction;

    public SortField(String name, Direction direction) {
        this.name = name;
        this.direction = direction;
    }

    public String getName() {
        return name;
    }

    public Direction getDirection() {
        return direction;
    }
}