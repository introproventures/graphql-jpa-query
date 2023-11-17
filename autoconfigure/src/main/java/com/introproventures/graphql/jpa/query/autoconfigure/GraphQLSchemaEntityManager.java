package com.introproventures.graphql.jpa.query.autoconfigure;

import jakarta.persistence.EntityManager;
import java.util.function.Supplier;

public interface GraphQLSchemaEntityManager extends Supplier<EntityManager> {}
