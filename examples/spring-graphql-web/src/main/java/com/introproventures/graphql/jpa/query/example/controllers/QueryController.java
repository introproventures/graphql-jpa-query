package com.introproventures.graphql.jpa.query.example.controllers;

import java.util.Date;
import java.util.Random;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class QueryController {

    @QueryMapping
    String hello(@Argument String name) {
        return "Greetings, " + name + "!";
    }

    @QueryMapping
    Long random() {
        return new Random().nextLong();
    }

    @QueryMapping
    Date now() {
        return new Date();
    }
}
