package com.introproventures.graphql.jpa.query.example.controllers;

import com.introproventures.graphql.jpa.query.example.controllers.dto.CreateBookResult;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Controller
public class SubscriptionController {

    @Autowired
    private Sinks.Many<CreateBookResult> createBookResultSink;

    @SubscriptionMapping
    Flux<String> greetings(@Argument String name) {
        return Flux
            .fromStream(Stream.generate(() -> "Hello, " + name + "@ " + Instant.now()))
            .delayElements(Duration.ofSeconds(1))
            .take(10);
    }

    @SubscriptionMapping
    Publisher<List<CreateBookResult>> notifyBookCreated() {
        return createBookResultSink.asFlux().buffer(Duration.ofSeconds(1));
    }


}
