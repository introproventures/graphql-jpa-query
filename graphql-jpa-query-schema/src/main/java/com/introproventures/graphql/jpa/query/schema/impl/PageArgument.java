package com.introproventures.graphql.jpa.query.schema.impl;


public final class PageArgument {
    private int start;
    private int limit;

    public PageArgument(Integer start, Integer limit) {
        this.start = start;
        this.limit = limit;
    }

    public int getStart() {
        return start;
    }
    
    public int getLimit() {
        return limit;
    }
    
    public int getOffset() {
        return (getStart() - 1) * getLimit();        
    }
}    
