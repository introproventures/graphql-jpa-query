package com.introproventures.graphql.jpa.query.converter.model;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonNodeConverter implements AttributeConverter<JsonNode, String> {
    private final static Logger logger = LoggerFactory.getLogger(JsonNodeConverter.class);
    
    private static final ObjectMapper objectMapper = new ObjectMapper()
                                                            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    @Override
    public String convertToDatabaseColumn(JsonNode jsonMap) {
 
        String jsonString = null;
        try {
            jsonString = objectMapper.writeValueAsString(jsonMap);
        } catch (final JsonProcessingException e) {
            logger.error("JSON writing error", e);
        }
 
        return jsonString;
    }
 
    @Override
    public JsonNode convertToEntityAttribute(String jsonString) {
 
        JsonNode jsonMap = null;
        try {
            jsonMap = objectMapper.readValue(jsonString, JsonNode.class);
            
        } catch (final IOException e) {
            logger.error("JSON reading error", e);
        }
 
        return jsonMap;
    }
 
}
