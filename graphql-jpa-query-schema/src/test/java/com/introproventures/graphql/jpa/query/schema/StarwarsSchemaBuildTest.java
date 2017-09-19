/*
 * Copyright 2017 IntroPro Ventures Inc. and/or its affiliates.
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

package com.introproventures.graphql.jpa.query.schema;

import static org.assertj.core.api.Assertions.assertThat;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;

import graphql.Scalars;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLSchema;

@RunWith(SpringRunner.class)
@SpringBootTest(
    webEnvironment=WebEnvironment.NONE
)
public class StarwarsSchemaBuildTest {

    @SpringBootApplication
    static class TestConfiguration {
        @Bean
        public GraphQLJpaSchemaBuilder graphQLSchemaBuilder(EntityManager entityManager) {
            return new GraphQLJpaSchemaBuilder(entityManager)
                .name("Starwars")
                .description("Starwars Universe Schema");
        }
    }
    
    @Autowired
    private GraphQLJpaSchemaBuilder builder;

    @Before
    public void setup() {
    }

    @Test
    public void correctlyDerivesSchemaFromGivenEntities() {
        //when
        GraphQLSchema schema = builder.build();

        // then
        assertThat(schema)
            .describedAs("Ensure the result is returned")
            .isNotNull();

        //then
        assertThat(schema.getQueryType().getFieldDefinition("Droid").getArgument("id"))
            .describedAs( "Ensure that identity can be queried on")
            .isNotNull();
        
        //then
        assertThat(schema.getQueryType().getFieldDefinition("Droids").getArgument("where"))
            .describedAs( "Ensure that collections can be queried on")
            .isNotNull();

        //then
        assertThat(schema.getQueryType().getFieldDefinition("CodeLists").getArguments())
            .describedAs("Ensure Subobjects may be queried upon")
            .hasSize(2);
        
        assertThat(((GraphQLInputObjectType)((GraphQLInputObjectType) schema.getQueryType()
            .getFieldDefinition("CodeLists").getArgument("where").getType())
            .getField("code").getType())
            .getField("EQ").getType()
        ).isEqualTo(Scalars.GraphQLString);
    }

    @Test
    public void correctlyDerivesPageableSchemaFromGivenEntities() {
        //when
        GraphQLSchema schema = builder.build();

        // then
        assertThat(schema)
            .describedAs("Ensure the schema is generated")
            .isNotNull();

        //then
        assertThat(schema.getQueryType().getFieldDefinition("Droids").getArgument("page"))
            .describedAs( "Ensure that query collection has page argument")
            .isNotNull();

        //then
        assertThat(schema.getQueryType().getFieldDefinition("Droids").getArgument("where"))
            .describedAs( "Ensure that query collection has where argument")
            .isNotNull();
        
        //then
        assertThat(schema.getQueryType()
            .getFieldDefinition("CodeLists").getArguments()
        )
        .describedAs("Ensure query has two arguments")
        .hasSize(2);
        
    }
    
}