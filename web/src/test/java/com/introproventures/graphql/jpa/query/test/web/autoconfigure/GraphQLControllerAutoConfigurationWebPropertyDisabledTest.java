package com.introproventures.graphql.jpa.query.test.web.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import com.introproventures.graphql.jpa.query.schema.GraphQLExecutor;
import com.introproventures.graphql.jpa.query.web.GraphQLController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = { "spring.graphql.jpa.query.enabled=true", "spring.graphql.jpa.query.web.enabled=false" }
)
public class GraphQLControllerAutoConfigurationWebPropertyDisabledTest {

    @MockBean
    private GraphQLExecutor graphQLExecutor;

    @Autowired(required = false)
    private GraphQLController graphQLController;

    @SpringBootApplication
    static class Application {}

    @Test
    public void contextLoads() {
        assertThat(graphQLController).isNull();
    }
}
