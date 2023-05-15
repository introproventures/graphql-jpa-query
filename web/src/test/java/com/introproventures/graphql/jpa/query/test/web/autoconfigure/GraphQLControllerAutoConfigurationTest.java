package com.introproventures.graphql.jpa.query.test.web.autoconfigure;

import com.introproventures.graphql.jpa.query.schema.GraphQLExecutor;
import com.introproventures.graphql.jpa.query.web.GraphQLController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Bean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT, properties = "debug=true")
public class GraphQLControllerAutoConfigurationTest {

    @Autowired
    private GraphQLController graphQLController;
    
    @SpringBootApplication
    static class Application {
        @Bean
        GraphQLExecutor graphQLExecutor() {
            return mock(GraphQLExecutor.class);
        }
    }
    
    @Test
    public void contextLoads() {
        assertThat(graphQLController).isNotNull();
    }

}
