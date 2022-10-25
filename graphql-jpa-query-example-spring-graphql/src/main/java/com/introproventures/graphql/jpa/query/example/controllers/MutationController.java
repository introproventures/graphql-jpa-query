package com.introproventures.graphql.jpa.query.example.controllers;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Controller
public class MutationController {

    @MutationMapping
    String echo(@Argument String name) {
        return name;
    }
}
