package com.introproventures.graphql.jpa.query.schema;

public class ExceptionGraphQLRuntime extends RuntimeException {

    public ExceptionGraphQLRuntime(String msg) {
        super(msg);
    }

    public ExceptionGraphQLRuntime(String message, Throwable cause) {
        super(message, cause);
    }

    public ExceptionGraphQLRuntime(Throwable cause) {
        super(cause);
    }

}
