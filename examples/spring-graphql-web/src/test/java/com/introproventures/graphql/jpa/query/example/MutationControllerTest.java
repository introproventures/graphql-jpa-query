package com.introproventures.graphql.jpa.query.example;

import static org.assertj.core.api.Assertions.assertThat;

import com.introproventures.graphql.jpa.query.example.repository.BookRepository;
import com.introproventures.graphql.jpa.query.schema.model.book.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.HttpGraphQlTester;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureHttpGraphQlTester
public class MutationControllerTest {

    @Autowired
    HttpGraphQlTester graphQlTester;

    @Autowired
    BookRepository bookRepository;

    @Test
    void createBook() {
        var createBook =
            """
                mutation createBook($authorId: ID!) {
                    createBook(bookInput: {title: "My Book", authorId: $authorId}) {
                      id
                    }
                  }
                  
            """;

        var bookId = graphQlTester
            .document(createBook)
            .variable("authorId", 8)
            .execute()
            .path("createBook.id")
            .entity(Long.class)
            .satisfies(id -> assertThat(id).isNotNull())
            .get();

        assertThat(bookRepository.findById(bookId)).get().extracting(Book::getTitle).isEqualTo("My Book");
    }

    @Test
    void createBooks() {
        var createBooks =
            """
                mutation createBooks($authorId: ID!) {
                  createBooks(bookInputs: [
                    {title: "My Book 1", authorId: $authorId}
                    {title: "My Book 2", authorId: $authorId}
                    {title: "My Book 3", authorId: $authorId}
                  ]
                  ) {
                    id
                  }
                }
            """;

        var bookIds = graphQlTester
            .document(createBooks)
            .variable("authorId", 8)
            .execute()
            .path("createBooks[*].id")
            .entityList(Long.class)
            .satisfies(id -> assertThat(id).isNotNull())
            .get();

        assertThat(bookRepository.findAllById(bookIds))
            .extracting(Book::getTitle)
            .containsExactly("My Book 1", "My Book 2", "My Book 3");
    }
}
