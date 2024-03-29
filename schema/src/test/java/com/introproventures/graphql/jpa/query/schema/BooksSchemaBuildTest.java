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
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenCode;

import com.introproventures.graphql.jpa.query.AbstractSpringBootTestSupport;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;
import com.introproventures.graphql.jpa.query.schema.model.book.Author;
import com.introproventures.graphql.jpa.query.schema.model.book.Book;
import com.introproventures.graphql.jpa.query.schema.model.book_superclass.SuperAuthor;
import com.introproventures.graphql.jpa.query.schema.model.book_superclass.SuperBook;
import com.introproventures.graphql.jpa.query.schema.model.uuid.Thing;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLSchema;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;

@SpringBootTest
public class BooksSchemaBuildTest extends AbstractSpringBootTestSupport {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestConfiguration {

        @Bean
        public GraphQLSchemaBuilder graphQLSchemaBuilder(EntityManager entityManager) {
            return new GraphQLJpaSchemaBuilder(entityManager)
                .name("BooksExampleSchema")
                .entityPath(Book.class.getName())
                .entityPath(SuperBook.class.getName())
                .entityPath(SuperAuthor.class.getName())
                .entityPath(Author.class.getName())
                .entityPath(Thing.class.getPackage().getName())
                .description("Books Example Schema");
        }
    }

    @Autowired
    private GraphQLJpaSchemaBuilder builder;

    @BeforeEach
    public void setup() {}

    @Test
    public void correctlyDerivesSchemaFromGivenEntities() {
        //when
        GraphQLSchema schema = builder.build();

        // then
        assertThat(schema).describedAs("Ensure the result is returned").isNotNull();

        //then
        assertThat(schema.getQueryType().getFieldDefinition("Book").getArgument("id"))
            .describedAs("Ensure that identity can be queried on")
            .isNotNull();

        //then
        assertThat(schema.getQueryType().getFieldDefinition("Books").getArgument("where"))
            .describedAs("Ensure that collections can be queried on")
            .isNotNull();

        //then
        assertThat(schema.getQueryType().getFieldDefinition("Author").getArgument("id"))
            .describedAs("Ensure that collections can be queried on")
            .isNotNull();

        assertThat(schema.getQueryType().getFieldDefinition("Authors").getArgument("where"))
            .describedAs("Ensure that collections can be queried on")
            .isNotNull();
    }

    @Test
    public void correctlyBuildsSchemaForEntitiesWithEntityPath() {
        //when
        GraphQLSchema schema = builder.build();

        // then
        assertThat(schema).describedAs("Ensure the result is returned").isNotNull();

        assertThat(schema.getQueryType().getFieldDefinitions())
            .extracting(GraphQLFieldDefinition::getName)
            .containsOnly(
                "Book",
                "Books",
                "Author",
                "Authors",
                "Thing",
                "Things",
                "SuperBook",
                "SuperBooks",
                "SuperAuthor",
                "SuperAuthors"
            );
    }

    @Test
    public void correctlyDerivesToManyOptionalFromGivenEntities() {
        //when
        GraphQLSchema schema = builder.build();

        // then
        assertThat(schema).describedAs("Ensure the schema is generated").isNotNull();

        //then
        assertThat(getFieldForType("author", "Book", schema))
            .isPresent()
            .get()
            .extracting(it -> it.getArgument("optional"))
            .extracting("defaultValue")
            .extracting("value")
            .isEqualTo(Boolean.FALSE);
    }

    @Test
    public void correctlyDerivesToOneOptionalFromGivenEntities() {
        //when
        GraphQLSchema schema = builder.build();

        // then
        assertThat(schema).describedAs("Ensure the schema is generated").isNotNull();

        //then
        assertThat(getFieldForType("books", "Author", schema))
            .isPresent()
            .get()
            .extracting(it -> it.getArgument("optional"))
            .extracting("defaultValue")
            .extracting("value")
            .isEqualTo(Boolean.TRUE);
    }

    @Test
    public void shouldBuildSchemaWithStringArrayAsStringListType() {
        //given
        //there is a property in the model that is of array type

        //when
        GraphQLSchema schema = builder.build();

        //then
        Optional<GraphQLFieldDefinition> tags = getFieldForType("tags", "SuperBook", schema);
        then(tags)
            .isPresent()
            .get()
            .extracting(GraphQLFieldDefinition::getType)
            .isInstanceOf(GraphQLList.class)
            .extracting("wrappedType")
            .extracting("name")
            .isEqualTo("String");
    }

    @Test
    public void shouldBuildSchemaWithStringArrayAsStringListTypeWithoutAnyError() {
        //given
        //there is a property in the model that is of array type

        //then
        thenCode(() -> builder.build()).doesNotThrowAnyException();
    }

    @Test
    public void testBuildSchema() {
        //given
        GraphQLSchema schema = builder.build();

        //then
        assertThat(schema).isNotNull();
    }

    private Optional<GraphQLFieldDefinition> getFieldForType(String fieldName, String type, GraphQLSchema schema) {
        return schema
            .getQueryType()
            .getFieldDefinition(type)
            .getType()
            .getChildren()
            .stream()
            .map(GraphQLFieldDefinition.class::cast)
            .filter(graphQLFieldDefinition -> graphQLFieldDefinition.getName().equals(fieldName))
            .findFirst();
    }
}
