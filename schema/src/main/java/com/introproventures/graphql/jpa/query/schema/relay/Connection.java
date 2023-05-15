package com.introproventures.graphql.jpa.query.schema.relay;

import java.util.List;

import graphql.relay.Edge;
import graphql.relay.PageInfo;

public interface Connection<E extends Edge<?>> {

    List<E> getEdges();

    PageInfo getPageInfo();
}