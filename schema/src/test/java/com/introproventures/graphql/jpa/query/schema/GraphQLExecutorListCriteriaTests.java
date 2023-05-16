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
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaExecutor;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.util.Assert;

@SpringBootTest
public class GraphQLExecutorListCriteriaTests extends AbstractSpringBootTestSupport {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class Application {

        @Bean
        public GraphQLExecutor graphQLExecutor(final GraphQLSchemaBuilder graphQLSchemaBuilder) {
            return new GraphQLJpaExecutor(graphQLSchemaBuilder.build());
        }

        @Bean
        public GraphQLSchemaBuilder graphQLSchemaBuilder(final EntityManager entityManager) {
            return new GraphQLJpaSchemaBuilder(entityManager).name("GraphQLBooks").description("Books JPA test schema");
        }
    }

    @Autowired
    private GraphQLExecutor executor;

    @Test
    public void contextLoads() {
        Assert.isAssignable(GraphQLExecutor.class, executor.getClass());
    }

    @Test
    public void queryWithWhereInsideOneToManyRelationsWithExplictAND() {
        //given:
        String query =
            "query { " +
            "Authors(where: {" +
            "    books: {" +
            "      AND: [{ " +
            "        genre: {IN: NOVEL}" +
            "        title: {LIKE: \"War\"}" +
            "      }]" +
            "    }" +
            "  }) {" +
            "    select {" +
            "      id" +
            "      name" +
            "      books {" +
            "        id" +
            "        title" +
            "        genre" +
            "      }" +
            "    }" +
            "  }" +
            "}";

        String expected =
            "{Authors={select=[" +
            "{id=1, name=Leo Tolstoy, books=[" +
            "{id=2, title=War and Peace, genre=NOVEL}" +
            "]" +
            "}]}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryWithWhereInsideOneToManyRelationsWithExplictANDEXISTS() {
        //given:
        String query =
            "query { " +
            "Authors(where: {" +
            "    EXISTS: {" +
            "      books: {" +
            "        AND: [{ " +
            "          genre: {IN: NOVEL}" +
            "          title: {LIKE: \"War\"}" +
            "        }]" +
            "      }" +
            "    }" +
            "  }) {" +
            "    select {" +
            "      id" +
            "      name" +
            "      books {" +
            "        id" +
            "        title" +
            "        genre" +
            "      }" +
            "    }" +
            "  }" +
            "}";

        String expected =
            "{Authors={select=[" +
            "{id=1, name=Leo Tolstoy, books=[" +
            "{id=2, title=War and Peace, genre=NOVEL}, " +
            "{id=3, title=Anna Karenina, genre=NOVEL}]" +
            "}]}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryWithWhereInsideOneToManyRelationsWithExplictOR() {
        //given:
        String query =
            "query { " +
            "Authors(where: {" +
            "    books: {" +
            "      OR: [{ " +
            "        genre: {IN: NOVEL}" +
            "        title: {LIKE: \"War\"}" +
            "      }]" +
            "    }" +
            "  }) {" +
            "    select {" +
            "      id" +
            "      name" +
            "      books {" +
            "        id" +
            "        title" +
            "        genre" +
            "      }" +
            "    }" +
            "  }" +
            "}";

        String expected =
            "{Authors={select=[" +
            "{id=1, name=Leo Tolstoy, books=[{id=2, title=War and Peace, genre=NOVEL}, " +
            "{id=3, title=Anna Karenina, genre=NOVEL}]}" +
            "]}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryWithWhereInsideOneToManyNestedRelationsWithManyToOneAndOR() {
        //given:
        String query =
            "query { " +
            "  Authors(where: {" +
            "    books: {" +
            "      author: {name: {LIKE:\"Leo\"}}" +
            "      AND: [{" +
            "        OR: [{" +
            "          id: {EQ: 2}" +
            "          title: {LIKE: \"Anna\"}" +
            "        }]" +
            "      }]" +
            "    }" +
            "  }) {" +
            "    select {" +
            "      id" +
            "      name" +
            "      books {" +
            "        id" +
            "        title" +
            "        genre" +
            "      }" +
            "    }" +
            "  }" +
            "}";

        String expected =
            "{Authors={select=[" +
            "{id=1, name=Leo Tolstoy, books=[" +
            "{id=2, title=War and Peace, genre=NOVEL}, " +
            "{id=3, title=Anna Karenina, genre=NOVEL}" +
            "]}" +
            "]}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryWithWhereInsideOneToManyNestedRelationsWithOneToManyDeepSelect() {
        //given:
        String query =
            "query { " +
            "  Authors(where: {" +
            "    books: {" +
            "      author: {name: {LIKE:\"Leo\"}}" +
            "    }" +
            "  }) {" +
            "    select {" +
            "      id" +
            "      name" +
            "      books {" +
            "        id" +
            "        title" +
            "        genre" +
            "        author {" +
            "          name" +
            "        }" +
            "      }" +
            "    }" +
            "  }" +
            "}";

        String expected =
            "{Authors={select=[{" +
            "id=1, name=Leo Tolstoy, books=[" +
            "{id=2, title=War and Peace, genre=NOVEL, author={name=Leo Tolstoy}}, " +
            "{id=3, title=Anna Karenina, genre=NOVEL, author={name=Leo Tolstoy}}" +
            "]}" +
            "]}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryWithWhereInsideOneToManyNestedRelationsListORCriteria() {
        //given:
        String query =
            "query { " +
            "  Authors(where: { " +
            "    books: {" +
            "      OR: [{" +
            "        author: { " +
            "          OR: [" +
            "            { name: {LIKE:\"Leo\"} }" +
            "            { name: {LIKE:\"Anton\"} }" +
            "          ]" +
            "        } " +
            "      }]" +
            "      }" +
            "  }) {" +
            "    select {" +
            "      id" +
            "      name" +
            "      books {" +
            "        id" +
            "        title" +
            "        genre" +
            "      }" +
            "    }" +
            "  }" +
            "}";

        String expected =
            "{Authors={select=[" +
            "{id=1, name=Leo Tolstoy, books=[" +
            "{id=2, title=War and Peace, genre=NOVEL}, " +
            "{id=3, title=Anna Karenina, genre=NOVEL}" +
            "]}, " +
            "{id=4, name=Anton Chekhov, books=[" +
            "{id=5, title=The Cherry Orchard, genre=PLAY}, " +
            "{id=6, title=The Seagull, genre=PLAY}, " +
            "{id=7, title=Three Sisters, genre=PLAY}" +
            "]}" +
            "]}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryWithWhereInsideManyToOneNestedRelationsListORCriteria() {
        //given:
        String query =
            "query { " +
            "  Books(where: { " +
            "    author: {" +
            "      OR: [" +
            "        {name: {LIKE: \"Leo\"}}" +
            "        {name: {LIKE: \"Anton\"}}" +
            "      ]" +
            "    }       " +
            "  }) {" +
            "    select {" +
            "      id" +
            "      title" +
            "      genre" +
            "    }" +
            "  }" +
            "}";

        String expected =
            "{Books={select=[" +
            "{id=2, title=War and Peace, genre=NOVEL}, " +
            "{id=3, title=Anna Karenina, genre=NOVEL}, " +
            "{id=5, title=The Cherry Orchard, genre=PLAY}, " +
            "{id=6, title=The Seagull, genre=PLAY}, " +
            "{id=7, title=Three Sisters, genre=PLAY}" +
            "]}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryWithWhereInsideManyToOneDeepNestedRelationsCompoundListCriteria() {
        //given:
        String query =
            "query { " +
            "  Books(where: { " +
            "    author: {" +
            "      OR: [" +
            "        { " +
            "          AND: [" +
            "            {name: {LIKE: \"Leo\"}}" +
            "            {books: {genre: {EQ: NOVEL}}}" +
            "          ]" +
            "        }" +
            "        {" +
            "          AND: [" +
            "            {name: {LIKE: \"Anton\"}}" +
            "            {books: {genre: {EQ: PLAY}}}" +
            "          ]" +
            "        }" +
            "      ]" +
            "    }       " +
            "  }) {" +
            "    select {" +
            "      id" +
            "      title" +
            "      genre" +
            "    }" +
            "  }" +
            "}";

        String expected =
            "{Books={select=[" +
            "{id=2, title=War and Peace, genre=NOVEL}, " +
            "{id=3, title=Anna Karenina, genre=NOVEL}, " +
            "{id=5, title=The Cherry Orchard, genre=PLAY}, " +
            "{id=6, title=The Seagull, genre=PLAY}, " +
            "{id=7, title=Three Sisters, genre=PLAY}" +
            "]}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryWithWhereCompoundListORCriteria() {
        //given:
        String query =
            "query { " +
            "   Authors(where: { " +
            "    OR: [" +
            "      {name: {LIKE: \"Leo\"}} " +
            "      {name: {LIKE: \"Anton\"}} " +
            "    ]" +
            "  }) {" +
            "    select {" +
            "      id" +
            "      name" +
            "    }" +
            "  }" +
            "}";

        String expected = "{Authors={select=[" + "{id=1, name=Leo Tolstoy}, " + "{id=4, name=Anton Chekhov}" + "]}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryWithWhereANDNestedOneToManyCompoundORListCriteria() {
        //given:
        String query =
            "query { " +
            "  Authors(where: { " +
            "    AND: [" +
            "      { books: { genre: {IN: [PLAY, NOVEL] } } }" +
            "      { " +
            "        OR: [" +
            "            { name: {LIKE: \"Anton\"}} " +
            "            { name: { LIKE: \"Leo\"} }" +
            "          ]" +
            "      }" +
            "    ]" +
            "  }) {" +
            "    select {" +
            "      id" +
            "      name" +
            "    }" +
            "  }" +
            "}";

        String expected = "{Authors={select=[" + "{id=1, name=Leo Tolstoy}, " + "{id=4, name=Anton Chekhov}" + "]}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryWithWhereANDListEXISTSMixedCriteria() {
        //given:
        String query =
            "query { " +
            "  Authors(where: {" +
            "    books: {" +
            "      AND: [{" +
            "        EXISTS: {" +
            "          author: {" +
            "            name: {LIKE: \"Leo\"}" +
            "          }" +
            "        }" +
            "      }, {" +
            "        title: {LIKE: \"War\"}" +
            "      }]" +
            "    }" +
            "  }) {" +
            "    select {" +
            "      id" +
            "      name" +
            "      books {" +
            "        id" +
            "        title" +
            "      }" +
            "    }" +
            "  } " +
            "}";

        String expected =
            "{Authors={select=[" + "{id=1, name=Leo Tolstoy, books=[" + "{id=2, title=War and Peace}" + "]}" + "]}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryWithWhereBooksANDListEXISTSANDImplicitCriteria() {
        //given:
        String query =
            "query { " +
            "  Authors(where: {" +
            "    books: {" +
            "      NOT_EXISTS: [{" +
            "        author: {" +
            "          name: {LIKE: \"Anton\"}" +
            "        }" +
            "      } {" +
            "        author: {" +
            "          name: {LIKE: \"Igor\"}" +
            "        }" +
            "      }]" +
            "    }" +
            "  }) {" +
            "    select {" +
            "      id" +
            "      name" +
            "      books {" +
            "        id" +
            "        title" +
            "      }" +
            "    }" +
            "  }" +
            "}";

        String expected =
            "{Authors={select=[" +
            "{id=1, name=Leo Tolstoy, books=[" +
            "{id=2, title=War and Peace}, " +
            "{id=3, title=Anna Karenina}]}, " +
            "{id=8, name=Igor Dianov, books=[]}" +
            "]}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryWithWhereListEXISTSANDImplicitCriteria() {
        //given:
        String query =
            "query { " +
            "  Authors(where: {" +
            "    EXISTS: [{" +
            "      books: {" +
            "        title: {LIKE: \"War\"}" +
            "      }" +
            "    } {" +
            "      books: {" +
            "        title: {LIKE: \"Anna\"}" +
            "      }" +
            "    }]" +
            "  }) {" +
            "    select {" +
            "      id" +
            "      name" +
            "      books {" +
            "        id" +
            "        title" +
            "      }" +
            "    }" +
            "  }" +
            "}";

        String expected =
            "{Authors={select=[" +
            "{id=1, name=Leo Tolstoy, books=[" +
            "{id=2, title=War and Peace}, " +
            "{id=3, title=Anna Karenina}]}" +
            "]}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryWithWhereANDListEXISTSCriteria() {
        //given:
        String query =
            "query { " +
            "  Authors(where: {" +
            "    AND: [{ " +
            "      EXISTS: {" +
            "        books: {" +
            "          title: {LIKE: \"War\"}" +
            "          id: {EQ: 2}" +
            "        }" +
            "      }" +
            "    }, { " +
            "      EXISTS: {" +
            "        books: {" +
            "          title: {LIKE: \"Anna\"}" +
            "          id: {EQ: 3}" +
            "        }" +
            "      }" +
            "    }]" +
            "  }) {" +
            "    select {" +
            "      id" +
            "      name" +
            "      books {" +
            "        id" +
            "        title" +
            "      }" +
            "    }" +
            "  }" +
            "}";

        String expected =
            "{Authors={select=[" +
            "{id=1, name=Leo Tolstoy, books=[" +
            "{id=2, title=War and Peace}, " +
            "{id=3, title=Anna Karenina}]}" +
            "]}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }
}
