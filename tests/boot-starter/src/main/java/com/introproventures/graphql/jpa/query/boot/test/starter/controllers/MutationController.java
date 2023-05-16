package com.introproventures.graphql.jpa.query.boot.test.starter.controllers;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
public class MutationController {

    @MutationMapping
    Mono<String> reverse(@Argument String string) {
        return Mono.just(new StringBuilder(string)).map(StringBuilder::reverse).map(StringBuilder::toString);
    }
}
