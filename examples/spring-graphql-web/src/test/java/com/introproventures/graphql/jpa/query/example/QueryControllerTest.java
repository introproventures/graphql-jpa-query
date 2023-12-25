package com.introproventures.graphql.jpa.query.example;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.graphql.test.tester.HttpGraphQlTester;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureHttpGraphQlTester
public class QueryControllerTest {

    @Autowired
    HttpGraphQlTester graphQlTester;

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
            query findAllBooks($authorNames: [String]!){
              findAllBooks(where: {author: {name: {IN: $authorNames}}}) {
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
            .variable("authorNames", Arrays.asList("Leo Tolstoy", "Anton Chekhov"))
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
