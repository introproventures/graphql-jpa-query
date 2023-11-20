package com.introproventures.graphql.jpa.query.example;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

@Disabled
@Testcontainers
@SpringBootTest
@Import(TestApplication.class)
class ApplicationTest {

    @Test
    void contextLoads() {}
}
