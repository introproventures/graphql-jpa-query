package com.introproventures.graphql.jpa.query.schema.model.book_superclass;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@MappedSuperclass
@Getter
@Setter
public class BaseAuthor {
    @Id
    Long id;

    @OneToMany(mappedBy="author", fetch= FetchType.LAZY)
    Collection<SuperBook> books;

    @ElementCollection(fetch=FetchType.LAZY)
    @CollectionTable(name = "author_phone_numbers", joinColumns = @JoinColumn(name = "author_id"))
    @Column(name = "phone_number")
    private Set<String> phoneNumbers = new HashSet<>();

    @Enumerated(EnumType.STRING)
    SuperGenre genre;


}
