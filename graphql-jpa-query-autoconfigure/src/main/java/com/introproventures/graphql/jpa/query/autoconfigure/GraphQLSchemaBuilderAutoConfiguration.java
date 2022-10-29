package com.introproventures.graphql.jpa.query.autoconfigure;

import com.introproventures.graphql.jpa.query.schema.GraphQLSchemaBuilder;
import com.introproventures.graphql.jpa.query.schema.RestrictedKeysProvider;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;
import graphql.GraphQL;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;

@Configuration
@ConditionalOnClass({EntityManagerFactory.class, GraphQL.class, GraphQLSchemaBuilder.class})
@ConditionalOnProperty(name="spring.graphql.jpa.query.enabled", havingValue="true", matchIfMissing=true)
@AutoConfigureBefore({GraphQLSchemaAutoConfiguration.class, GraphQLJpaQueryGraphQlSourceAutoConfiguration.class})
@AutoConfigureAfter(HibernateJpaAutoConfiguration.class)
public class GraphQLSchemaBuilderAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnSingleCandidate(EntityManagerFactory.class)
    GraphQLSchemaBuilder defaultGraphQLJpaSchemaBuilder(EntityManagerFactory entityManagerFactory,
                                                        GraphQLJpaQueryProperties properties,
                                                        ObjectProvider<RestrictedKeysProvider> restrictedKeysProvider,
                                                        ObjectProvider<GraphQLSchemaBuilderCustomizer> graphQLSchemaBuilderCustomizer) {
        GraphQLJpaSchemaBuilder builder = new GraphQLJpaSchemaBuilder(entityManagerFactory.createEntityManager());

        EnableGraphQLJpaQuerySchemaImportSelector.getPackageNames()
                                                 .stream()
                                                 .forEach(builder::entityPath);

        graphQLSchemaBuilderCustomizer.ifAvailable(it -> it.customize(builder));

        restrictedKeysProvider.ifAvailable(builder::restrictedKeysProvider);

        return builder;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnSingleCandidate(GraphQLSchemaBuilder.class)
    GraphQLSchemaConfigurer defaultGraphQLJpaSchemaBuilderConfigurer(GraphQLSchemaBuilder graphQLJpaSchemaBuilder) {
        return registry -> registry.register(graphQLJpaSchemaBuilder.build());
    }

}
