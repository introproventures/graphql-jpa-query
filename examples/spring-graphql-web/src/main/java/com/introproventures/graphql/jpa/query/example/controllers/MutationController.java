package com.introproventures.graphql.jpa.query.example.controllers;

import com.introproventures.graphql.jpa.query.example.controllers.dto.CreateBookInput;
import com.introproventures.graphql.jpa.query.example.controllers.dto.CreateBookResult;
import com.introproventures.graphql.jpa.query.example.repository.BookRepository;
import com.introproventures.graphql.jpa.query.schema.model.book.Author;
import com.introproventures.graphql.jpa.query.schema.model.book.Book;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.function.Function;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Controller
public class MutationController {

    private final EntityManager entityManager;

    private final BookRepository bookRepository;

    private final Sinks.Many<CreateBookResult> createBookResultSink;

    public MutationController(
        EntityManager entityManager,
        BookRepository bookRepository,
        Sinks.Many<CreateBookResult> createBookResultSink
    ) {
        this.entityManager = entityManager;
        this.bookRepository = bookRepository;
        this.createBookResultSink = createBookResultSink;
    }

    @MutationMapping
    public Mono<CreateBookResult> createBook(@Argument CreateBookInput bookInput) {
        return Mono.just(bookInput).map(this::doCreateBook);
    }

    @MutationMapping
    public Flux<CreateBookResult> createBooks(@Argument List<CreateBookInput> bookInputs) {
        return Flux.fromStream(bookInputs.stream()).map(this::doCreateBook);
    }

    CreateBookResult doCreateBook(CreateBookInput input) {
        return createBookEntity()
            .andThen(bookRepository::save)
            .andThen(createBookResult())
            .andThen(notifyCreateBookResult())
            .apply(input);
    }

    Function<CreateBookInput, Book> createBookEntity() {
        return bookInput -> {
            Book book = new Book();
            book.setTitle(bookInput.title());
            book.setAuthor(entityManager.getReference(Author.class, bookInput.authorId()));
            return book;
        };
    }

    Function<CreateBookResult, CreateBookResult> notifyCreateBookResult() {
        return result -> {
            createBookResultSink.emitNext(result, Sinks.EmitFailureHandler.FAIL_FAST);
            return result;
        };
    }

    Function<Book, CreateBookResult> createBookResult() {
        return book -> CreateBookResult.builder().id(book.getId()).build();
    }
}
