package com.introproventures.graphql.jpa.query.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;

import graphql.GraphQL;
import graphql.Scalars;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.StaticDataFetcher;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.NONE)
public class GraphQLSchemaAutoConfigurationTest {
    
    @Autowired
    private GraphQLSchema graphQLSchema;

    @Autowired
    private GraphQLJpaQueryProperties graphQLJpaQueryProperties;
    
    @SpringBootApplication
    static class Application {

        @Component
        static class MutationGraphQLSchemaConfigurer implements GraphQLSchemaConfigurer {

            @Override
            public void configure(GraphQLShemaRegistration registry) {

                GraphQLObjectType mutation = GraphQLObjectType.newObject()
                                                              .name("mutation")
                                                              .field(GraphQLFieldDefinition.newFieldDefinition()
                                                                                           .name("greet")
                                                                                           .type(Scalars.GraphQLString)
                                                                                           .dataFetcher(new StaticDataFetcher("hello world")))
                                                              .field(GraphQLFieldDefinition.newFieldDefinition()
                                                                                           .name("greet2")
                                                                                           .type(Scalars.GraphQLString))
                                                              .build();

                GraphQLCodeRegistry codeRegistry = GraphQLCodeRegistry.newCodeRegistry()
                                                                      .dataFetcher(FieldCoordinates.coordinates(mutation.getName(),
                                                                                                                "greet2"),
                                                                                   new StaticDataFetcher("hello world2"))
                                                                      .build();

                GraphQLSchema graphQLSchema = GraphQLSchema.newSchema()
                                                           .query(GraphQLObjectType.newObject().name("null"))
                                                           .mutation(mutation)
                                                           .codeRegistry(codeRegistry)
                                                           .build();
                
                registry.register(graphQLSchema);
            }
        }        
        @Component
        static class QueryGraphQLSchemaConfigurer implements GraphQLSchemaConfigurer {

            @Override
            public void configure(GraphQLShemaRegistration registry) {
                GraphQLObjectType query = GraphQLObjectType.newObject()
                                                           .name("query")
                                                           .field(GraphQLFieldDefinition.newFieldDefinition()
                                                                                        .name("hello")
                                                                                        .type(Scalars.GraphQLString)
                                                                                        .dataFetcher(new StaticDataFetcher("world")))
                                                           .field(GraphQLFieldDefinition.newFieldDefinition()
                                                                                        .name("hello2")
                                                                                        .type(Scalars.GraphQLString))
                                                           .build();

                GraphQLCodeRegistry codeRegistry = GraphQLCodeRegistry.newCodeRegistry()
                                                                      .dataFetcher(FieldCoordinates.coordinates(query.getName(),
                                                                                                                "hello2"),
                                                                                   new StaticDataFetcher("world2"))
                                                                      .build();

                GraphQLSchema graphQLSchema = GraphQLSchema.newSchema()
                                                           .query(query)
                                                           .codeRegistry(codeRegistry)
                                                           .build();
                
                registry.register(graphQLSchema);
            }
        }
    }

    @Test
    public void contextLoads() {
        // given
        GraphQL graphQL = GraphQL.newGraphQL(graphQLSchema).build();

        // when
        Map<String, Object> result = graphQL.execute("query {hello}").getData();
        Map<String, Object> result2 = graphQL.execute("mutation {greet}").getData();
        Map<String, Object> result3 = graphQL.execute("mutation {greet2}").getData();
        Map<String, Object> result4 = graphQL.execute("query {hello2}").getData();
        
        // then
        assertThat(result.toString()).isEqualTo("{hello=world}");
        assertThat(result2.toString()).isEqualTo("{greet=hello world}");
        assertThat(result3.toString()).isEqualTo("{greet2=hello world2}");
        assertThat(result4.toString()).isEqualTo("{hello2=world2}");
        
        assertThat(graphQLSchema.getQueryType())
                .extracting(GraphQLObjectType::getName, GraphQLObjectType::getDescription)
                .containsExactly("GraphQLBooks", "GraphQL Books Schema Description");
        	
    }
    

    @Test
    public void defaultConfigurationProperties() {
        // given
        assertThat(graphQLJpaQueryProperties.isDefaultDistinct()).isTrue();
        assertThat(graphQLJpaQueryProperties.isUseDistinctParameter()).isFalse();
        assertThat(graphQLJpaQueryProperties.isToManyDefaultOptional()).isTrue();
    }
        

}
