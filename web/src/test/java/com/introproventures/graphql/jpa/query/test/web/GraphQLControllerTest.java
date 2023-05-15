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
package com.introproventures.graphql.jpa.query.test.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaExecutor;
import com.introproventures.graphql.jpa.query.web.GraphQLController;
import com.introproventures.graphql.jpa.query.web.GraphQLController.GraphQLQueryRequest;
import graphql.ExecutionResultImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GraphQLController.class)
public class GraphQLControllerTest {

    @Autowired
    private MockMvc mockmvc;

    @MockBean
    private GraphQLJpaExecutor executor;

    @Autowired
    private ObjectMapper mapper;

    @Configuration
    @Import(GraphQLController.class)
    static class Config {
    }
    
    /**
     * Mock executor responses to be non-null in order for Mock MVC 
     * to produce correct output content type in MockHttpResponse
     * 
     */
    @BeforeEach
    public void setUp() {
        when(executor.execute(Mockito.anyString()))
            .thenReturn(new ExecutionResultImpl(new HashMap<>(), new ArrayList<>()));
        
        when(executor.execute(Mockito.anyString(),Mockito.nullable(Map.class)))
            .thenReturn(new ExecutionResultImpl(new HashMap<>(), new ArrayList<>()));
        
        when(executor.execute(Mockito.anyString(),Mockito.nullable(String.class),Mockito.nullable(Map.class)))
    		.thenReturn(new ExecutionResultImpl(new HashMap<>(), new ArrayList<>()));
    }

    private void ok(final GraphQLQueryRequest query) throws Exception, JsonProcessingException {
        ok(mapper.writeValueAsString(query));
    }

    private void ok(final String json) throws Exception, JsonProcessingException {
        perform(json)
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            ;
    }

