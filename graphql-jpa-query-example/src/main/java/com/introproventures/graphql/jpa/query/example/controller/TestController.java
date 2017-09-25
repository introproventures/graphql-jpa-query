package com.introproventures.graphql.jpa.query.example.controller;


import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
public class TestController {

    @RequestMapping("/")
    public String index() {
        return "Greetings from grafql!!!";
    }

}