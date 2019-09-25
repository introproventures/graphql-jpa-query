package com.introproventures.graphql.jpa.query.embeddedid.model;


import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

import lombok.Data;

@Data
@Entity
public class Book {

    @EmbeddedId
    private BookId bookId;

    private String description;
}