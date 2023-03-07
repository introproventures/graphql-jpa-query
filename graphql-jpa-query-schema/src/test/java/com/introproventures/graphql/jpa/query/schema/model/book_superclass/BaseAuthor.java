package com.introproventures.graphql.jpa.query.schema.model.book_superclass;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

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
