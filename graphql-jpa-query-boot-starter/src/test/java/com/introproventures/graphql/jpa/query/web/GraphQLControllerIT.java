package com.introproventures.graphql.jpa.query.web;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.introproventures.graphql.jpa.query.web.GraphQLController.GraphQLQueryRequest;

import graphql.ExecutionResult;
import graphql.GraphQLError;
import lombok.Value;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class GraphQLControllerIT {
	private static final String	WAR_AND_PEACE	= "War and Peace";

    @SpringBootApplication
    static class Application {
    }
   	
	@Autowired
	TestRestTemplate			rest;

	@Test
	public void testGraphql() {
		GraphQLQueryRequest query = new GraphQLQueryRequest("{Books(where:{title:{EQ: \"" + WAR_AND_PEACE + "\"}}){ select {title genre}}}");

		ResponseEntity<Result> entity = rest.postForEntity("/graphql", new HttpEntity<>(query), Result.class);
		Assert.assertEquals(entity.toString(), HttpStatus.OK, entity.getStatusCode());

		Result result = entity.getBody();
		Assert.assertNotNull(result);
		Assert.assertTrue(result.getErrors().toString(), result.getErrors().isEmpty());
		Assert.assertEquals("{Books={select=[{title=War and Peace, genre=NOVEL}]}}", result.getData().toString());
	}

	@Test
	public void testGraphqlArguments() throws JsonParseException, JsonMappingException, IOException {
		GraphQLQueryRequest query = new GraphQLQueryRequest("query BookQuery($title: String!){Books(where:{title:{EQ: $title}}){select{title genre}}}");
		
		String variables = "{\"title\":\"" + WAR_AND_PEACE + "\"}";
		query.setVariables(variables);

		ResponseEntity<Result> entity = rest.postForEntity("/graphql", new HttpEntity<>(query), Result.class);
		Assert.assertEquals(entity.toString(), HttpStatus.OK, entity.getStatusCode());

		Result result = entity.getBody();
		Assert.assertNotNull(result);
		Assert.assertTrue(result.getErrors().toString(), result.getErrors().isEmpty());
		Assert.assertEquals("{Books={select=[{title=War and Peace, genre=NOVEL}]}}", result.getData().toString());
	}
}

@Value
class Result implements ExecutionResult {
	Map<String, Object>   data;
	List<GraphQLError>				   errors;
	Map<Object, Object>                extensions;	
}
