package com.introproventures.graphql.jpa.query.embeddedid.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.Data;

@Data
@Entity
public class Book {

    @EmbeddedId
    private BookId bookId;

    private String description;
}
