package com.introproventures.graphql.jpa.query.schema.model.book;

import javax.persistence.Embeddable;

import lombok.Data;

@Data
@Embeddable
public class Publisher {
    private String name;

    private String country;
}
