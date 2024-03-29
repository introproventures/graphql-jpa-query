package com.introproventures.graphql.jpa.query.boot.test.starter.controllers;

import java.util.Date;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
public class QueryController {

    @QueryMapping
    Mono<Long> count(@Argument String string) {
        return Mono.just(string.length()).map(Long::valueOf);
    }

    @QueryMapping
    Mono<Date> today() {
        return Mono.just(new Date());
    }
}
