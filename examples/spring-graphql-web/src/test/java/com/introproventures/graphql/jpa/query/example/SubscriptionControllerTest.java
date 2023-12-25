package com.introproventures.graphql.jpa.query.example;

import static org.assertj.core.api.Assertions.assertThat;

import com.introproventures.graphql.jpa.query.example.controllers.dto.CreateBookResult;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.graphql.test.tester.WebSocketGraphQlTester;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SubscriptionControllerTest {

    static WebSocketGraphQlTester graphQlTester;

    @BeforeAll
    static void setUp(@LocalServerPort int port) {
        graphQlTester =
            WebSocketGraphQlTester.create(
                URI.create("ws://localhost:" + port + "/graphql/ws"),
                new ReactorNettyWebSocketClient()
            );
    }

    @Test
    void notifyBookCreated() {
        var createBook =
            """
                mutation createBook($authorId: ID!) {
                    createBook(bookInput: {title: "My Book", authorId: $authorId}) {
                        id
                    }
                }
            """;

        var notifyBookCreated =
            """
                subscription {
                  notifyBookCreated {
                    id
                  }
                }
            """;

        Flux<List<CreateBookResult>> result = graphQlTester
            .document(notifyBookCreated)
            .executeSubscription()
            .toFlux()
            .map(spec ->
                spec.path("notifyBookCreated").entity(new ParameterizedTypeReference<List<CreateBookResult>>() {}).get()
            );

        var bookId = graphQlTester
            .document(createBook)
            .variable("authorId", 8)
            .execute()
            .path("createBook.id")
            .entity(Long.class)
            .satisfies(id -> assertThat(id).isNotNull())
            .get();

        StepVerifier
            .create(result)
            .consumeNextWith(c -> assertThat(c).extracting(CreateBookResult::id).containsExactly(bookId))
            .thenCancel()
            .verify();
    }
}
