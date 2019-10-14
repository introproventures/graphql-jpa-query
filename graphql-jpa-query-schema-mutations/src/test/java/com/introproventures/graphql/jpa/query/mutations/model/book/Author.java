package com.introproventures.graphql.jpa.query.mutations.model.book;

import com.introproventures.graphql.jpa.query.mutations.annotation.GraphQLWriteEntityForRole;
import com.introproventures.graphql.jpa.query.mutations.annotation.GraphQLWriteType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.*;

@Entity
@ToString
@EqualsAndHashCode(exclude={"books","phoneNumbers"}) // Fixes NPE in Hibernate when initializing loaded collections #1
@GraphQLWriteEntityForRole(value = {"user"}, operations = {GraphQLWriteType.ALL})
public class Author {
	@Id
	Long id;

	String name;

	@OneToMany(mappedBy="author", fetch=FetchType.LAZY, /*cascade = CascadeType.ALL,*/ orphanRemoval=true)
	List<Book> books = new ArrayList<>();
	
	@ElementCollection(fetch=FetchType.LAZY) 
	@CollectionTable(name = "author_phone_numbers", joinColumns = @JoinColumn(name = "author_id")) 
	@Column(name = "phone_number")
	private Set<String> phoneNumbers = new HashSet<>();	
	
	@Enumerated(EnumType.STRING)
    Genre genre;

	@OneToOne(mappedBy = "author" /*, cascade = CascadeType.ALL*/,	fetch = FetchType.LAZY, optional = true)
	private Alias alias;

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

	public Set<String> getPhoneNumbers() {
		return phoneNumbers;
	}

	public void setPhoneNumbers(Set<String> phoneNumbers) {
		this.phoneNumbers = phoneNumbers;
	}

	public Genre getGenre() {
		return genre;
	}

	public void setGenre(Genre genre) {
		this.genre = genre;
	}

	public Alias getAlias() {
		return alias;
	}

	public void setAlias(Alias alias) {
		this.alias = alias;
	}
}
