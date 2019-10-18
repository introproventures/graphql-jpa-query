package com.introproventures.graphql.jpa.query.converter;

import static org.assertj.core.api.Assertions.assertThat;

import javax.persistence.EntityManager;

import com.introproventures.graphql.jpa.query.schema.GraphQLExecutor;
import com.introproventures.graphql.jpa.query.schema.GraphQLSchemaBuilder;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaExecutor;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.NONE,
    properties = "spring.datasource.data=GraphQLJpaConverterTests.sql")
@TestPropertySource({"classpath:hibernate.properties"})
public class BaseConverterTest {

    @Autowired
    GraphQLExecutor executor;

    @Autowired
    EntityManager entityManager;

    @Test
    public void contextLoads() {
        assertThat(executor).isNotNull();
        assertThat(entityManager).isNotNull();
    }

    @SpringBootApplication
    static class Application {
        @Bean
        public GraphQLExecutor graphQLExecutor(final GraphQLSchemaBuilder graphQLSchemaBuilder) {
            return new GraphQLJpaExecutor(graphQLSchemaBuilder.build());
        }

        @Bean
        public GraphQLSchemaBuilder graphQLSchemaBuilder(final EntityManager entityManager) {
            return new GraphQLJpaSchemaBuilder(entityManager)
                       .name("CustomAttributeConverterSchema")
                       .description("Custom Attribute Converter Schema");
        }

    }
}
