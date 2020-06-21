package com.introproventures.graphql.jpa.query.schema;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.introproventures.graphql.jpa.query.schema.impl.EntityIntrospector.EntityIntrospectionResult;

@FunctionalInterface
public interface RestrictedKeysProvider extends Function<EntityIntrospectionResult, Optional<List<Object>>> {

}
