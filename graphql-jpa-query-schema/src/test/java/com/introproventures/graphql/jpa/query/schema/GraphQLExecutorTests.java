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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaExecutor;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;
import graphql.ErrorType;
import graphql.ExecutionResult;
import graphql.GraphQLError;
import graphql.validation.ValidationError;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.NONE)
@TestPropertySource({"classpath:hibernate.properties"})
public class GraphQLExecutorTests {
    
    @SpringBootApplication
    static class Application {
        @Bean
        public GraphQLExecutor graphQLExecutor(final GraphQLSchemaBuilder graphQLSchemaBuilder) {
            return new GraphQLJpaExecutor(graphQLSchemaBuilder.build());
        }

        @Bean
        public GraphQLSchemaBuilder graphQLSchemaBuilder(final EntityManager entityManager) {
            
            return new GraphQLJpaSchemaBuilder(entityManager)
                .name("GraphQLBooks")
                .description("Books JPA test schema");
        }
        
    }
    
    @Autowired
    private GraphQLExecutor executor;
    
    @Test
    public void contextLoads() {
        Assert.isAssignable(GraphQLExecutor.class, executor.getClass());
    }
    
    @Test
    public void GetsAllThings() {
        //given
        String query = "query AllThingsQuery { Things { select {id type } } }";
        String expected = "{Things={select=[{id=2d1ebc5b-7d27-4197-9cf0-e84451c5bbb1, type=Thing1}]}}";

        //when
        Object result = executor.execute(query).getData();

        //then
        assertThat(result.toString()).isEqualTo(expected);
        
    }

