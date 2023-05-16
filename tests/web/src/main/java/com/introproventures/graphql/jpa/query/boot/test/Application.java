package com.introproventures.graphql.jpa.query.boot.test;

import com.introproventures.graphql.jpa.query.autoconfigure.EnableGraphQLJpaQuerySchema;
import com.introproventures.graphql.jpa.query.schema.model.book.Author;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableGraphQLJpaQuerySchema(basePackageClasses = Author.class)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
