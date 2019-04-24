package com.introproventures.graphql.jpa.query.schema.model.book_superclass;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@MappedSuperclass
@Getter
@Setter
public class BaseBook {
    @Id
    Long id;

    @ManyToOne(fetch= FetchType.LAZY)
    SuperAuthor author;

    @Enumerated(EnumType.STRING)
    SuperGenre genre;

    Date publicationDate;
}
