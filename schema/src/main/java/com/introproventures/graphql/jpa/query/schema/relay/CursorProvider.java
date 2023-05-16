package com.introproventures.graphql.jpa.query.schema.relay;

import graphql.relay.ConnectionCursor;

@FunctionalInterface
public interface CursorProvider<N> {
    ConnectionCursor createCursor(N node, int index);
}
