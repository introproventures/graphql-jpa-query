package com.introproventures.graphql.jpa.query.schema;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.introproventures.graphql.jpa.query.schema.impl.EntityIntrospector.EntityIntrospectionResult;

/**
 * The RestrictedKeysProvider functional interface should provide a list of restricted keys in order to filter records  
 * at runtime based on user security context based on EntityIntrospection descriptor. 
 * 
 * The return argument uses Optional<List<Object>> return type. 
 * The non-empty list will restrict the query to provided keys
 * The empty Optional will block running the query and return empty result. 
 * The empty list will run the query unrestricted. 
 *
 */
@FunctionalInterface
public interface RestrictedKeysProvider extends Function<EntityIntrospectionResult, Optional<List<Object>>> {

}
