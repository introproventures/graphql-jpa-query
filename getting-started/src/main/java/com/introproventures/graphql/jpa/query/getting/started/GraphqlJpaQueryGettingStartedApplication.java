package com.introproventures.graphql.jpa.query.getting.started;

import com.introproventures.graphql.jpa.query.autoconfigure.EnableGraphQLJpaQuerySchema;
import com.introproventures.graphql.jpa.query.schema.model.book.Book;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableGraphQLJpaQuerySchema(Book.class)
public class GraphqlJpaQueryGettingStartedApplication {

    public static void main(String[] args) {
        SpringApplication.run(GraphqlJpaQueryGettingStartedApplication.class, args);
    }
}
