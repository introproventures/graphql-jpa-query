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
package com.introproventures.graphql.jpa.query.boot.test.boot.autoconfigure;

import com.introproventures.graphql.jpa.query.boot.autoconfigure.JavaScalarsRuntimeWiringConfigurer;
import com.introproventures.graphql.jpa.query.boot.test.starter.model.Author;
import com.introproventures.graphql.jpa.query.schema.GraphQLSchemaBuilder;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.graphql.ExecutionGraphQlService;
import org.springframework.graphql.execution.BatchLoaderRegistry;
import org.springframework.graphql.execution.GraphQlSource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class GraphQLJpaQueryBootStarterGraphQlAutoConfigurationTest {

    @SpringBootApplication
    @EntityScan(basePackageClasses=Author.class)
    static class Application {
    }
    
    @Autowired
    private JavaScalarsRuntimeWiringConfigurer javaScalarsRuntimeWiringConfigurer;

    @Autowired
    private GraphQLSchemaBuilder graphQLSchemaBuilder;
    
    @Autowired
    private GraphQLSchema graphQLSchema;

    @Autowired(required = false)
    private GraphQlSource graphQlSource;

    @Autowired
    BatchLoaderRegistry batchLoaderRegistry;

    @Autowired
    ExecutionGraphQlService executionGraphQlService;

    @Test
    public void contextIsAutoConfigured() {
        assertThat(javaScalarsRuntimeWiringConfigurer).isNotNull();
        
        assertThat(graphQLSchemaBuilder).isNotNull()
                                        .isInstanceOf(GraphQLJpaSchemaBuilder.class);

        assertThat(graphQLSchema.getQueryType())
                                .extracting(GraphQLObjectType::getName, GraphQLObjectType::getDescription)
                                .containsExactly("GraphQLBooks", "GraphQL Books Schema Description");

        assertThat(graphQlSource).isNotNull();

    }
}