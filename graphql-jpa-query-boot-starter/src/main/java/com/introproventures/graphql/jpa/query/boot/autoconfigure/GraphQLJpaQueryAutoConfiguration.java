/*
 * Copyright 2017 IntroPro Ventures, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.introproventures.graphql.jpa.query.boot.autoconfigure;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.introproventures.graphql.jpa.query.autoconfigure.GraphQLSchemaConfigurer;
import com.introproventures.graphql.jpa.query.schema.GraphQLExecutor;
import com.introproventures.graphql.jpa.query.schema.GraphQLExecutorContextFactory;
import com.introproventures.graphql.jpa.query.schema.GraphQLSchemaBuilder;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaExecutor;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaExecutorContextFactory;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;

@Configuration
@ConditionalOnClass(GraphQL.class)
@ConditionalOnProperty(name="spring.graphql.jpa.query.enabled", havingValue="true", matchIfMissing=true)
public class GraphQLJpaQueryAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnSingleCandidate(EntityManagerFactory.class)
    public GraphQLSchemaBuilder graphQLSchemaBuilder(final EntityManagerFactory entityManagerFactory) {
        return new GraphQLJpaSchemaBuilder(entityManagerFactory.createEntityManager());
    }
    
    @Bean
    @ConditionalOnMissingBean
    public GraphQLSchemaConfigurer graphQLJpaQuerySchemaConfigurer(GraphQLSchemaBuilder graphQLSchemaBuilder) {
        
        return (registry) -> {
            registry.register(graphQLSchemaBuilder.build());
        };
    }    
    
    @Configuration
    public static class DefaultGraphQLJpaQueryConfiguration {

        @Bean 
        @ConditionalOnMissingBean
        public GraphQLExecutionInputFactory graphQLExecutionInputFactory() {
            return new GraphQLExecutionInputFactory() { };
        }
        
        @Bean 
        @ConditionalOnMissingBean
        public GraphQLExecutorContextFactory graphQLExecutorContextFactory(GraphQLExecutionInputFactory graphQLExecutionInputFactory) {
            return new GraphQLJpaExecutorContextFactory(graphQLExecutionInputFactory);
        }

        @Bean
        @ConditionalOnMissingBean
        public GraphQLExecutor graphQLExecutor(GraphQLSchema graphQLSchema,
                                               GraphQLExecutorContextFactory graphQLExecutorContextFactory) {
            return new GraphQLJpaExecutor(graphQLSchema,
                                          graphQLExecutorContextFactory);
        }

        @Bean
        @ConditionalOnMissingBean
        public GraphQLSchemaBuilder graphQLSchemaBuilder(final EntityManager entityManager) {
            return new GraphQLJpaSchemaBuilder(entityManager);
        }

    }
}
