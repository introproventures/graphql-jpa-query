package com.introproventures.graphql.jpa.query.example.controllers;

import com.introproventures.graphql.jpa.query.example.controllers.dto.CreateBookInput;
import com.introproventures.graphql.jpa.query.example.controllers.dto.CreateBookResult;
import com.introproventures.graphql.jpa.query.example.repository.BookRepository;
import com.introproventures.graphql.jpa.query.schema.model.book.Author;
import com.introproventures.graphql.jpa.query.schema.model.book.Book;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Controller
public class MutationController {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private Sinks.Many<CreateBookResult> createBookResultSink;

    @MutationMapping
    String echo(@Argument String name) {
        return name;
    }

    @MutationMapping
    public Mono<CreateBookResult> createBook(@Argument CreateBookInput bookInput) {
        return Mono.just(bookInput)
            .map(this.createBook());
    }

    @MutationMapping
    public Flux<CreateBookResult> createBooks(@Argument List<CreateBookInput> bookInputs) {
        return Flux.fromStream(bookInputs.stream())
            .map(this.createBook());
    }

    Function<CreateBookResult, CreateBookResult> notifyCreateBookResult() {
        return result -> {
            createBookResultSink.emitNext(result, Sinks.EmitFailureHandler.FAIL_FAST);
            return result;
        };
    }

    Function<CreateBookInput, CreateBookResult> createBook() {
        return createBookInputBookFunction()
            .andThen(bookRepository::save)
            .andThen(createBookResultFunction())
            .andThen(notifyCreateBookResult());
    }

    Function<CreateBookInput, Book> createBookInputBookFunction() {
        return bookInput -> {
            Book book = new Book();
            book.setTitle(bookInput.title());
            book.setAuthor(entityManager.getReference(Author.class, bookInput.authorId()));
            return book;
        };
    }

    Function<Book, CreateBookResult> createBookResultFunction() {
        return book -> CreateBookResult
            .builder()
            .id(book.getId())
            .build();
    }

}
