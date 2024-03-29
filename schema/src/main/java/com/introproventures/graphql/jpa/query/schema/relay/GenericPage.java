package com.introproventures.graphql.jpa.query.schema.relay;

import graphql.relay.Edge;
import graphql.relay.PageInfo;
import java.util.List;

public class GenericPage<N> implements Page<N> {

    private List<Edge<N>> edges;
    private PageInfo pageInfo;

    public GenericPage(List<Edge<N>> edges, PageInfo pageInfo) {
        this.edges = edges;
        this.pageInfo = pageInfo;
    }

    @Override
    public List<Edge<N>> getEdges() {
        return edges;
    }

    @Override
    public PageInfo getPageInfo() {
        return pageInfo;
    }
}
