package com.introproventures.graphql.jpa.query.mutations.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface GraphQLWriteEntityForRole {
    String[] value();

    GraphQLWriteType[] operations() default {GraphQLWriteType.ALL};
}


