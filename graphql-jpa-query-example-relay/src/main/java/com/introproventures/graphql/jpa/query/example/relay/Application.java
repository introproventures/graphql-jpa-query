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
package com.introproventures.graphql.jpa.query.example.relay;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;

import com.introproventures.graphql.jpa.query.autoconfigure.GraphQLJpaQueryProperties;
import com.introproventures.graphql.jpa.query.autoconfigure.GraphQLSchemaConfigurer;
import com.introproventures.graphql.jpa.query.autoconfigure.GraphQLShemaRegistration;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;
import com.introproventures.graphql.jpa.query.schema.model.book.Book;

/**
 * GraphQL JPA Query Example Relay with Spring Boot Autoconfiguration
 * 
 * You can configure GraphQL JPA Query properties in application.yml 
 * 
 * @author Igor Dianov
 *
 */
@SpringBootApplication
@EntityScan(basePackageClasses=Book.class)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    @Configuration
    public static class GraphQLJpaQuerySchemaConfigurer implements GraphQLSchemaConfigurer {

        private final EntityManager entityManager;

        @Autowired
        private GraphQLJpaQueryProperties properties;

        public GraphQLJpaQuerySchemaConfigurer(EntityManager entityManager) {
            this.entityManager = entityManager;
        }

        @Override
        public void configure(GraphQLShemaRegistration registry) {
            registry.register(
                    new GraphQLJpaSchemaBuilder(entityManager)
                        .name(properties.getName())
                        .description(properties.getDescription())
                        .useDistinctParameter(properties.isUseDistinctParameter())
                        .defaultDistinct(properties.isDefaultDistinct())
                        .enableRelay(properties.isEnableRelay())
                        .build()
            );
        }
    }    
    
}
