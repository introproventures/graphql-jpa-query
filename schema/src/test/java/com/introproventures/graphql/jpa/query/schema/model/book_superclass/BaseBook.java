package com.introproventures.graphql.jpa.query.schema.model.book_superclass;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public class BaseBook {

    @Id
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    SuperAuthor author;

    @Enumerated(EnumType.STRING)
    SuperGenre genre;

    Date publicationDate;

    String[] tags;
}