    private ResultActions perform(final String json) throws Exception {
        return mockmvc.perform(post("/graphql")
           .content(json)
           .contentType(MediaType.APPLICATION_JSON)
           .accept(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testGraphqlGetQueryNoVariables() throws Exception {
        mockmvc.perform(get("/graphql")
               .param("query", "{Tasks(where: {name: {EQ: \"name\"}}){select{id}}}")
               .contentType(GraphQLController.APPLICATION_GRAPHQL_VALUE)               
               .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            ;
        
        verify(executor)
            .execute("{Tasks(where: {name: {EQ: \"name\"}}){select{id}}}", null);
    }
    
    @Test
    public void testGraphqlPostQuery() throws Exception {
        mockmvc.perform(post("/graphql")
               .content("{Tasks(where: {name: {EQ: \"name\"}}){select{id}}}")
               .contentType(GraphQLController.APPLICATION_GRAPHQL_VALUE)               
               .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            ;
        
        verify(executor)
            .execute("{Tasks(where: {name: {EQ: \"name\"}}){select{id}}}", null);
    }
    
    
    @Test
    public void testGraphqlQueryGetWithNullVariables() throws Exception {
        mockmvc.perform(get("/graphql")
                .param("query", "{Tasks(where: {name: {EQ: \"name\"}}){select{id}}}")
                .param("variables", (String) null)
                .contentType(GraphQLController.APPLICATION_GRAPHQL_VALUE)
                .accept(MediaType.APPLICATION_JSON))
             .andExpect(status().isOk())
             .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
             ;
        
        verify(executor)
            .execute("{Tasks(where: {name: {EQ: \"name\"}}){select{id}}}", null);
    }

    @Test
    public void testGraphqlGetQueryWithVariables() throws Exception {
        Map<String, Object> args = new HashMap<>();
        args.put("title", "value");
        String variablesStr = mapper.writeValueAsString(args);
        
        mockmvc.perform(get("/graphql")
                .param("query", "{Tasks(where: {name: {EQ: \"name\"}}){select{id}}}")
                .param("variables", variablesStr)
                .contentType(GraphQLController.APPLICATION_GRAPHQL_VALUE)
                .accept(MediaType.APPLICATION_JSON))
             .andExpect(status().isOk())
             .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
             ;
        
        verify(executor)
            .execute("{Tasks(where: {name: {EQ: \"name\"}}){select{id}}}", args);
    }

    @Test
    public void testGraphqlQueryGetWithEmptyVariables() throws Exception {
        mockmvc.perform(get("/graphql")
                .param("query", "{Tasks(where: {name: {EQ: \"name\"}}){select{id}}}")
                .param("variables", "")
                .contentType(GraphQLController.APPLICATION_GRAPHQL_VALUE))
             .andExpect(status().isOk())
             .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
             ;
        
        verify(executor)
            .execute("{Tasks(where: {name: {EQ: \"name\"}}){select{id}}}", null);
    }
    
    @Test
    public void testGraphqlQueryGetUnsupportedMediaType() throws Exception {
        mockmvc.perform(get("/graphql")
               .param("query", "{Tasks(where: {name: {EQ: \"name\"}}){select{id}}}")
               .contentType(MediaType.TEXT_HTML))
            .andExpect(status().is(415))
            ;
        
        verify(executor, never())
            .execute("{Tasks(where: {name: {EQ: \"name\"}}){select{id}}}", null);
    }

    @Test
    public void testGraphqlQueryPostUnsupportedMediaType() throws Exception {
        mockmvc.perform(post("/graphql")
               .contentType(MediaType.TEXT_HTML))
            .andExpect(status().is(415))
            ;
        
        verify(executor, never())
            .execute("{Tasks(where: {name: {EQ: \"name\"}}){select{id}}}", null);
    }
    
    
    @Test
    public void testGraphqlQuery() throws Exception {
        ok(new GraphQLQueryRequest("{Tasks(where: {name: {EQ: \"name\"}}){select{id}}}"));
        
        verify(executor)
            .execute("{Tasks(where: {name: {EQ: \"name\"}}){select{id}}}", null, null);
    }

    @Test
    public void testGraphqlQueryNull() throws Exception {
        perform(mapper.writeValueAsString(new GraphQLQueryRequest(null)))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void testGraphqlArguments() throws Exception {
        GraphQLQueryRequest query = new GraphQLQueryRequest("query TasksQuery($title: String!){Tasks(where:{name: {EQ: $title}}){select{id name}}}");

        Map<String, Object> variables = new HashMap<>();
        variables.put("title", "value");
        query.setVariables(variables);

        ok(query);
        
        verify(executor)
            .execute(query.getQuery(), null, variables);
    }

    @Test
    public void testGraphqlOperationName() throws Exception {
    	String operationName = "TasksQuery";
        GraphQLQueryRequest query = new GraphQLQueryRequest("query TasksQuery($title: String!){Tasks(where:{name: {EQ: $title}}){select{id name}}}");

        Map<String, Object> variables = new HashMap<>();
        variables.put("title", "value");
        query.setVariables(variables);
        query.setOperationName(operationName);

        ok(query);
        
        verify(executor)
            .execute(query.getQuery(), operationName, variables);
    }    
    // Json directly
    @Test
    public void testGraphqlArgumentsJson() throws Exception {
        String json = "{\"query\": \"{Tasks(where:{name:{EQ: \\\"title\\\"}}){select{ title genre }}\", \"variables\": {\"key\": \"value\"}}";
        
        ok(json);
        
        verify(executor).execute("{Tasks(where:{name:{EQ: \"title\"}}){select{ title genre }}", null, singletonMap("key", "value"));
    }

    @Test
    public void testGraphqlArgumentsEmptyString() throws Exception {
        String json = "{\"query\": \"{Tasks(where:{name:{EQ: \\\"title\\\"}}){select{id name}}\", \"variables\": {}}";
        
        ok(json);
        
        verify(executor).execute("{Tasks(where:{name:{EQ: \"title\"}}){select{id name}}", null, emptyMap());
    }

    @Test
    public void testGraphqlArgumentsNull() throws Exception {
        String json = "{\"query\": \"{Tasks(where:{name:{EQ: \\\"title\\\"}}){select{id name}}\", \"variables\": null}";
        
        ok(json);
        
        verify(executor).execute("{Tasks(where:{name:{EQ: \"title\"}}){select{id name}}", null, null);
    }

    @Test
    public void testGraphqlNoArguments() throws Exception {
        String json = "{\"query\": \"{Tasks(where:{name:{EQ: \\\"title\\\"}}){select{id name}}\"}";
        
        ok(json);
        
        verify(executor).execute("{Tasks(where:{name:{EQ: \"title\"}}){select{id name}}", null, null);
    }
    
    // Form submitted data
    @Test
    public void testGraphqlArgumentsParams() throws Exception {
        String query = "{Tasks(title: \"title\"){title genre}}";
        
        mockmvc.perform(post("/graphql")
               .param("query", query)
               .contentType(MediaType.APPLICATION_FORM_URLENCODED))
               .andExpect(status().isOk());
        
        verify(executor).execute(query, null);
    }

    @Test
    public void testGraphqlArgumentsParamsVariables() throws Exception {
        String query = "query TasksQuery($title: String!){Tasks(name: $title){id name}}";
        Map<String, Object> args = new HashMap<>();
        args.put("title", "value");
        String argsStr = mapper.writeValueAsString(args);
        
        mockmvc.perform(post("/graphql")
            .param("query", query)
            .param("variables", argsStr)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(status().isOk());
        
        verify(executor).execute(query, args);
    }

    @Test
    public void testGraphqlArgumentsParamsVariablesEmpty() throws Exception {
        String query = "{Tasks(name: \"title\"){id name}}";
        
        mockmvc.perform(post("/graphql")
            .param("query", query)
            .param("variables", "")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(status().isOk());

        verify(executor).execute(query, null);
    }
    
}
