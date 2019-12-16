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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;

import com.introproventures.graphql.jpa.query.AbstractSpringBootTestSupport;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;

import graphql.Scalars;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;

@SpringBootTest
public class StarwarsSchemaBuildTest extends AbstractSpringBootTestSupport {

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
    
    
    @Test
    public void correctlyDerivesSchemaDescriptionsFromGivenEntities() {
        //when
        GraphQLSchema schema = builder.build();

        // then
        assertThat(schema)
            .describedAs("Ensure the schema is generated")
            .isNotNull();

        //then
        assertThat(schema.getQueryType().getFieldDefinition("Droid").getDescription())
            .describedAs( "Ensure that Droid has the expected description")
            .isEqualTo("Represents an electromechanical robot in the Star Wars Universe");
        
        //then
        assertThat(
                ((GraphQLObjectType)schema.getQueryType().getFieldDefinition("Droid").getType())
                .getFieldDefinition("primaryFunction")
                .getDescription()
        )
            .describedAs( "Ensure that Droid.primaryFunction has the expected description")
            .isEqualTo("Documents the primary purpose this droid serves");

        //then
        assertThat(
                ((GraphQLObjectType)schema.getQueryType().getFieldDefinition("Droid").getType())
                        .getFieldDefinition("id")
                        .getDescription()
        )
                .describedAs( "Ensure that Droid.id has the expected description, inherited from Character")
                .isEqualTo("Primary Key for the Character Class");
        
        //then
        assertThat(
                ((GraphQLObjectType)schema.getQueryType().getFieldDefinition("Droid").getType())
                        .getFieldDefinition("name")
                        .getDescription()
        )
                .describedAs( "Ensure that Droid.name has the expected description, inherited from Character")
                .isEqualTo("Name of the character");

        //then
        assertThat(
                ((GraphQLObjectType)schema.getQueryType().getFieldDefinition("CodeList").getType())
                        .getFieldDefinition("id")
                        .getDescription()
        )
                .describedAs( "Ensure that CodeList.id has the expected description")
                .isEqualTo("Primary Key for the Code List Class");
        
        //then
        assertThat(
                ((GraphQLObjectType)schema.getQueryType().getFieldDefinition("CodeList").getType())
                        .getFieldDefinition("parent")
                        .getDescription()
        )
                .describedAs( "Ensure that CodeList.parent has the expected description")
                .isEqualTo("The CodeList's parent CodeList");
    }

    @Test
    public void testBuildSchema(){
        //given
        GraphQLSchema schema = builder.build();

        //then
        assertThat(schema).isNotNull();
    }
    
}