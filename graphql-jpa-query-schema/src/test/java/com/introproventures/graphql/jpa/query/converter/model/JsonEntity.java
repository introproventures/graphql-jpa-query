package com.introproventures.graphql.jpa.query.converter.model;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Entity(name = "JsonEntity")
@Table(name = "json_entity")
@Data
public class JsonEntity {
 
    @Id
    private int id;
 
    private String firstName;
 
    private String lastName;
 
    @Convert(converter = JsonNodeConverter.class)
    @Column(columnDefinition = "text")
    private JsonNode attributes;
    
}