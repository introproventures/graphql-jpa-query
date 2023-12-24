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

import com.introproventures.graphql.jpa.query.AbstractSpringBootTestSupport;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;
import com.introproventures.graphql.jpa.query.schema.model.book.Author;
import com.introproventures.graphql.jpa.query.schema.model.book.Book;
import com.introproventures.graphql.jpa.query.schema.model.uuid.Thing;
import graphql.schema.idl.SchemaPrinter;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;

@SpringBootTest
public class BooksSchemaCustomizerBuildTest extends AbstractSpringBootTestSupport {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestConfiguration {

        @Bean
        public GraphQLSchemaBuilder graphQLSchemaBuilder(EntityManager entityManager) {
            return new GraphQLJpaSchemaBuilder(entityManager)
                .name("BooksExampleSchema")
                .namingStrategy(new CustomNamingStrategy())
                .entityPath(Book.class.getName())
                .entityPath(Author.class.getName())
                .entityPath(Thing.class.getPackage().getName())
                .queryByIdFieldNameCustomizer("find%sById"::formatted)
                .queryAllFieldNameCustomizer("findAll%s"::formatted)
                .queryResultTypeNameCustomizer("Query%sResult"::formatted)
                .queryTypeNameCustomizer("%sQueries"::formatted)
                .queryEmbeddableTypeNameCustomizer("%sCustomEmbeddableType"::formatted)
                .subqueryArgumentTypeNameCustomizer("%sCustomSubqueryCriteriaExpression"::formatted)
                .queryWhereArgumentTypeNameCustomizer("%sCustomWhereArgumentType"::formatted)
                .queryWhereInputTypeNameCustomizer("%sCustomWhereInputType"::formatted)
                .description("Books Example Schema");
        }
    }

    private static String schema;

    @BeforeAll
    static void buildSchema(@Autowired GraphQLJpaSchemaBuilder builder) {
        schema = new SchemaPrinter().print(builder.build());

        System.out.println(schema);
    }

    @Test
    public void queryByIdFieldNameCustomizer() {
        //then
        assertThat(schema).contains("findAuthorEntityById", "findBookEntityById");
    }

    @Test
    public void queryAllFieldNameCustomizer() {
        //then
        assertThat(schema).contains("findAllAuthorsEntities", "findAllBooksEntities");
    }

    @Test
    public void queryResultTypeNameCustomizer() {
        //then
        assertThat(schema)
            .contains("type QueryAuthorEntityResult", "type QueryBookEntityResult", "type QueryThingEntityResult");
    }

    @Test
    public void queryTypeNameCustomizer() {
        //then
        assertThat(schema).contains("query: BooksExampleSchemaQueries", "type BooksExampleSchemaQueries");
    }

    @Test
    public void queryEmbeddableTypeNameCustomizer() {
        //then
        assertThat(schema)
            .contains("type PublisherEntityCustomEmbeddableType", "publishers: [PublisherEntityCustomEmbeddableType]");
    }

    @Test
    public void subqueryArgumentTypeNameCustomizer() {
        //then
        assertThat(schema)
            .contains(
                "input AuthorsEntitiesCustomSubqueryCriteriaExpression",
                "input BooksEntitiesCustomSubqueryCriteriaExpression"
            );
    }

    @Test
    public void queryWhereArgumentTypeNameCustomizer() {
        //then
        assertThat(schema)
            .contains(
                "input AuthorsEntitiesCustomWhereArgumentType",
                "where: AuthorsEntitiesCustomWhereArgumentType",
                "input BooksEntitiesCustomWhereArgumentType",
                "where: BooksEntitiesCustomWhereArgumentType"
            );
    }

    @Test
    public void queryWhereInputTypeNameCustomizer() {
        //then
        assertThat(schema)
            .contains(
                "books: BooksEntitiesCustomWhereInputType",
                "input PublishersEntitiesCustomWhereInputType",
                "input AuthorsEntitiesCustomWhereInputType",
                "author: AuthorsEntitiesCustomWhereInputType"
            );
    }

    @Test
    public void namingStrategy() {
        //then
        assertThat(schema)
            .contains(
                "input countryEntityPublisherCriteria",
                "input genreEntityAuthorCriteria",
                "input genreEntityBookCriteria",
                "input idEntityAuthorCriteria"
            );
    }

    static class CustomNamingStrategy implements NamingStrategy {

        @Override
        public String singularize(String word) {
            return NamingStrategy.super.singularize(word).concat("Entity");
        }

        @Override
        public String pluralize(String word) {
            return NamingStrategy.super.pluralize(word).concat("Entities");
        }
    }
}
