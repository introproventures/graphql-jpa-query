package com.introproventures.graphql.jpa.query.web;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.introproventures.graphql.jpa.query.schema.GraphQLExecutor;
import com.introproventures.graphql.jpa.query.web.GraphQLController.GraphQLQueryRequest;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = GraphQLController.class)
public class GraphQLControllerTest {
    
    
    @Configuration
    @Import(GraphQLController.class)
    static class Config { }
    
	@Autowired
	MockMvc			mockmvc;
	
	@MockBean
	GraphQLExecutor	executor;
	
	@Autowired
	ObjectMapper	mapper;

	private void ok(final GraphQLQueryRequest query) throws Exception, JsonProcessingException {
		perform(mapper.writeValueAsString(query)).andExpect(status().isOk());
	}

	private void ok(final String json) throws Exception, JsonProcessingException {
		perform(json).andExpect(status().isOk());
	}

	private ResultActions perform(final String json) throws Exception {
		return mockmvc.perform(post("/graphql").content(json).contentType(MediaType.APPLICATION_JSON));
	}

	// Serialize a Query object
	@Test
	public void testGraphqlQuery() throws Exception {
		System.out.println("testGraphqlQuery");
		ok(new GraphQLQueryRequest("{Books(where:{title:{EQ:\"title\"}}){select {title genre}}}"));
		verify(executor).execute("{Books(where:{title:{EQ:\"title\"}}){select {title genre}}}", null);
	}

	@Test
	public void testGraphqlQueryNull() throws Exception {
		perform(mapper.writeValueAsString(new GraphQLQueryRequest(null))).andExpect(status().isBadRequest());
	}

	@SuppressWarnings( "serial" )
    @Test
	public void testGraphqlArguments() throws Exception {
		System.out.println("testGraphqlArguments");
		GraphQLQueryRequest query = new GraphQLQueryRequest("query BookQuery($title: String!){Books(title: $title){title genre}}");
		query.setVariables("{\"title\":\"value\"}");
		
		ok(query);
		verify(executor).execute(
		    query.getQuery(), 
		    new HashMap<String, Object>() {{
		        put("title", "value"); 
		    }}
		 );
	}

	// Json directly
	@Test
	public void testGraphqlArgumentsJson() throws Exception {
		String json = "{\"query\": \"{Books(title: \\\"title\\\"){title genre}\", \"arguments\": {\"title\": \"title\"}}";
		ok(json);
		verify(executor).execute("{Books(title: \"title\"){title genre}", null);
	}

	@Test
	public void testGraphqlArgumentsEmptyString() throws Exception {
		String json = "{\"query\": \"{Books(title: \\\"title\\\"){title genre}\", \"arguments\": \"\"}";
		ok(json);
		verify(executor).execute("{Books(title: \"title\"){title genre}", null);
	}

	@Test
	public void testGraphqlArgumentsNull() throws Exception {
		String json = "{\"query\": \"{Books(title: \\\"title\\\"){title genre}\", \"arguments\": null}";
		ok(json);
		verify(executor).execute("{Books(title: \"title\"){title genre}", null);
	}

	// Form submitted data
	@Test
	public void testGraphqlArgumentsParams() throws Exception {
		String query = "{Books(title: \"title\"){title genre}}";
		mockmvc.perform(post("/graphql").param("query", query).contentType(MediaType.APPLICATION_FORM_URLENCODED)).andExpect(status().isOk());
		verify(executor).execute(query, null);
	}

	@Test
	public void testGraphqlArgumentsParamsVariables() throws Exception {
		String query = "query BookQuery($title: String!){Books(title: $title){title genre}}";
		Map<String, Object> args = new HashMap<>();
		args.put("title", "value");
		String argsStr = mapper.writeValueAsString(args);
		mockmvc.perform(post("/graphql").param("query", query).param("variables", argsStr).contentType(MediaType.APPLICATION_FORM_URLENCODED)).andExpect(status().isOk());
		verify(executor).execute(query, args);
	}

	@Test
	public void testGraphqlArgumentsParamsVariablesEmpty() throws Exception {
		String query = "{Books(title: \"title\"){title genre}}";
		mockmvc.perform(post("/graphql").param("query", query).param("variables", "").contentType(MediaType.APPLICATION_FORM_URLENCODED)).andExpect(status().isOk());
		verify(executor).execute(query, null);
	}
}
