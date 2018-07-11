/*
 * Copyright 2017 IntroPro Ventures Inc. and/or its affiliates.
 * Copyright IBM Corporation 2018
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

import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaExecutor;
import com.introproventures.graphql.jpa.query.schema.security.GraphQLJpaSchemaWithExtensionsBuilder;
import com.introproventures.graphql.jpa.query.schema.security.User;
import graphql.ExecutionResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@RunWith(SpringRunner.class)
@SpringBootTest
public class StarwarsQueryAuthTests {

    @SpringBootApplication
    static class Application {
        @Bean
        public GraphQLExecutor graphQLExecutor(final GraphQLSchemaBuilder graphQLSchemaBuilder) {
            return new GraphQLJpaExecutor(graphQLSchemaBuilder.build());
        }

        @Bean
        public GraphQLSchemaBuilder graphQLSchemaBuilder(final EntityManager entityManager) {

            return new GraphQLJpaSchemaWithExtensionsBuilder(entityManager)
                    .name("StarwarsAuth")
                    .description("Starwars JPA test schema with authorizations");
        }
    }


    @Autowired
    private GraphQLExecutor executor;

    @Autowired
    private EntityManager em;

    @Test
    public void contextLoads() {
    }

    @Test
    @Transactional
    public void JPASampleTester() {
        // given:
        Query query = em.createQuery("select h, h.friends from Human h");

        // when:
        List<?> result = query.getResultList();

        // then:
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(13);
    }

    @Test
    public void getHumanForAuthorizedUser() {
        //given:
        String query = "query HumanQuery { Humans { select {name homePlanet gender { description } } } }";

        String expected = "{Humans={select=[" +
                "{name=Luke Skywalker, homePlanet=Tatooine, gender={description=Male}}, " +
                "{name=Darth Vader, homePlanet=Tatooine, gender={description=Male}}, " +
                "{name=Han Solo, homePlanet=null, gender={description=Male}}, " +
                "{name=Leia Organa, homePlanet=Alderaan, gender={description=Female}}, " +
                "{name=Wilhuff Tarkin, homePlanet=null, gender={description=Male}}" +
                "]}}";

        //when:
        User user = new User.Builder().name("Test User").userId("testu").withRole("admin").withRole("user").build();
        ExecutionResult executionResult = executor.execute(query, null, user);
        if (executionResult.getErrors().size() > 0) {
            fail(executionResult.getErrors().get(0).getMessage());
        }

        Object result = executionResult.getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void getHumanForUnauthorizedEntity() {
        //given:
        String query = "query HumanQuery { Humans { select {name homePlanet gender { description } } } }";

        String expected = "Exception while fetching data (/Humans) : AUTH_ERR: You are not authorized to access the requested resource";

        //when:
        User user = new User.Builder().name("Test User").userId("testu").withRole("admin").build();
        String result = executor.execute(query, null, user).getErrors().get(0).getMessage();

        //then:
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void getHumanForUnauthorizedAttribute() {
        //given:
        String query = "query HumanQuery { Humans { select {name homePlanet gender { description } } } }";

        String expected = "Exception while fetching data (/Humans/select[0]/gender) : AUTH_ERR: You are not authorized to access the requested resource";

        //when:
        User user = new User.Builder().name("Test User").userId("testu").withRole("user").build();
        String result = executor.execute(query, null, user).getErrors().get(0).getMessage();

        //then:
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void getHumanForAuthorizedAttributes() {
        //given:
        String query = "query HumanQuery { Humans { select {name homePlanet } } }";

        String expected = "{Humans={select=[" +
                "{name=Luke Skywalker, homePlanet=Tatooine}, " +
                "{name=Darth Vader, homePlanet=Tatooine}, " +
                "{name=Han Solo, homePlanet=null}, " +
                "{name=Leia Organa, homePlanet=Alderaan}, " +
                "{name=Wilhuff Tarkin, homePlanet=null}" +
                "]}}";

        //when:
        User user = new User.Builder().name("Test User").userId("testu").withRole("admin").withRole("user").build();
        ExecutionResult executionResult = executor.execute(query, null, user);
        if (executionResult.getErrors().size() > 0) {
            fail(executionResult.getErrors().get(0).getMessage());
        }

        Object result = executionResult.getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }

}
