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
package com.introproventures.graphql.jpa.query.boot.test;

import static org.assertj.core.api.Assertions.assertThat;

import com.introproventures.graphql.jpa.query.boot.test.Result.GraphQLError;
import com.introproventures.graphql.jpa.query.web.GraphQLController.GraphQLQueryRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class GraphQLJpaQueryStarterTest {

    private static final String WAR_AND_PEACE = "War and Peace";

    @Autowired
    TestRestTemplate rest;

    @Test
    public void testGraphql() {
        GraphQLQueryRequest query = new GraphQLQueryRequest(
            "{Books(where:{title:{EQ: \"" + WAR_AND_PEACE + "\"}}){ select {title genre}}}"
        );

        ResponseEntity<Result> entity = rest.postForEntity("/graphql", new HttpEntity<>(query), Result.class);
        assertThat(HttpStatus.OK).isEqualTo(entity.getStatusCode());

        Result result = entity.getBody();
        assertThat(result).isNotNull();
        assertThat(result.getErrors()).isNull();
        assertThat("{Books={select=[{title=War and Peace, genre=NOVEL}]}}").isEqualTo(result.getData().toString());
    }

    @Test
    public void testGraphqlArguments() {
        GraphQLQueryRequest query = new GraphQLQueryRequest(
            "query BookQuery($title: String!){Books(where:{title:{EQ: $title}}){select{title genre}}}"
        );

        HashMap<String, Object> variables = new HashMap<>();
        variables.put("title", WAR_AND_PEACE);

        query.setVariables(variables);

        ResponseEntity<Result> entity = rest.postForEntity("/graphql", new HttpEntity<>(query), Result.class);
        assertThat(HttpStatus.OK).isEqualTo(entity.getStatusCode());

        Result result = entity.getBody();
        assertThat(result).isNotNull();
        assertThat(result.getErrors()).isNull();
        assertThat("{Books={select=[{title=War and Peace, genre=NOVEL}]}}").isEqualTo(result.getData().toString());
    }

    @Test
    public void testGraphqlErrorResult() {
        GraphQLQueryRequest query = new GraphQLQueryRequest("{ }");

        ResponseEntity<Result> entity = rest.postForEntity("/graphql", new HttpEntity<>(query), Result.class);
        assertThat(entity.getStatusCode()).as(entity.toString()).isEqualTo(HttpStatus.OK);
        Result result = entity.getBody();
        assertThat(result).isNotNull();
        assertThat(result.getErrors()).isNotNull();
        assertThat(result.getErrors()).extracting(GraphQLError::getMessage).isNotEmpty();
        assertThat(result.getErrors()).extracting(GraphQLError::getExtensions).isNotEmpty();
        assertThat(result.getErrors()).extracting(GraphQLError::getLocations).isNotEmpty();
        assertThat(result.getData()).isNull();
    }
}

class Result {

    Map<String, Object> data;
    List<GraphQLError> errors;
    Map<Object, Object> extensions;

    static class GraphQLError {

        String message;
        List<SourceLocation> locations;
        Map<String, Object> extensions;

        public String getMessage() {
            return message;
        }

        public List<SourceLocation> getLocations() {
            return locations;
        }

        public Map<String, Object> getExtensions() {
            return extensions;
        }
    }

    static class SourceLocation {

        int line;
        int column;
        String sourceName;

        public int getLine() {
            return line;
        }

        public int getColumn() {
            return column;
        }

        public String getSourceName() {
            return sourceName;
        }
    }

    public Map<String, Object> getData() {
        return data;
    }

    public List<GraphQLError> getErrors() {
        return errors;
    }

    public Map<Object, Object> getExtensions() {
        return extensions;
    }
}
