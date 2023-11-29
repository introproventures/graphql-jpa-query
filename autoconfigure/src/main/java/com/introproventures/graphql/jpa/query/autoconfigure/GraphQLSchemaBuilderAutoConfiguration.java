package com.introproventures.graphql.jpa.query.autoconfigure;

import static com.introproventures.graphql.jpa.query.autoconfigure.TransactionalDelegateExecutionStrategy.Builder.newTransactionalExecutionStrategy;

import com.introproventures.graphql.jpa.query.schema.GraphQLSchemaBuilder;
import com.introproventures.graphql.jpa.query.schema.RestrictedKeysProvider;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;
import graphql.GraphQL;
import graphql.execution.AsyncExecutionStrategy;
import graphql.execution.SubscriptionExecutionStrategy;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@AutoConfiguration(
    before = { GraphQLSchemaAutoConfiguration.class, GraphQLJpaQueryGraphQlSourceAutoConfiguration.class },
    after = HibernateJpaAutoConfiguration.class
)
@EnableConfigurationProperties(GraphQLJpaQueryProperties.class)
@ConditionalOnClass({ EntityManagerFactory.class, GraphQL.class, GraphQLSchemaBuilder.class })
@ConditionalOnProperty(name = "spring.graphql.jpa.query.enabled", havingValue = "true", matchIfMissing = true)
public class GraphQLSchemaBuilderAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(GraphQLSchemaBuilderAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean(GraphQLSchemaTransactionTemplate.class)
    @ConditionalOnSingleCandidate(PlatformTransactionManager.class)
    GraphQLSchemaTransactionTemplate graphQLSchemaTransactionTemplate(PlatformTransactionManager transactionManager) {
        return () -> new TransactionTemplate(transactionManager);
    }

    @Bean
    @ConditionalOnMissingBean(QueryExecutionStrategyProvider.class)
    @ConditionalOnSingleCandidate(GraphQLSchemaTransactionTemplate.class)
    QueryExecutionStrategyProvider queryExecutionStrategy(
        GraphQLSchemaTransactionTemplate graphQLSchemaTransactionTemplate
    ) {
        var transactionTemplate = graphQLSchemaTransactionTemplate.get();
        transactionTemplate.setReadOnly(true);

        return () ->
            newTransactionalExecutionStrategy(transactionTemplate).delegate(new AsyncExecutionStrategy()).build();
    }

    @Bean
    @ConditionalOnMissingBean(MutationExecutionStrategyProvider.class)
    @ConditionalOnSingleCandidate(GraphQLSchemaTransactionTemplate.class)
    MutationExecutionStrategyProvider mutationExecutionStrategy(
        GraphQLSchemaTransactionTemplate graphQLSchemaTransactionTemplate
    ) {
        var transactionTemplate = graphQLSchemaTransactionTemplate.get();
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        return () ->
            newTransactionalExecutionStrategy(transactionTemplate).delegate(new AsyncExecutionStrategy()).build();
    }

    @Bean
    @ConditionalOnMissingBean(SubscriptionExecutionStrategyProvider.class)
    @ConditionalOnSingleCandidate(GraphQLSchemaTransactionTemplate.class)
    SubscriptionExecutionStrategyProvider subscriptionExecutionStrategy(
        GraphQLSchemaTransactionTemplate graphQLSchemaTransactionTemplate
    ) {
        var transactionTemplate = graphQLSchemaTransactionTemplate.get();
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_SUPPORTS);
        transactionTemplate.setReadOnly(true);

        return () ->
            newTransactionalExecutionStrategy(transactionTemplate)
                .delegate(new SubscriptionExecutionStrategy())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(GraphQLSchemaEntityManager.class)
    @ConditionalOnSingleCandidate(EntityManagerFactory.class)
    GraphQLSchemaEntityManager graphQLSchemaEntityManager(EntityManagerFactory entityManagerFactory) {
        return () -> SharedEntityManagerCreator.createSharedEntityManager(entityManagerFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnSingleCandidate(EntityManagerFactory.class)
    GraphQLJpaSchemaBuilder defaultGraphQLJpaSchemaBuilder(
        GraphQLSchemaEntityManager graphQLSchemaEntityManager,
        GraphQLJpaQueryProperties properties,
        ObjectProvider<RestrictedKeysProvider> restrictedKeysProvider
    ) {
        final EntityManager entityManager = graphQLSchemaEntityManager.get();

        GraphQLJpaSchemaBuilder builder = new GraphQLJpaSchemaBuilder(entityManager);

        builder
            .name(properties.getName())
            .description(properties.getDescription())
            .defaultDistinct(properties.isDefaultDistinct())
            .useDistinctParameter(properties.isUseDistinctParameter())
            .toManyDefaultOptional(properties.isToManyDefaultOptional())
            .enableRelay(properties.isEnableRelay());

        EnableGraphQLJpaQuerySchemaImportSelector.getPackageNames().stream().forEach(builder::entityPath);

        restrictedKeysProvider.ifAvailable(builder::restrictedKeysProvider);

        log.warn("Configured {} for {} GraphQL schema", entityManager, properties.getName());

        return builder;
    }

    @Bean
    @ConditionalOnSingleCandidate(GraphQLSchemaBuilder.class)
    GraphQLSchemaConfigurer defaultGraphQLJpaSchemaBuilderConfigurer(
        GraphQLJpaSchemaBuilder builder,
        ObjectProvider<GraphQLJPASchemaBuilderCustomizer> customizer
    ) {
        return registry -> {
            customizer.ifAvailable(it -> it.customize(builder));

            registry.register(builder.build());
        };
    }
}
