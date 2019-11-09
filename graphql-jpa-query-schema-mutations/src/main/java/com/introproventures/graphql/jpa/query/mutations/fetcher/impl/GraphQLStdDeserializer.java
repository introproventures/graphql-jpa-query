package com.introproventures.graphql.jpa.query.mutations.fetcher.impl;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;


import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.introproventures.graphql.jpa.query.mutations.fetcher.MutationContext;


public class GraphQLStdDeserializer extends BeanDeserializer implements java.io.Serializable {
	private final BeanDeserializer defaultDeserializer;
	private MutationContext mutationContext;

	public GraphQLStdDeserializer(BeanDeserializer defaultDeserializer, MutationContext mutationContext)
	{
	    super(defaultDeserializer);
	    this.defaultDeserializer = defaultDeserializer;
	    this.mutationContext = mutationContext;
	}

	public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		JsonLocation startLocation = p.getCurrentLocation();
        long charOffsetStart = startLocation.getCharOffset();
        
        Object obj = defaultDeserializer.deserialize(p, ctxt);

		JsonLocation endLocation = p.getCurrentLocation();
        long charOffsetEnd = endLocation.getCharOffset();
        String jsonSubString = endLocation.getSourceRef().toString().substring((int)charOffsetStart - 1, (int)charOffsetEnd);

		try {
			Map<String,Object> jsonObj =
					new ObjectMapper().readValue(jsonSubString, HashMap.class);

			mutationContext.addContextFields(obj, jsonObj.keySet().stream().collect(Collectors.toList()));

			return obj;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
