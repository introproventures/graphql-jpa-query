package com.introproventures.graphql.jpa.query.example.repository;

import com.introproventures.graphql.jpa.query.schema.model.book.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {}
