package com.introproventures.graphql.jpa.query.mutations.fetcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MutationContext {
    private Map<Object, List<String>> objFields = new HashMap<>();
    private String operationName;

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public void addContextFields(Object obj, List<String> fields) {
        objFields.put(obj, fields);
    }

    public List<String> getObjectFields(Object obj) {
        return objFields.get(obj);
    }
}
