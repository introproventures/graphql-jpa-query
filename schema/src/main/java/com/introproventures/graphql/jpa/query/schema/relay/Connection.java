package com.introproventures.graphql.jpa.query.schema.relay;

import graphql.relay.Edge;
import graphql.relay.PageInfo;
import java.util.List;

public interface Connection<E extends Edge<?>> {
    List<E> getEdges();

    PageInfo getPageInfo();
}
