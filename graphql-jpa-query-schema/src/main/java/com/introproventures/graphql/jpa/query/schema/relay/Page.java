package com.introproventures.graphql.jpa.query.schema.relay;

import java.util.List;

import graphql.relay.Edge;
import graphql.relay.PageInfo;

public interface Page<N> extends Connection<Edge<N>> {
    @Override
    List<Edge<N>> getEdges();

    @Override
    PageInfo getPageInfo();
}