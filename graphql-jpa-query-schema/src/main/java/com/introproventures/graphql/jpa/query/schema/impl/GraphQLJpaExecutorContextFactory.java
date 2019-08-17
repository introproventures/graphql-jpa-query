package com.introproventures.graphql.jpa.query.schema.impl;


public interface GraphQLJpaExecutorContextFactory {

    default GraphQLJpaExecutorContext create() {
        return new GraphQLJpaExecutorContext() { };
    };
    
}
