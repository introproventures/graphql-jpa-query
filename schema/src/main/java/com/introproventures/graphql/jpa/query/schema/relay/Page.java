package com.introproventures.graphql.jpa.query.schema.relay;

import graphql.relay.Edge;
import graphql.relay.PageInfo;
import java.util.List;

public interface Page<N> extends Connection<Edge<N>> {
    @Override
    List<Edge<N>> getEdges();

    @Override
    PageInfo getPageInfo();
}
