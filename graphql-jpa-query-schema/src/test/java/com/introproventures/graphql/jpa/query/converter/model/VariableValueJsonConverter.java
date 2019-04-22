package com.introproventures.graphql.jpa.query.converter.model;


import java.io.IOException;

import javax.persistence.AttributeConverter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.QueryException;

public class VariableValueJsonConverter implements AttributeConverter<VariableValue<?>, String> {

    private static ObjectMapper objectMapper = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    public VariableValueJsonConverter() {
    }

    @Override
    public String convertToDatabaseColumn(VariableValue<?> variableValue) {
        try {
            return objectMapper.writeValueAsString(variableValue);
        } catch (JsonProcessingException e) {
            throw new QueryException("Unable to serialize variable.", e);
        }
    }

    @Override
    public VariableValue<?> convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, VariableValue.class);
        } catch (IOException e) {
            throw new QueryException("Unable to deserialize variable.", e);
        }
    }
    
}