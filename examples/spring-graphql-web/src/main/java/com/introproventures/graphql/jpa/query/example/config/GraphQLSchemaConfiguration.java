package com.introproventures.graphql.jpa.query.example.config;

import com.introproventures.graphql.jpa.query.autoconfigure.EnableGraphQLJpaQuerySchema;
import com.introproventures.graphql.jpa.query.autoconfigure.GraphQLJPASchemaBuilderCustomizer;
import com.introproventures.graphql.jpa.query.example.controllers.dto.CreateBookResult;
import com.introproventures.graphql.jpa.query.schema.model.book.Book;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Sinks;

@Configuration
@EnableGraphQLJpaQuerySchema(basePackageClasses = Book.class)
public class GraphQLSchemaConfiguration {

    @Bean
    GraphQLJPASchemaBuilderCustomizer graphQLJPASchemaBuilderCustomizer() {
        return builder ->
            builder
                .graphQLIDType(true)
                .queryByIdFieldNameCustomizer("find%sById"::formatted)
                .queryAllFieldNameCustomizer("findAll%s"::formatted);
    }

    @Bean
    Sinks.Many<CreateBookResult> createBookResultSink() {
        return Sinks.many().replay().latest();
    }
}
