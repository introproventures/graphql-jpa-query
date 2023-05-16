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

import com.introproventures.graphql.jpa.query.autoconfigure.EnableGraphQLJpaQuerySchema;
import com.introproventures.graphql.jpa.query.autoconfigure.GraphQLJPASchemaBuilderCustomizer;
import com.introproventures.graphql.jpa.query.autoconfigure.GraphQLJpaQueryProperties;
import com.introproventures.graphql.jpa.query.schema.model.book.Book;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * GraphQL JPA Query Example Relay with Spring Boot Autoconfiguration
 *
 * You can configure GraphQL JPA Query properties in application.yml
 *
 * @author Igor Dianov
 *
 */
@SpringBootApplication
@EnableGraphQLJpaQuerySchema(basePackageClasses = Book.class)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Configuration
    public static class GraphQLJpaQuerySchemaConfigurer {

        @Bean
        GraphQLJPASchemaBuilderCustomizer graphQLSchemaBuilderCustomizer(GraphQLJpaQueryProperties properties) {
            return builder -> builder.enableRelay(properties.isEnableRelay());
        }
    }
}
