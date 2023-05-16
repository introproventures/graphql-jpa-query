package com.introproventures.graphql.jpa.query.converter.model;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
