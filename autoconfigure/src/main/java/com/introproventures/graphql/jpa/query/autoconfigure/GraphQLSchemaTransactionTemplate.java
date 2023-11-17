package com.introproventures.graphql.jpa.query.autoconfigure;

import java.util.function.Supplier;
import org.springframework.transaction.support.TransactionTemplate;

public interface GraphQLSchemaTransactionTemplate extends Supplier<TransactionTemplate> {}
