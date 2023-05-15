package com.introproventures.graphql.jpa.query.example.controllers;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

@Controller
public class SubscriptionController {

    @SubscriptionMapping
    Flux<String> greetings(@Argument String name) {
      return Flux.fromStream(Stream.generate(() -> "Hello, " + name + "@ " + Instant.now()))
                 .delayElements(Duration.ofSeconds(1))
                 .take(10);
    }
}
