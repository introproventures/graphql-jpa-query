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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Supplier;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.introproventures.graphql.jpa.query.boot.test.starter.model.Author;
import com.introproventures.graphql.jpa.query.schema.GraphQLExecutionInputFactory;
import com.introproventures.graphql.jpa.query.schema.GraphQLExecutor;
import com.introproventures.graphql.jpa.query.schema.GraphQLSchemaBuilder;
import com.introproventures.graphql.jpa.query.schema.RestrictedKeysProvider;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaExecutor;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaExecutorContextFactory;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;

import graphql.GraphQLContext;
import graphql.execution.instrumentation.Instrumentation;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.visibility.GraphqlFieldVisibility;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class GraphQLJpaQueryAutoConfigurationTest {

    @SpringBootApplication
    @EntityScan(basePackageClasses=Author.class)
    static class Application {
        @MockBean
        private GraphQLExecutionInputFactory mockExecutionInputFactory;

        @MockBean
        private Supplier<Instrumentation> mockInstrumentation;

        @MockBean
        private Supplier<GraphqlFieldVisibility> mockGraphqlFieldVisibility;
        
        @MockBean
        private Supplier<GraphQLContext> graphqlContext;
        
        @MockBean
        private RestrictedKeysProvider restrictedKeysProvider;
    }
    
    @Autowired(required=false)
    private GraphQLExecutor graphQLExecutor;

    @Autowired(required=false)
    private GraphQLSchemaBuilder graphQLSchemaBuilder;
    
    @Autowired(required=false)
    private GraphQLJpaExecutorContextFactory executorContextFactory;   

    @Autowired
    private ObjectProvider<GraphQLExecutionInputFactory> executionInputFactory;   

    @Autowired
    private ObjectProvider<Supplier<Instrumentation>> instrumentation;   
    
    @Autowired
    private ObjectProvider<Supplier<GraphqlFieldVisibility>> graphqlFieldVisibility;   

    @Autowired
    private ObjectProvider<Supplier<GraphQLContext>> graphqlContext;   

    @Autowired
    private ObjectProvider<RestrictedKeysProvider> restrictedKeysObjectProvider;   
    
    @Autowired
    private GraphQLSchema graphQLSchema;
    
    @Test
    public void contextIsAutoConfigured() {
        assertThat(graphQLExecutor).isNotNull()
                                   .isInstanceOf(GraphQLJpaExecutor.class);
        
        assertThat(graphQLSchemaBuilder).isNotNull()
                                        .isInstanceOf(GraphQLJpaSchemaBuilder.class);

        assertThat(GraphQLJpaSchemaBuilder.class.cast(graphQLSchemaBuilder)
                                                .getRestrictedKeysProvider()).isEqualTo(restrictedKeysObjectProvider.getObject());
        
        assertThat(executorContextFactory).isNotNull()
                                          .isInstanceOf(GraphQLJpaExecutorContextFactory.class);
        
        assertThat(executionInputFactory.getIfAvailable()).isNotNull();
        assertThat(instrumentation.getIfAvailable()).isNotNull();
        assertThat(graphqlFieldVisibility.getIfAvailable()).isNotNull();
        assertThat(graphqlContext.getIfAvailable()).isNotNull();

        assertThat(executorContextFactory.getExecutionInputFactory()).isEqualTo(executionInputFactory.getObject());
        assertThat(executorContextFactory.getInstrumentation()).isEqualTo(instrumentation.getObject());
        assertThat(executorContextFactory.getGraphqlFieldVisibility()).isEqualTo(graphqlFieldVisibility.getObject());
        assertThat(executorContextFactory.getGraphqlContext()).isEqualTo(graphqlContext.getObject());
        
        assertThat(graphQLSchema.getQueryType())
                                .extracting(GraphQLObjectType::getName, GraphQLObjectType::getDescription)
                                .containsExactly("GraphQLBooks", "GraphQL Books Schema Description");
    }
}