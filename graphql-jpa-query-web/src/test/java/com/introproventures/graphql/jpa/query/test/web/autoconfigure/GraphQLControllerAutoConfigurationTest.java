package com.introproventures.graphql.jpa.query.test.web.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.introproventures.graphql.jpa.query.schema.GraphQLExecutor;
import com.introproventures.graphql.jpa.query.web.GraphQLController;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
public class GraphQLControllerAutoConfigurationTest {
    
    @MockBean
    private GraphQLExecutor graphQLExecutor;
    
    @Autowired
    private GraphQLController graphQLController;
    
    @SpringBootApplication
    static class Application {
        
    }
    
    @Test
    public void contextLoads() {
        assertThat(graphQLController).isNotNull();
    }

}
