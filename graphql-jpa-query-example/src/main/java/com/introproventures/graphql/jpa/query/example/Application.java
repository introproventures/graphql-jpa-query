package com.introproventures.graphql.jpa.query.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
//    @Bean
//    public GraphQLExecutor graphQLExecutor(final GraphQLSchemaBuilder graphQLSchemaBuilder) {
//        return new GraphQLJpaExecutor(graphQLSchemaBuilder.build());
//    }
//
//    @Bean
//    public GraphQLSchemaBuilder graphQLSchemaBuilder(final EntityManager entityManager) {
//        
//        return new GraphQLJpaSchemaBuilder(entityManager)
//            .name("GrapQLBooks")
//            .description("Books JPA Test Schema");
//    }
    
}
