package com.introproventures.graphql.jpa.query.autoconfigure;

import com.introproventures.graphql.jpa.query.schema.GraphQLSchemaBuilder;
import com.introproventures.graphql.jpa.query.schema.RestrictedKeysProvider;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;
import graphql.GraphQL;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(
    before = { GraphQLSchemaAutoConfiguration.class, GraphQLJpaQueryGraphQlSourceAutoConfiguration.class },
    after = HibernateJpaAutoConfiguration.class
)
@EnableConfigurationProperties(GraphQLJpaQueryProperties.class)
@ConditionalOnClass({ EntityManagerFactory.class, GraphQL.class, GraphQLSchemaBuilder.class })
@ConditionalOnProperty(name = "spring.graphql.jpa.query.enabled", havingValue = "true", matchIfMissing = true)
public class GraphQLSchemaBuilderAutoConfiguration {

    @Bean
    @GraphQLSchemaEntityManager
    @ConditionalOnMissingBean(value = EntityManager.class, parameterizedContainer = Supplier.class)
    @ConditionalOnSingleCandidate(EntityManagerFactory.class)
    Supplier<EntityManager> graphQLSchemaEntityManager(EntityManagerFactory entityManagerFactory) {
        return () -> SharedEntityManagerCreator.createSharedEntityManager(entityManagerFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnSingleCandidate(EntityManagerFactory.class)
    GraphQLJpaSchemaBuilder defaultGraphQLJpaSchemaBuilder(
        @GraphQLSchemaEntityManager Supplier<EntityManager> graphQLSchemaEntityManager,
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
