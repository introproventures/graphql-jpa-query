package com.introproventures.graphql.jpa.query.boot.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

import com.introproventures.graphql.jpa.query.schema.GraphQLExecutor;
import com.introproventures.graphql.jpa.query.schema.GraphQLSchemaBuilder;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaExecutor;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;
import com.introproventures.graphql.jpa.query.web.model.Author;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class GraphQLJpaQueryAutoConfigurationTest {

    @SpringBootApplication
    @EntityScan(basePackageClasses=Author.class)
    static class Application {
    }
    
    @Autowired
    GraphQLJpaQueryProperties  graphQLJpaQueryProperties;

    @Autowired
    GraphQLExecutor graphQLExecutor;

    @Autowired
    GraphQLSchemaBuilder graphQLSchemaBuilder;
    
    @Test
    public void contextIsAutoConfigured() {
        assertThat(graphQLExecutor).isInstanceOf(GraphQLJpaExecutor.class);
        assertThat(graphQLSchemaBuilder).isInstanceOf(GraphQLJpaSchemaBuilder.class);
        
        assertThat(graphQLJpaQueryProperties.getName()).isEqualTo("GraphQLBooks");
        assertThat(graphQLJpaQueryProperties.getDescription()).isEqualTo("GraphQL Books Schema Description");
        assertThat(graphQLJpaQueryProperties.getPath()).isEqualTo("/graphql");
        assertThat(graphQLJpaQueryProperties.isEnabled()).isEqualTo(true);
        
    }
}