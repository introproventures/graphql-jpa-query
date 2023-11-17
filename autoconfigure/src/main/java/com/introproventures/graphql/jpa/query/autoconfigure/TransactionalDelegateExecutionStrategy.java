package com.introproventures.graphql.jpa.query.autoconfigure;

import graphql.ExecutionResult;
import graphql.execution.AsyncExecutionStrategy;
import graphql.execution.ExecutionContext;
import graphql.execution.ExecutionStrategy;
import graphql.execution.ExecutionStrategyParameters;
import graphql.execution.NonNullableFieldWasNullException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

public class TransactionalDelegateExecutionStrategy extends ExecutionStrategy {

    private static final Logger log = LoggerFactory.getLogger(TransactionalDelegateExecutionStrategy.class);

    private final TransactionTemplate transactionTemplate;

    private final ExecutionStrategy delegate;

    private final Supplier<Executor> executor;

    public TransactionalDelegateExecutionStrategy(
        TransactionTemplate transactionTemplate,
        ExecutionStrategy delegate,
        Supplier<Executor> executor
    ) {
        this.transactionTemplate = transactionTemplate;
        this.delegate = delegate;
        this.executor = executor;
    }

    @Override
    public CompletableFuture<ExecutionResult> execute(
        ExecutionContext executionContext,
        ExecutionStrategyParameters parameters
    ) throws NonNullableFieldWasNullException {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            if (log.isTraceEnabled()) {
                log.trace(
                    "Start root execution request {} running on {}",
                    executionContext.getExecutionId(),
                    Thread.currentThread()
                );
            }
            return CompletableFuture.supplyAsync(
                () -> {
                    return transactionTemplate.execute(status -> {
                        if (log.isTraceEnabled()) {
                            log.trace(
                                "Begin transaction for {} on {}",
                                executionContext.getExecutionId(),
                                Thread.currentThread()
                            );
                        }
                        try {
                            if (log.isTraceEnabled()) {
                                log.trace(
                                    "Execute request for {} on {}",
                                    executionContext.getExecutionId(),
                                    Thread.currentThread()
                                );
                            }

                            return delegate.execute(executionContext, parameters).join();
                        } finally {
                            if (log.isTraceEnabled()) {
                                log.trace(
                                    "End transaction for {} on {}",
                                    executionContext.getExecutionId(),
                                    Thread.currentThread()
                                );
                            }
                        }
                    });
                },
                executor.get()
            );
        } else {
            if (log.isTraceEnabled()) {
                log.trace(
                    "Execute request {} for {} on {}",
                    executionContext.getExecutionId(),
                    parameters.getField().getName(),
                    Thread.currentThread()
                );
            }
            return delegate.execute(executionContext, parameters);
        }
    }

    public static final class Builder {

        private TransactionTemplate transactionTemplate;
        private Supplier<Executor> executor = Executors::newCachedThreadPool;
        private ExecutionStrategy delegate = new AsyncExecutionStrategy();

        private Builder() {}

        public static Builder newTransactionalExecutionStrategy(TransactionTemplate transactionTemplate) {
            return new Builder().transactionTemplate(transactionTemplate);
        }

        public Builder transactionTemplate(TransactionTemplate transactionTemplate) {
            this.transactionTemplate = transactionTemplate;
            return this;
        }

        public Builder delegate(ExecutionStrategy delegate) {
            this.delegate = delegate;
            return this;
        }

        public Builder executor(Supplier<Executor> executor) {
            this.executor = executor;
            return this;
        }

        public TransactionalDelegateExecutionStrategy build() {
            return new TransactionalDelegateExecutionStrategy(transactionTemplate, delegate, executor);
        }
    }
}
