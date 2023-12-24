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
import com.introproventures.graphql.jpa.query.schema.model.book_superclass.SuperAuthor;
import com.introproventures.graphql.jpa.query.schema.model.book_superclass.SuperBook;
import com.introproventures.graphql.jpa.query.schema.model.uuid.Thing;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaPrinter;
import jakarta.persistence.EntityManager;
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
                .namingStrategy(new NamingStrategy() {})
                .entityPath(Book.class.getName())
                .entityPath(SuperBook.class.getName())
                .entityPath(SuperAuthor.class.getName())
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

    @Autowired
    private GraphQLJpaSchemaBuilder builder;

    @Test
    public void testBuildSchemaCustomizer() {
        //given
        GraphQLSchema schema = builder.build();

        //then
        assertThat(schema).isNotNull();

        SchemaPrinter schemaPrinter = new SchemaPrinter();

        System.out.println(schemaPrinter.print(schema));
    }
}
