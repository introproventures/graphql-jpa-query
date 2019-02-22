package com.introproventures.graphql.jpa.query.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target( { FIELD, METHOD })
@Retention(RUNTIME)
public @interface GraphQLCalcField {
}
