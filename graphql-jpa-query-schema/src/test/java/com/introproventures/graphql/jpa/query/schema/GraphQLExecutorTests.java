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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaExecutor;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.NONE)
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
        //String query = "{ Author(id: 1) { id name } }";
        
        String expected = "{Author={id=1, name=Leo Tolstoy, phoneNumbers=[1-123-1234, 1-123-5678]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }
    

}