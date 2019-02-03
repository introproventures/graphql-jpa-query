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

import com.introproventures.graphql.jpa.query.schema.GraphQLExecutor;
import com.introproventures.graphql.jpa.query.schema.GraphQLSchemaBuilder;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaExecutor;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;
import com.introproventures.graphql.jpa.query.starter.model.Author;

@RunWith(SpringRunner.class)
@SpringBootTest(
    properties={"spring.graphql.jpa.query.enabled=false"},
    webEnvironment = WebEnvironment.RANDOM_PORT
)
public class EnableGraphQLJpaQueryTest {
    
    @SpringBootApplication(exclude=GraphQLJpaQueryAutoConfiguration.class)
    @EntityScan(basePackageClasses=Author.class)
    @EnableGraphQLJpaQuery
    static class Application {
    }

    @Autowired
    GraphQLJpaQueryProperties  graphQLJpaQueryProperties;

    @Autowired
    GraphQLExecutor graphQLExecutor;

    @Autowired
    GraphQLSchemaBuilder graphQLSchemaBuilder;
    
    @Test
    public void contextIsConfigured() {
        assertThat(graphQLExecutor).isInstanceOf(GraphQLJpaExecutor.class);
        assertThat(graphQLSchemaBuilder).isInstanceOf(GraphQLJpaSchemaBuilder.class);
        
        assertThat(graphQLJpaQueryProperties.getName()).isEqualTo("GraphQLBooks");
        assertThat(graphQLJpaQueryProperties.getDescription()).isEqualTo("GraphQL Books Schema Description");
        assertThat(graphQLJpaQueryProperties.getPath()).isEqualTo("/graphql");
        assertThat(graphQLJpaQueryProperties.isEnabled()).isEqualTo(true);
        
    }
    
}
