package com.introproventures.graphql.jpa.query.example;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.graphql.test.tester.HttpGraphQlTester;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureHttpGraphQlTester
public class ApplicationTest {

    @Autowired
    HttpGraphQlTester graphQlTester;

    @Test
    void contextLoads() {
        // success
    }

    @Test
    void findBookById() {
        var findBookById =
            """
            query findBookById($id: ID!) {
              findBookById(id: $id) {
                id
                title
                author {
                  name
                }
              }
            }
            """;

        graphQlTester
            .document(findBookById)
            .variable("id", 2)
            .execute()
            .path("findBookById.title")
            .entity(String.class)
            .satisfies(title -> assertThat(title).isEqualTo("War and Peace"));
    }

    @Test
    void findAllBooks() {
        var allBooks =
            """
            query {
              findAllBooks {
                select {
                  id
                  title
                  author {
                    id
                    name
                  }
                }
              }
            }
            """;

        graphQlTester
            .document(allBooks)
            .execute()
            .path("findAllBooks.select[*].title")
            .entityList(String.class)
            .satisfies(titles ->
                assertThat(titles)
                    .containsExactly(
                        "War and Peace",
                        "Anna Karenina",
                        "The Cherry Orchard",
                        "The Seagull",
                        "Three Sisters"
                    )
            );
    }
}
