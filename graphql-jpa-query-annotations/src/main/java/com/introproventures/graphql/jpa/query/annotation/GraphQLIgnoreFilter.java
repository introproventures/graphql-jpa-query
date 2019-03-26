package com.introproventures.graphql.jpa.query.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target( { TYPE, FIELD })
@Retention(RUNTIME)
public @interface GraphQLIgnoreFilter {
}

