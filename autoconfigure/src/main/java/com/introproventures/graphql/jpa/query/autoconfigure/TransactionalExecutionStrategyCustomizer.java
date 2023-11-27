package com.introproventures.graphql.jpa.query.autoconfigure;

import graphql.execution.ExecutionStrategy;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface TransactionalExecutionStrategyCustomizer<T extends Supplier<ExecutionStrategy>>
    extends Consumer<TransactionalDelegateExecutionStrategy.Builder> {}
