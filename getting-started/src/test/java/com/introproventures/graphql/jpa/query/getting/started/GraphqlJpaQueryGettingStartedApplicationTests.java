package com.introproventures.graphql.jpa.query.getting.started;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.GraphQlTester;

@SpringBootTest
@AutoConfigureGraphQlTester
class GraphqlJpaQueryGettingStartedApplicationTests {

    @Autowired
    private GraphQlTester graphQlTester;

    @Test
    void contextLoads() {}
}
