package com.introproventures.graphql.jpa.query.schema.impl;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.util.Lists.list;

import java.util.TimeZone;
import java.util.function.Supplier;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import com.introproventures.graphql.jpa.query.schema.GraphQLExecutor;
import com.introproventures.graphql.jpa.query.schema.GraphQLExecutorContextFactory;

import graphql.ExecutionResult;
import graphql.GraphQLContext;
import graphql.Scalars;
import graphql.execution.instrumentation.Instrumentation;
import graphql.execution.instrumentation.tracing.TracingInstrumentation;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.visibility.BlockedFields;
import graphql.schema.visibility.GraphqlFieldVisibility;
import graphql.validation.ValidationErrorType;


@RunWith(SpringRunner.class)
@SpringBootTest
public class GraphQLJpaExecutorContextFactoryTest {

    @Autowired
    private GraphQLExecutor executor;

    @SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
    static class Application {
        
        @Bean
        public Supplier<GraphqlFieldVisibility> graphqlFieldVisibility() {
            return () -> BlockedFields.newBlock()
                                      .addPattern("Book.price")
                                      .build(); 
        }
        
        @Bean
        public Supplier<GraphQLContext> graphqlContext() {
            return () -> GraphQLContext.newContext()
                                       .of("username", "john")
                                       .build();
        }
        
        @Bean
        public Supplier<Instrumentation> instrumentation() {
            return () -> new TracingInstrumentation();
        }        

        @Bean
        public GraphQLExecutorContextFactory graphQLJpaExecutorContextFactory(Supplier<GraphQLContext> graphQLContext,
                                                                              Supplier<GraphqlFieldVisibility> graphqlFieldVisibility,
                                                                              Supplier<Instrumentation> instrumentation) {
            return new GraphQLJpaExecutorContextFactory().withGraphqlContext(graphQLContext)
                                                         .withGraphqlFieldVisibility(graphqlFieldVisibility)
                                                         .withInstrumentation(instrumentation);
        }
        
        @Bean
        public GraphQLExecutor graphQLExecutor(final GraphQLSchema graphQLSchema,
                                               final GraphQLExecutorContextFactory graphQLJpaExecutorContextFactory) {
            return new GraphQLJpaExecutor(graphQLSchema,
                                          graphQLJpaExecutorContextFactory);
        }

        @SuppressWarnings("deprecation")
        @Bean
        public GraphQLSchema graphQLSchema() {
            
            GraphQLObjectType query = GraphQLObjectType.newObject()
                                                       .name("query")
                                                       .field(newFieldDefinition().name("hello")
                                                                                  .type(Scalars.GraphQLString)
                                                                                  .dataFetcher(env -> {
                                                                                      GraphQLContext context = env.getContext();

                                                                                      return context.get("username");
                                                                                  })
                                                       )
                                                       .field(newFieldDefinition().name("Books")
                                                                                  .type(GraphQLObjectType.newObject()
                                                                                                         .name("Book")
                                                                                                         .field(newFieldDefinition().name("price")
                                                                                                                                    .type(Scalars.GraphQLBigDecimal)))
                                                       )
                                                       .build();
            
            return GraphQLSchema.newSchema()
                                .query(query)
                                .build();
        }
    }

    @BeforeClass
    public static void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
    
    @Test
    public void contextLoads() {
        // success
    }
    
    @Test
    public void testGraphqlFieldVisibility() {
        //given
        String query = "{ Books { price } }";
        
        //when
        ExecutionResult result = executor.execute(query);

        // then
        assertThat(result.getErrors()).isNotEmpty()
                                      .extracting("validationErrorType", "queryPath")
                                      .containsOnly(tuple(ValidationErrorType.FieldUndefined,
                                                          list("Books", "price")));
    }
    
    @Test
    public void testGraphQLContext() {
        //given
        String query = "{ hello }";
        
        //when
        ExecutionResult result = executor.execute(query);

        // then
        assertThat(result.getErrors()).isEmpty();
        
        String data = result.getData().toString();
        
        assertThat(data).isEqualTo("{hello=john}");
    }
    
    @Test
    public void testInstrumentation() {
        //given
        String query = "{ hello }";
        
        //when
        ExecutionResult result = executor.execute(query);

        // then
        assertThat(result.getExtensions()).isNotEmpty();
    }

    
}
