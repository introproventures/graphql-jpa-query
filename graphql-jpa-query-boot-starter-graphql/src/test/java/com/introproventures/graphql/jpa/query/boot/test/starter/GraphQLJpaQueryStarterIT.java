/*
 * Copyright 2017 IntroPro Ventures, Inc. and/or its affiliates.
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
package com.introproventures.graphql.jpa.query.boot.test.starter;

import com.introproventures.graphql.jpa.query.autoconfigure.EnableGraphQLJpaQuerySchema;
import com.introproventures.graphql.jpa.query.boot.test.starter.model.Book;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.graphql.ExecutionGraphQlService;
import org.springframework.graphql.ResponseError;
import org.springframework.graphql.test.tester.ExecutionGraphQlServiceTester;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
public class GraphQLJpaQueryStarterIT {
	private static final String	WAR_AND_PEACE	= "War and Peace";

    @SpringBootApplication
    @EnableGraphQLJpaQuerySchema(basePackageClasses = Book.class)
    static class Application {

        @Bean
        ExecutionGraphQlServiceTester graphQlTester(ExecutionGraphQlService graphQlService) {
            return ExecutionGraphQlServiceTester.create(graphQlService);
        }
    }

    @Autowired
    private ExecutionGraphQlServiceTester graphQlTester;

	@Test
	public void testGraphqlBooksQuery() {
        graphQlTester.document("{Books(where:{title:{EQ: \"" + WAR_AND_PEACE + "\"}}){ select {title genre}}}")
                     .execute()
                     .errors().verify()
                     .path("Books.select").entityList(Book.class).hasSize(1);
	}

	@Test
	public void testGraphqlBookQueryArguments() {
        graphQlTester.document("query BookQuery($title: String!){Books(where:{title:{EQ: $title}}){select{title genre}}}")
                     .variable("title", WAR_AND_PEACE)
                     .execute()
                     .errors().verify()
                     .path("Books.select").entityList(Map.class).hasSize(1)
                                          .satisfies(result -> assertThat(result).extracting("title", "genre")
                                                                                 .contains(tuple("War and Peace", "NOVEL")));
	}

    @Test
    public void testGraphqlQueryControllerLongCount() {
        graphQlTester.document("{ count(string: \"Hello world!\") }")
                     .execute()
                     .errors().verify()
                     .path("count").entity(Long.class).isEqualTo(12L);

    }

    @Test
    public void testGraphqlQueryControllerToday() {
        graphQlTester.document("{ today }")
                     .execute()
                     .errors().verify()
                     .path("today").entity(Date.class).satisfies(date -> assertThat(date).isNotNull());
    }

    @Test
    public void testGraphqlMutationController() {
        graphQlTester.document("mutation { reverse(string: \"Hello world!\") }")
                     .execute()
                     .errors().verify()
                     .path("reverse").entity(String.class).isEqualTo("!dlrow olleH");
    }

    @Test
    public void testGraphqlErrorResult() {
        graphQlTester.document("{ }")
                     .execute()
                     .errors().satisfy(result -> {
                         assertThat(result).extracting(ResponseError::getMessage)
                                                       .isNotEmpty();
                         assertThat(result).extracting(ResponseError::getExtensions)
                                                       .isNotEmpty();
                         assertThat(result).extracting(ResponseError::getLocations)
                                                       .isNotEmpty();
                     });

    }
}

