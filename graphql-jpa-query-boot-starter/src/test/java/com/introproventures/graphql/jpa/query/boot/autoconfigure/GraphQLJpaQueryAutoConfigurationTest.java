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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

import com.introproventures.graphql.jpa.query.schema.GraphQLExecutionInputFactory;
import com.introproventures.graphql.jpa.query.schema.GraphQLExecutor;
import com.introproventures.graphql.jpa.query.schema.GraphQLExecutorContextFactory;
import com.introproventures.graphql.jpa.query.schema.GraphQLSchemaBuilder;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaExecutor;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaExecutorContextFactory;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;
import com.introproventures.graphql.jpa.query.starter.model.Author;

import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class GraphQLJpaQueryAutoConfigurationTest {

    @SpringBootApplication
    @EntityScan(basePackageClasses=Author.class)
    static class Application {
    }
    
    @Autowired(required=false)
    private GraphQLExecutor graphQLExecutor;

    @Autowired(required=false)
    private GraphQLSchemaBuilder graphQLSchemaBuilder;
    
    @Autowired(required=false)
    private GraphQLExecutorContextFactory executorContextFactory;   

    @Autowired(required=false)
    private GraphQLExecutionInputFactory executionInputFactory;   
    
    @Autowired
    private GraphQLSchema graphQLSchema;
    
    @Test
    public void contextIsAutoConfigured() {
        assertThat(graphQLExecutor).isNotNull()
                                   .isInstanceOf(GraphQLJpaExecutor.class);
        
        assertThat(graphQLSchemaBuilder).isNotNull()
                                        .isInstanceOf(GraphQLJpaSchemaBuilder.class);
        
        assertThat(executorContextFactory).isNotNull()
                                          .isInstanceOf(GraphQLJpaExecutorContextFactory.class);

        assertThat(executionInputFactory).isNotNull()
                                          .isInstanceOf(GraphQLExecutionInputFactory.class);
        
        assertThat(graphQLSchema.getQueryType())
                                .extracting(GraphQLObjectType::getName, GraphQLObjectType::getDescription)
                                .containsExactly("GraphQLBooks", "GraphQL Books Schema Description");
    }
}