package com.introproventures.graphql.jpa.query.embeddedid.model;


import java.io.Serializable;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class BookId implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String title;
    private String language;
}
