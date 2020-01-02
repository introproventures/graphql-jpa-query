package com.introproventures.graphql.jpa.query.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;

import com.introproventures.graphql.jpa.query.schema.GraphQLExecutionInputFactory;

import graphql.ExecutionInput.Builder;

public class GraphQLHttpRequestExecutionInputFactory implements GraphQLExecutionInputFactory {
    
    @Autowired
    private HttpServletRequest request;

    @Override
    public Builder create() {
        return GraphQLExecutionInputFactory.super.create()
                                                 .context(request);
    }

}
