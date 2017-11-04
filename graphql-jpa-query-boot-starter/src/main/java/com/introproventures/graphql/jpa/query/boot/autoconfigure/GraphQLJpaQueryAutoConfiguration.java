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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportAware;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;

import com.introproventures.graphql.jpa.query.schema.GraphQLExecutor;
import com.introproventures.graphql.jpa.query.schema.GraphQLSchemaBuilder;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaExecutor;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;
import com.introproventures.graphql.jpa.query.web.GraphQLController;

import graphql.GraphQL;

@Configuration
@PropertySource("classpath:/com/introproventures/graphql/jpa/query/boot/autoconfigure/default.properties")
@ConditionalOnClass(GraphQL.class)
@ConditionalOnProperty(name="spring.graphql.jpa.query.enabled", havingValue="true", matchIfMissing=false)
public class GraphQLJpaQueryAutoConfiguration {
    
    @Configuration
    @Import(GraphQLController.class)
    @EnableConfigurationProperties(GraphQLJpaQueryProperties.class)
    public static class DefaultActivitiGraphQLJpaConfiguration implements ImportAware {
        
        @Autowired
        GraphQLJpaQueryProperties properties;

        @Bean
        @ConditionalOnMissingBean(GraphQLExecutor.class)
        public GraphQLExecutor graphQLExecutor(final GraphQLSchemaBuilder graphQLSchemaBuilder) {
            return new GraphQLJpaExecutor(graphQLSchemaBuilder.build());
        }

        @Bean
        @ConditionalOnMissingBean(GraphQLSchemaBuilder.class)
        public GraphQLSchemaBuilder graphQLSchemaBuilder(final EntityManager entityManager) {
            Assert.notNull(properties.getName(), "GraphQL schema name cannot be null.");
            Assert.notNull(properties.getDescription(), "GraphQL schema description cannot be null.");
            
            return new GraphQLJpaSchemaBuilder(entityManager)
                .name(properties.getName())
                .description(properties.getDescription());
        }

        @Override
        public void setImportMetadata(AnnotationMetadata importMetadata) {
            properties.setEnabled(true);
        }
    }
}
