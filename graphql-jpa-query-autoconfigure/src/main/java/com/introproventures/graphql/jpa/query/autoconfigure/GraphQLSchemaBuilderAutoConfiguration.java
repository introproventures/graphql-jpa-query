package com.introproventures.graphql.jpa.query.autoconfigure;

import com.introproventures.graphql.jpa.query.schema.GraphQLSchemaBuilder;
import com.introproventures.graphql.jpa.query.schema.RestrictedKeysProvider;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;
import graphql.GraphQL;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import java.util.List;

@Configuration
@ConditionalOnClass({GraphQL.class, GraphQLSchemaBuilder.class})
@ConditionalOnProperty(name="spring.graphql.jpa.query.enabled", havingValue="true", matchIfMissing=true)
@AutoConfigureAfter(HibernateJpaAutoConfiguration.class)
public class GraphQLSchemaBuilderAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnSingleCandidate(EntityManagerFactory.class)
    public GraphQLSchemaBuilder graphQLJpaSchemaBuilder(final EntityManagerFactory entityManagerFactory,
                                                        ObjectProvider<RestrictedKeysProvider> restrictedKeysProvider) {
        GraphQLJpaSchemaBuilder bean = new GraphQLJpaSchemaBuilder(entityManagerFactory.createEntityManager());

        restrictedKeysProvider.ifAvailable(bean::restrictedKeysProvider);

        return bean;
    }

    @Bean
    @ConditionalOnMissingBean(GraphQLSchemaConfigurer.class)
    InitializingBean defaultGraphQLSchemaBuilderConfigurer(GraphQLShemaRegistration graphQLShemaRegistration,
                                                           GraphQLSchemaBuilder graphQLSchemaBuilder,
                                                           List<GraphQLSchemaConfigurer> configurers) {
        return () -> {
            if (configurers.isEmpty()) {
                graphQLShemaRegistration.register(graphQLSchemaBuilder.build());
            }
        };
    }

}
