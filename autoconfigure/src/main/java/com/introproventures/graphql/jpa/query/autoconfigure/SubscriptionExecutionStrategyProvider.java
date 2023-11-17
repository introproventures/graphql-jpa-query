package com.introproventures.graphql.jpa.query.autoconfigure;

import graphql.execution.ExecutionStrategy;
import java.util.function.Supplier;

public interface SubscriptionExecutionStrategyProvider extends Supplier<ExecutionStrategy> {}
