package com.introproventures.graphql.jpa.query.schema;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.introproventures.graphql.jpa.query.schema.impl.EntityIntrospector.EntityIntrospectionResult;

/**
 * The RestrictedKeysProvider functional interface should provide a list of restricted keys in order to filter records  
 * at runtime based on user security context based on EntityIntrospection descriptor. 
 * 
 * The return argument uses Optional of List of Objects return type:
 * The non-empty list will restrict the query to provided keys
 * The empty list will run the query unrestricted. 
 * The empty Optional will block running the query and return empty result. 
 *
 */
@FunctionalInterface
public interface RestrictedKeysProvider extends Function<EntityIntrospectionResult, Optional<List<Object>>> {

    /**
     * Applies this restricted keys provider function to the given argument of entityDescriptor.
     *
     * @param entityDescriptor the function argument
     * @return the function result with optional list of keys
     */
    @Override
    Optional<List<Object>> apply(EntityIntrospectionResult entityDescriptor);
    
}
