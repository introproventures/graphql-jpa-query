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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
public class GraphQLEnumVariableBindingsTests extends AbstractSpringBootTestSupport {

    @SpringBootConfiguration
    @EnableAutoConfiguration
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
    public void queryEnumSimpleVariableBinding() {
        //given
        String query = "query($genre: Genre) {" + 
                "    Books(where: {" + 
                "    genre: {EQ: $genre}" + 
                "  }) {" + 
                "    select {" + 
                "      id" + 
                "      title" + 
                "      genre" + 
                "    }" + 
                "  }" + 
                "}";
        
        Map<String, Object> variables = new HashMap<String, Object>() {{
            put("genre", "NOVEL");
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
    public void queryEnumSimpleArrayVariableBinding() {
        //given
        String query = "query($genre: Genre) {" + 
                "    Books(where: {" + 
                "    genre: {IN: [$genre]}" + 
                "  }) {" + 
                "    select {" + 
                "      id" + 
                "      title" + 
                "      genre" + 
                "    }" + 
                "  }" + 
                "}";
        
        Map<String, Object> variables = new HashMap<String, Object>() {{
            put("genre", "NOVEL");
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
    public void queryEnumArrayVariableBindingInNestedRelation() {
        //given
        String query = "query($genres: [Genre!]) {" + 
                "    Authors(where: {" + 
                "    books: {" + 
                "        genre: {IN: $genres}" + 
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
        
        Map<String, Object> variables = new HashMap<String, Object>() {{
            put("genres", Arrays.asList("NOVEL"));
        }};


        String expected = "{Authors={select=[{"
                + "id=1, name=Leo Tolstoy, books=["
                +   "{id=2, title=War and Peace, genre=NOVEL}, "
                +   "{id=3, title=Anna Karenina, genre=NOVEL}"
                + "]}"
                + "]}}";

        //when
        Object result = executor.execute(query, variables).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }        
    
    @Test
    public void queryEnumArrayVariableBindingInEmbeddedRelation() {
        //given
        String query = "query($genres: [Genre!]) {" + 
                "    Authors {" + 
                "    select {" + 
                "      id" + 
                "      name" + 
                "      books(where:  {" + 
                "        genre: {IN: $genres}" + 
                "    }) {" + 
                "        id" + 
                "        title" + 
                "        genre" + 
                "      }" + 
                "    }" + 
                "  }  " + 
                "}";
        
        Map<String, Object> variables = new HashMap<String, Object>() {{
            put("genres", Arrays.asList("NOVEL"));
        }};


        String expected = "{Authors={select=["
                +   "{id=1, name=Leo Tolstoy, books=["
                +       "{id=2, title=War and Peace, genre=NOVEL}, "
                +       "{id=3, title=Anna Karenina, genre=NOVEL}"
                +   "]}, "
                +   "{id=4, name=Anton Chekhov, books=[]}, "
                +   "{id=8, name=Igor Dianov, books=[]}"
                + "]}}";
        
        //when
        Object result = executor.execute(query, variables).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }
}
