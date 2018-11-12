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

import java.util.HashMap;

import javax.persistence.EntityManager;

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

import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaExecutor;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;

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
    public void queryForEntityWithEmeddableType() {
        //given
        String query = "{ Boat(id: \"1\") { id engine { identification } } }";
        
        String expected = "{Boat={id=1, engine={identification=12345}}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryForEntityWithEmeddableTypeAndWhere() {
        //given
        String query = "{ Boats { select { id engine(where: { identification: { EQ: \"12345\"}}) { identification } } } }";

        String expected = "{Boats={select=[{id=1, engine={identification=12345}}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }
    
    

}