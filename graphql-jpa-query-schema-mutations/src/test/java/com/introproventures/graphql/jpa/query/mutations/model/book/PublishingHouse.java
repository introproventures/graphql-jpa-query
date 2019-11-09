package com.introproventures.graphql.jpa.query.mutations.model.book;

import com.introproventures.graphql.jpa.query.mutations.annotation.GraphQLWriteEntityForRole;
import com.introproventures.graphql.jpa.query.mutations.annotation.GraphQLWriteType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
/*
@Getter
@Setter
*/
@ToString
@EqualsAndHashCode(exclude={"books"}) // Fixes NPE in Hibernate when initializing loaded collections #1
@GraphQLWriteEntityForRole(value = {"user"}, operations = {GraphQLWriteType.ALL})
public class    PublishingHouse {
    @Id
    Long id;

    @Column(name = "name", nullable = true, length = 256)
    String name;

    @ManyToMany(fetch = FetchType.LAZY /*, cascade = CascadeType.ALL*/)
    @JoinTable(name = "publish_book",
            joinColumns = @JoinColumn(name = "ph_id"),
            inverseJoinColumns = @JoinColumn(name = "b_id")
    )
    private List<Book> books = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Book> getBooks() {
        return books;
    }

    public void setBooks(List<Book> books) {
        this.books.clear();
        this.books.addAll(books);
    }
}