    @Test
    public void queryForThingById() {
        //given
        String query = "query ThingByIdQuery { Thing(id: \"2d1ebc5b-7d27-4197-9cf0-e84451c5bbb1\") { id type } }";
        
        String expected = "{Thing={id=2d1ebc5b-7d27-4197-9cf0-e84451c5bbb1, type=Thing1}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @SuppressWarnings( { "rawtypes", "serial", "unchecked" } )
    @Test
    public void queryWithParameter() {
        //given:
        String query = " query ThingByIdQuery($id: UUID) { Thing(id: $id) { id type } }";
        String expected = "{Thing={id=2d1ebc5b-7d27-4197-9cf0-e84451c5bbb1, type=Thing1}}";

        //when:
        Object result = executor.execute(query, new HashMap() {{
            put("id","2d1ebc5b-7d27-4197-9cf0-e84451c5bbb1");}}
        ).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryEmptyParameter() {
        //given:
        String query = " query { Thing { id type } }";
        String expected = "{Thing=null}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }
    
    @SuppressWarnings( { "rawtypes", "unchecked", "serial" } )
    @Test
    public void queryWithParameterNoResult() {
        //given:
        String query = " query ThingByIdQuery($id: UUID) { Things(where: {id: {EQ:$id}}) { select {id type } }}";
        String expected = "{Things={select=[]}}";

        //when:
        Object result = executor.execute(query, new HashMap() {{put("id", null);}}).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }

    @SuppressWarnings( { "rawtypes", "unchecked", "serial" } )
    @Test
    public void queryForThingByIdViaWhere() {
        //given:

        String query = "query { Things(where: {id: {EQ: \"2d1ebc5b-7d27-4197-9cf0-e84451c5bbb1\"}}) { select { id }}}";
        String expected = "{Things={select=[{id=2d1ebc5b-7d27-4197-9cf0-e84451c5bbb1}]}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }

    @SuppressWarnings( { "rawtypes", "unchecked", "serial" } )
    @Test
    public void queryForThingByIdViaWhereIN() {
        //given:

        String query = "query { Things(where: {id: {IN: [\"2d1ebc5b-7d27-4197-9cf0-e84451c5bbb1\", \"2d1ebc5b-7d27-4197-9cf0-e84451c5bbb1\" ]}}) { select { id }}}";
        String expected = "{Things={select=[{id=2d1ebc5b-7d27-4197-9cf0-e84451c5bbb1}]}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }

    @SuppressWarnings( { "rawtypes", "unchecked", "serial" } )
    @Test
    public void queryForThingByIdViaWhereNE() {
        //given:

        String query = "query { Things(where: {id: {NE: \"2d1ebc5b-7d27-4197-9cf0-e84451c5bbb2\"}}) { select { id }}}";
        String expected = "{Things={select=[{id=2d1ebc5b-7d27-4197-9cf0-e84451c5bbb1}]}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);

        query = "query { Things(where: {id: {NE: \"2d1ebc5b-7d27-4197-9cf0-e84451c5bbb1\"}}) { select { id }}}";
        expected = "{Things={select=[]}}";

        //when:
        result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryWithAlias() {
        //given:
        String query = "{ t1:  Thing(id: \"2d1ebc5b-7d27-4197-9cf0-e84451c5bbb1\") { id type } }";

        String expected = "{t1={id=2d1ebc5b-7d27-4197-9cf0-e84451c5bbb1, type=Thing1}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }
    
    
    // https://github.com/introproventures/graphql-jpa-query/issues/33
    @Test
    public void queryForElementCollection() {
        //given
        String query = "{ Author(id: 1) { id name, phoneNumbers } }";
        
        String expected = "{Author={id=1, name=Leo Tolstoy, phoneNumbers=[1-123-1234, 1-123-5678]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryForEnumIn() {
        //given
        String query = "{ Books(where: {genre: {IN: PLAY}}) { select { id title, genre } }}";
        
        String expected = "{Books={select=["
        		+ "{id=5, title=The Cherry Orchard, genre=PLAY}, "
        		+ "{id=6, title=The Seagull, genre=PLAY}, "
        		+ "{id=7, title=Three Sisters, genre=PLAY}"
        		+ "]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }
    
    @Test
    public void queryForEnumInArray() {
        //given
        String query = "{ Books(where: {genre: {IN: [NOVEL, PLAY]}}) { select { id title, genre } }}";
        
        String expected = "{Books={select=["
        		+ "{id=2, title=War and Peace, genre=NOVEL}, "
        		+ "{id=3, title=Anna Karenina, genre=NOVEL}, "
        		+ "{id=5, title=The Cherry Orchard, genre=PLAY}, "
        		+ "{id=6, title=The Seagull, genre=PLAY}, "
        		+ "{id=7, title=Three Sisters, genre=PLAY}"
        		+ "]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryForEnumNinArray() {
        //given
        String query = "{ Books(where: {genre: {NIN: [NOVEL]}}) { select { id title, genre } }}";
        
        String expected = "{Books={select=["
        		+ "{id=5, title=The Cherry Orchard, genre=PLAY}, "
        		+ "{id=6, title=The Seagull, genre=PLAY}, "
        		+ "{id=7, title=Three Sisters, genre=PLAY}"
        		+ "]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }
    
    @Test
    public void queryForEnumEq() {
        //given
        String query = "{ Books(where: {genre: {EQ: NOVEL}}) { select { id title, genre } }}";
        
        String expected = "{Books={select=["
        		+ "{id=2, title=War and Peace, genre=NOVEL}, "
        		+ "{id=3, title=Anna Karenina, genre=NOVEL}"
        		+ "]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryForEnumNe() {
        //given
        String query = "{ Books(where: {genre: {NE: PLAY}}) { select { id title, genre } }}";
        
        String expected = "{Books={select=["
        		+ "{id=2, title=War and Peace, genre=NOVEL}, "
        		+ "{id=3, title=Anna Karenina, genre=NOVEL}"
        		+ "]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }
    
    @Test
    public void queryForEnumNin() {
        //given
        String query = "{ Books(where: {genre: {NIN: PLAY}}) { select { id title, genre } }}";
        
        String expected = "{Books={select=["
        		+ "{id=2, title=War and Peace, genre=NOVEL}, "
        		+ "{id=3, title=Anna Karenina, genre=NOVEL}"
        		+ "]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }
        
    @Test
    public void queryForParentWithEnum() {
        //given
        String query = "{ Books { select { id title, author( where: { genre: { EQ: NOVEL } }) { name } } } }";
        
        String expected = "{Books={select=["
        		+ "{id=2, title=War and Peace, author={name=Leo Tolstoy}}, "
        		+ "{id=3, title=Anna Karenina, author={name=Leo Tolstoy}}"
        		+ "]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }
    
    @Test
    public void queryAuthorBooksWithExplictOptional() {
        //given
        String query = "query { "
                + "Authors(" + 
                "    where: {" + 
                "      books: {" + 
                "        title: {LIKE: \"War\"}" + 
                "      }" + 
                "    }" + 
                "  ) {" + 
                "    select {" + 
                "      id" + 
                "      name" + 
                "      books(optional: true) {" + 
                "        id" + 
                "        title(orderBy: ASC)" + 
                "        genre" + 
                "      }" + 
                "    }" + 
                "  }"
                + "}";
        
        String expected = "{Authors={select=["
                + "{id=1, name=Leo Tolstoy, books=[{id=3, title=Anna Karenina, genre=NOVEL}, "
                + "{id=2, title=War and Peace, genre=NOVEL}]}"
                + "]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }
        
    @Test
    public void queryAuthorBooksWithIsNullId() {
        //given
        String query = "query { "
                + "Authors(" + 
                "    where: {" + 
                "      books: {" + 
                "        id: {IS_NULL: true}" + 
                "      }" + 
                "    }" + 
                "  ) {" + 
                "    select {" + 
                "      id" + 
                "      name" + 
                "      books {" + 
                "        id" + 
                "        title" + 
                "        genre" + 
                "      }" + 
                "    }" + 
                "  }"
                + "}";
        
        String expected = "{Authors={select=[]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }
    
    
    // https://github.com/introproventures/graphql-jpa-query/issues/30
    @Test
    public void queryForEntityWithMappedSuperclass() {
        //given
        String query = "{ Car(id: \"1\") { id brand } }";
        
        String expected = "{Car={id=1, brand=Ford}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    // https://github.com/introproventures/graphql-jpa-query/issues/30
    @Test
    public void queryForEntityWithEmbeddedIdAndEmbeddedField() {
        //given
        String query = "{ Boat(boatId: {id: \"1\" country: \"EN\"}) { boatId {id country} engine { identification } } }";
        
        String expected = "{Boat={boatId={id=1, country=EN}, engine={identification=12345}}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryForEntityWithEmbeddedFieldWithWhere() {
        //given
        String query = "{ Boats { select { boatId {id country} engine(where: { identification: { EQ: \"12345\"}}) { identification } } } }";

        String expected = "{Boats={select=[{boatId={id=1, country=EN}, engine={identification=12345}}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }
 
    @Test
    public void queryWithNumericBetweenPredicate() {
        //given:
        String query = "query { Books ( where: { id: {BETWEEN: [2, 5]}}) { select { id title} } }";

        String expected = "{Books={select=[" +
                "{id=2, title=War and Peace}, " +
                "{id=3, title=Anna Karenina}, " +
                "{id=5, title=The Cherry Orchard}" +
                "]}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryWithNumericNotBetweenPredicate() {
        //given:
        String query = "query { Books ( where: { id: {NOT_BETWEEN: [2, 5]}}) { select { id title} } }";

        String expected = "{Books={select=[" +
                "{id=6, title=The Seagull}, " +
                "{id=7, title=Three Sisters}" +
                "]}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryWithDateBetweenPredicate() {
        //given:
        String query = "query { Books ( where: { publicationDate: {BETWEEN: [\"1869-01-01\", \"1896-01-01\"]}}) { select { id title publicationDate} } }";

        String expected = "{Books={select=[" +
                "{id=2, title=War and Peace, publicationDate=1869-01-01 00:00:00.0}, " +
                "{id=3, title=Anna Karenina, publicationDate=1877-04-01 00:00:00.0}" +
                "]}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryWithDateNotBetweenPredicate() {
        //given:
        String query = "query { Books ( where: { publicationDate: {NOT_BETWEEN: [\"1869-01-01\", \"1896-01-01\"]}}) { select { id title publicationDate} } }";

        String expected = "{Books={select=[" +
                "{id=5, title=The Cherry Orchard, publicationDate=1904-01-17 00:00:00.0}, " +
                "{id=6, title=The Seagull, publicationDate=1896-10-17 00:00:00.0}, " +
                "{id=7, title=Three Sisters, publicationDate=1900-01-01 00:00:00.0}" +
                "]}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }    
    

    @Test
    public void queryForEntitiesWithWithEmbeddedIdWithWhere() {
        //given
        String query = "{ Boats { select { boatId(where: { id: { LIKE: \"1\"} country: { EQ: \"EN\"}}) {id country} engine { identification } } } }";

        String expected = "{Boats={select=[{boatId={id=1, country=EN}, engine={identification=12345}}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }
    
    @Test
    public void queryForBooksWithWhereAuthorById() {
        //given
        String query = "query { "
                + "Books(where: {author: {id: {EQ: 1}}}) {" + 
                "    select {" + 
                "      id" + 
                "      title" + 
                "      genre" + 
                "      author {" + 
                "        id" + 
                "        name" + 
                "      }" + 
                "    }" + 
                "  }"+
                "}";

        String expected = "{Books={select=["
                + "{id=2, title=War and Peace, genre=NOVEL, author={id=1, name=Leo Tolstoy}}, "
                + "{id=3, title=Anna Karenina, genre=NOVEL, author={id=1, name=Leo Tolstoy}}"
                + "]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }    
    
    @Test
    public void queryForBooksWithWhereAuthorEqIdWithVariables() {
        //given
        String query = "query($authorId: Long ) { "
                + "  Books(where: {" + 
                "    author: {id: {EQ: $authorId}}" + 
                "  }) {" + 
                "    select {" + 
                "      id" + 
                "      title" + 
                "      genre" + 
                "    }" + 
                "  }"+
                "}";
        Map<String, Object> variables = new HashMap<String, Object>() {{
            put("authorId", 1L);
        }};


        String expected = "{Books={select=["
                + "{id=2, title=War and Peace, genre=NOVEL}, "
                + "{id=3, title=Anna Karenina, genre=NOVEL}"
                + "]}}";

        //when
        Object result = executor.execute(query, variables).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }    
        
    
    
    @Test
    public void queryForAuthorssWithWhereBooksGenreEquals() {
        //given
        String query = "query { "
                + "Authors(where: {books: {genre: {EQ: NOVEL}}}) {" + 
                "    select {" + 
                "      id" + 
                "      name" + 
                "      books {" + 
                "        id" + 
                "        title" + 
                "        genre" + 
                "      }" + 
                "    }" + 
                "  }"+
                "}";

        String expected = "{Authors={select=["
                + "{id=1, name=Leo Tolstoy, books=["
                +   "{id=2, title=War and Peace, genre=NOVEL}, "
                +   "{id=3, title=Anna Karenina, genre=NOVEL}"
                + "]}"
                + "]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }        
    
    @Test
    public void queryForAuthorssWithWhereBooksManyToOneRelationCriteria() {
        //given
        String query = "query { " +
                "  Authors(where: {" + 
                "    books: {" + 
                "      author: {" + 
                "        name: {LIKE: \"Leo\"}" + 
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
                "        author {" + 
                "          name" + 
                "        }" + 
                "      }" + 
                "    }" + 
                "  }" +
                "}";

        String expected = "{Authors={select=[{"
                +   "id=1, name=Leo Tolstoy, books=["
                +       "{id=2, title=War and Peace, genre=NOVEL, author={name=Leo Tolstoy}}, "
                +       "{id=3, title=Anna Karenina, genre=NOVEL, author={name=Leo Tolstoy}}"
                +   "]}"
                + "]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }        
        

    @Test
    public void queryWithWhereInsideOneToManyRelationsImplicitAND() {
        //given:
        String query = "query { "
                + "Authors(where: {" + 
                "    books: {" + 
                "      genre: {IN: NOVEL}" + 
                "      title: {LIKE: \"War\"}" + 
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

        String expected = "{Authors={select=[{"
                +   "id=1, "
                +   "name=Leo Tolstoy, "
                +   "books=["
                +       "{id=2, title=War and Peace, genre=NOVEL}, "
                +       "{id=3, title=Anna Karenina, genre=NOVEL}"
                +   "]}"
                + "]}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }    

    @Test
    public void queryWithWhereInsideOneToManyRelationsWithExplictAND() {
        //given:
        String query = "query { "
                + "Authors(where: {" + 
                "    books: {" + 
                "      AND: { "+
                "        genre: {IN: NOVEL}" + 
                "        title: {LIKE: \"War\"}" +
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

        String expected = "{Authors={select=["
                + "{id=1, name=Leo Tolstoy, books=["
                +   "{id=2, title=War and Peace, genre=NOVEL}, "
                +   "{id=3, title=Anna Karenina, genre=NOVEL}]}"
                + "]}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }    
    
    @Test
    public void queryWithWhereInsideOneToManyRelationsWithExplictOR() {
        //given:
        String query = "query { "
                + "Authors(where: {" + 
                "    books: {" + 
                "      OR: { "+
                "        genre: {IN: NOVEL}" + 
                "        title: {LIKE: \"War\"}" +
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

        String expected = "{Authors={select=["
                + "{id=1, name=Leo Tolstoy, books=[{id=2, title=War and Peace, genre=NOVEL}, "
                + "{id=3, title=Anna Karenina, genre=NOVEL}]}"
                + "]}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }    
    
    @Test
    public void queryWithWhereInsideOneToManyNestedRelationsWithManyToOneAndOR() {
        //given:
        String query = "query { " +
                "  Authors(where: {" + 
                "    books: {" + 
                "      author: {name: {LIKE:\"Leo\"}}" +
                "      AND: {" +
                "        OR: {" + 
                "          id: {EQ: 2}" + 
                "          title: {LIKE: \"Anna\"}" + 
                "        }" + 
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

        String expected = "{Authors={select=["
                + "{id=1, name=Leo Tolstoy, books=["
                +   "{id=2, title=War and Peace, genre=NOVEL}, "
                +   "{id=3, title=Anna Karenina, genre=NOVEL}"
                + "]}"
                + "]}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }    
    
    @Test
    public void queryWithWhereInsideOneToManyNestedRelationsWithOneToManyDeepSelect() {
        //given:
        String query = "query { " +
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

        String expected = "{Authors={select=[{"
                + "id=1, name=Leo Tolstoy, books=["
                +   "{id=2, title=War and Peace, genre=NOVEL, author={name=Leo Tolstoy}}, "
                +   "{id=3, title=Anna Karenina, genre=NOVEL, author={name=Leo Tolstoy}}"
                + "]}"
                + "]}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }    
    
    
    @Test
    public void queryWithWhereInsideManyToOneNestedRelationsWithOnToManyCollectionFilter() {
        //given:
        String query = "query { " +
                "  Books(where: {" + 
                "    title:{LIKE: \"War\"}" + 
                "    author: {" + 
                "      name:{LIKE: \"Leo\"}" + 
                "      books: {title: {LIKE: \"Anna\"}}" + 
                "    }" + 
                "  }) {" + 
                "    select {" + 
                "      id" + 
                "      title" + 
                "      genre" + 
                "      author {" + 
                "        id" + 
                "        name" + 
                "        books {" + 
                "          id" + 
                "          title" + 
                "          genre" + 
                "        }" + 
                "      }" + 
                "    }" + 
                "  }" +
                "}";

        String expected = "{Books={select=[{"
                + "id=2, "
                + "title=War and Peace, genre=NOVEL, "
                + "author={"
                +   "id=1, "
                +   "name=Leo Tolstoy, "
                +   "books=["
                +       "{id=2, title=War and Peace, genre=NOVEL}, "
                +       "{id=3, title=Anna Karenina, genre=NOVEL}"
                +   "]}"
                + "}]}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }    
    
    @Test
    public void queryWithOneToManyNestedRelationsWithManyToOneOptionalTrue() {
        //given:
        String query = "query { " +
                "  Authors {" + 
                "    select {" + 
                "      id" + 
                "      name" + 
                "      books {" + 
                "        id" + 
                "        title" + 
                "        genre" + 
                "        author(optional: true) {" + 
                "          id" + 
                "        }" + 
                "      }" + 
                "    }" + 
                "  }" +
                "}";

        String expected = "{Authors={select=["
                + "{id=1, name=Leo Tolstoy, books=["
                +   "{id=2, title=War and Peace, genre=NOVEL, author={id=1}}, "
                +   "{id=3, title=Anna Karenina, genre=NOVEL, author={id=1}}"
                + "]}, "
                + "{id=4, name=Anton Chekhov, books=["
                +   "{id=5, title=The Cherry Orchard, genre=PLAY, author={id=4}}, "
                +   "{id=6, title=The Seagull, genre=PLAY, author={id=4}}, "
                +   "{id=7, title=Three Sisters, genre=PLAY, author={id=4}}"
                + "]}, "
                + "{id=8, name=Igor Dianov, books=[]}"
                + "]}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }    

    @Test
    public void queryWithOneToManyNestedRelationsWithManyToOneOptionalFalse() {
        //given:
        String query = "query { " +
                "  Authors {" + 
                "    select {" + 
                "      id" + 
                "      name" + 
                "      books {" + 
                "        id" + 
                "        title" + 
                "        genre" + 
                "        author(optional: false) {" + 
                "          id" + 
                "        }" + 
                "      }" + 
                "    }" + 
                "  }" +
                "}";

        String expected = "{Authors={select=["
                + "{id=1, name=Leo Tolstoy, books=["
                +   "{id=2, title=War and Peace, genre=NOVEL, author={id=1}}, "
                +   "{id=3, title=Anna Karenina, genre=NOVEL, author={id=1}}"
                + "]}, "
                + "{id=4, name=Anton Chekhov, books=["
                +   "{id=5, title=The Cherry Orchard, genre=PLAY, author={id=4}}, "
                +   "{id=6, title=The Seagull, genre=PLAY, author={id=4}}, "
                +   "{id=7, title=Three Sisters, genre=PLAY, author={id=4}}"
                + "]}"
                + "]}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }    
    
    @Test
    public void ignoreFilter() {
        //given
        String query = "{ Books(where: {description: {LIKE: \"%Chekhov%\"}} ) { select { id title description} }}";

        ExecutionResult res = executor.execute(query);


        List<GraphQLError> result = executor.execute(query).getErrors();

        //then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isExactlyInstanceOf(ValidationError.class)
                .extracting(ValidationError.class::cast)
                .extracting("errorType", "queryPath")
                .contains(ErrorType.ValidationError, Arrays.asList("Books"));

    }

    @Test
    public void ignoreSubFilter() {
        //given
        String query = "query { "
                + "Authors(where: {" +
                "    books: {" +
                "      OR: { "+
                "        genre: {IN: NOVEL}" +
                "        description: {LIKE: \"War\"}" +
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

        List<GraphQLError> result = executor.execute(query).getErrors();

        //then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isExactlyInstanceOf(ValidationError.class)
                .extracting(ValidationError.class::cast)
                .extracting("errorType", "queryPath")
                .contains(ErrorType.ValidationError, Arrays.asList("Authors"));

    }

    @Test
    public void ignoreOrder() {
        //given
        String query = "{ Books{ select { id description(orderBy:ASC) } }}";

        List<GraphQLError> result = executor.execute(query).getErrors();

        //then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isExactlyInstanceOf(ValidationError.class)
                .extracting(ValidationError.class::cast)
                .extracting("errorType", "queryPath")
                .contains(ErrorType.ValidationError, Arrays.asList("Books", "select", "description"));

    }

    @Test
    public void titleOrder() {
        //given
        String query = "{ Books{ select { id title(orderBy:ASC) description } }}";

        String expected = "{Books={select=[" +
                "{id=3, title=Anna Karenina, description=A complex novel in eight parts, with more than a dozen major characters, it is spread over more than 800 pages (depending on the translation), typically contained in two volumes.}, " +
                "{id=5, title=The Cherry Orchard, description=The play concerns an aristocratic Russian landowner who returns to her family estate (which includes a large and well-known cherry orchard) just before it is auctioned to pay the mortgage.}, " +
                "{id=6, title=The Seagull, description=It dramatises the romantic and artistic conflicts between four characters}, " +
                "{id=7, title=Three Sisters, description=The play is sometimes included on the short list of Chekhov's outstanding plays, along with The Cherry Orchard, The Seagull and Uncle Vanya.[1]}, " +
                "{id=2, title=War and Peace, description=The novel chronicles the history of the French invasion of Russia and the impact of the Napoleonic era on Tsarist society through the stories of five Russian aristocratic families.}]}}";
        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }
    
    @Test
    public void queryForAuthorsWithDefaultOptionalBooks() {
        //given
        String query = "query { "
                + "Authors {" + 
                "    select {" + 
                "      id" + 
                "      name" + 
                "      books {" + 
                "        id" + 
                "        title" + 
                "        genre" + 
                "      }" + 
                "    }" + 
                "  }"+
                "}";

        String expected = "{Authors={select=["
                + "{id=1, name=Leo Tolstoy, books=["
                +   "{id=2, title=War and Peace, genre=NOVEL}, "
                +   "{id=3, title=Anna Karenina, genre=NOVEL}"
                + "]}, "
                + "{id=4, name=Anton Chekhov, books=["
                +   "{id=5, title=The Cherry Orchard, genre=PLAY}, "
                +   "{id=6, title=The Seagull, genre=PLAY}, "
                +   "{id=7, title=Three Sisters, genre=PLAY}"
                + "]}, "
                + "{id=8, name=Igor Dianov, books=[]}"
                + "]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }
    
    @Test
    public void queryForAuthorsWithExlicitOptionalBooksFalse() {
        //given
        String query = "query { "
                + "Authors {" + 
                "    select {" + 
                "      id" + 
                "      name" + 
                "      books(optional: false) {" + 
                "        id" + 
                "        title" + 
                "        genre" + 
                "      }" + 
                "    }" + 
                "  }"+
                "}";

        String expected = "{Authors={select=["
                + "{id=1, name=Leo Tolstoy, books=["
                +   "{id=2, title=War and Peace, genre=NOVEL}, "
                +   "{id=3, title=Anna Karenina, genre=NOVEL}"
                + "]}, "
                + "{id=4, name=Anton Chekhov, books=["
                +   "{id=5, title=The Cherry Orchard, genre=PLAY}, "
                +   "{id=6, title=The Seagull, genre=PLAY}, "
                +   "{id=7, title=Three Sisters, genre=PLAY}"
                + "]}"
                + "]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }
 
    @Test
    public void queryForAuthorsWithExlicitOptionalBooksTrue() {
        //given
        String query = "query { "
                + "Authors {" + 
                "    select {" + 
                "      id" + 
                "      name" + 
                "      books(optional: true) {" + 
                "        id" + 
                "        title" + 
                "        genre" + 
                "      }" + 
                "    }" + 
                "  }"+
                "}";

        String expected = "{Authors={select=["
                + "{id=1, name=Leo Tolstoy, books=["
                +   "{id=2, title=War and Peace, genre=NOVEL}, "
                +   "{id=3, title=Anna Karenina, genre=NOVEL}"
                + "]}, "
                + "{id=4, name=Anton Chekhov, books=["
                +   "{id=5, title=The Cherry Orchard, genre=PLAY}, "
                +   "{id=6, title=The Seagull, genre=PLAY}, "
                +   "{id=7, title=Three Sisters, genre=PLAY}"
                + "]}, "
                + "{id=8, name=Igor Dianov, books=[]}"
                + "]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }    
}