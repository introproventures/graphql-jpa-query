package com.introproventures.graphql.jpa.query.autoconfigure;

import static graphql.annotations.AnnotationsSchemaCreator.newAnnotationsSchema;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

import com.introproventures.graphql.jpa.query.autoconfigure.support.AdditionalGraphQLType;
import com.introproventures.graphql.jpa.query.autoconfigure.support.MutationRoot;
import com.introproventures.graphql.jpa.query.autoconfigure.support.QueryRoot;
import com.introproventures.graphql.jpa.query.autoconfigure.support.SubscriptionRoot;

import graphql.Directives;
import graphql.GraphQL;
import graphql.Scalars;
import graphql.annotations.AnnotationsSchemaCreator;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLInvokeDetached;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.directives.definition.GraphQLDirectiveDefinition;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLDirective;
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
        
        @Configuration
        static class GraphQLAnnotationsSchemaConfigurer implements GraphQLSchemaConfigurer {
            @Autowired(required = false)
            private QueryRoot queryRoot = new NullQuery();

            @Autowired(required = false)
            private MutationRoot mutationRoot;

            @Autowired(required = false)
            private SubscriptionRoot subscriptionRoot;

            @Autowired(required = false)
            private Set<AdditionalGraphQLType> additionalGraphQLTypes;

            @Value("${directives.package:}")
            private String directivesPackage;

            @Override
            public void configure(GraphQLShemaRegistration registry) {
                AnnotationsSchemaCreator.Builder builder = newAnnotationsSchema();
                builder.query(queryRoot.getClass());
                if (mutationRoot != null) {
                    builder.mutation(mutationRoot.getClass());
                }
                if (subscriptionRoot != null) {
                    builder.subscription(subscriptionRoot.getClass());
                }

                if(!StringUtils.isEmpty(directivesPackage)) {
                    Reflections reflections = new Reflections(directivesPackage);
    
                    Set<Class<?>> directiveDeclarations = reflections.getTypesAnnotatedWith(GraphQLDirectiveDefinition.class);
    
                    builder.directives(directiveDeclarations);
                }

                if (additionalGraphQLTypes!=null) {
                    builder.additionalTypes(additionalGraphQLTypes.stream()
                                                                  .map(AdditionalGraphQLType::getClass)
                                                                  .collect(Collectors.toSet()));
                }

                registry.register(builder.build());
            }
            
            @GraphQLName("null")
            class NullQuery implements QueryRoot {
                
            }
        }
        
        @Component
        public static class AnnotatedMutation implements MutationRoot {
            
             @GraphQLField
             @GraphQLInvokeDetached
             public String salut(@GraphQLName("name") String name) {
                 return "Salut, " + name + "!";
             }
        }
        
        @Component
        public static class AnnotatedQuery implements QueryRoot {
             @GraphQLField
             @GraphQLInvokeDetached
             public Greeting greeting(@GraphQLName("name") String name) {
                 return new Greeting("Hi, " + name + "!");
             }
             
            public static class Greeting {

                @GraphQLField
                public String value;

                public Greeting(String value) {
                    this.value = value;
                }
            }
        }
        
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
                                                           .query(GraphQLObjectType.newObject().name("null")
                                                                                   .field(GraphQLFieldDefinition.newFieldDefinition()
                                                                                                                .name("null")
                                                                                                                .type(Scalars.GraphQLString)))
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
                                                           .field(newFieldDefinition().name("hello")
                                                                                      .type(Scalars.GraphQLString)
                                                                                      .dataFetcher(new StaticDataFetcher("world")))
                                                           .field(newFieldDefinition().name("hello2")
                                                                                      .type(Scalars.GraphQLString))
                                                           .field(newFieldDefinition().name("hello3")
                                                                                      .type(GraphQLObjectType.newObject()
                                                                                                             .name("Hello3")
                                                                                                             .field(newFieldDefinition().name("canada")
                                                                                                                                        .type(Scalars.GraphQLString))                                                                                                             
                                                                                                             .field(newFieldDefinition().name("america")
                                                                                                                                        .type(Scalars.GraphQLString))))
                                                           .build();

                GraphQLCodeRegistry codeRegistry = GraphQLCodeRegistry.newCodeRegistry()
                                                                      .dataFetcher(FieldCoordinates.coordinates(query.getName(), "hello2"),
                                                                                   new StaticDataFetcher("world2"))
                                                                      .dataFetcher(FieldCoordinates.coordinates(query.getName(), "hello3"),
                                                                                   new StaticDataFetcher(Collections.emptyMap()))
                                                                      .dataFetcher(FieldCoordinates.coordinates("Hello3", "america"),
                                                                                   new StaticDataFetcher("Hi!"))
                                                                      .dataFetcher(FieldCoordinates.coordinates("Hello3", "canada"),
                                                                                   new StaticDataFetcher("Eh?"))
                                                                      .build();

                GraphQLSchema graphQLSchema = GraphQLSchema.newSchema()
                                                           .query(query)
                                                           .codeRegistry(codeRegistry)
                                                           //.additionalDirective(Directives.DeferDirective)
                                                           .build();
                
                registry.register(graphQLSchema);
            }
        }
    }

    @Test
    public void contextLoads() {
        assertThat(graphQLSchema.getQueryType())
                .extracting(GraphQLObjectType::getName, GraphQLObjectType::getDescription)
                .containsExactly("GraphQLBooks", "GraphQL Books Schema Description");
        	
    }
    
    @Test
    public void directivesSupport() {
        assertThat(graphQLSchema.getDirectives())
                .extracting(GraphQLDirective::getName)
                .containsExactly("include", "skip", "specifiedBy", "deprecated");
    }
    
    @Test
    public void querySchemaConfigurer() {
        // given
        GraphQL graphQL = GraphQL.newGraphQL(graphQLSchema).build();

        // when
        Map<String, Object> query1 = graphQL.execute("query {hello}").getData();
        Map<String, Object> query2 = graphQL.execute("query {hello2}").getData();
        Map<String, Object> query3 = graphQL.execute("query {hello3 { america canada } }").getData();
        
        // then
        assertThat(query1.toString()).isEqualTo("{hello=world}");
        assertThat(query2.toString()).isEqualTo("{hello2=world2}");
        assertThat(query3.toString()).isEqualTo("{hello3={america=Hi!, canada=Eh?}}");
    }

    @Test
    public void mutationSchemaConfigurer() {
        // given
        GraphQL graphQL = GraphQL.newGraphQL(graphQLSchema).build();

        // when
        Map<String, Object> query1 = graphQL.execute("mutation {greet}").getData();
        Map<String, Object> query2 = graphQL.execute("mutation {greet2}").getData();
        
        // then
        assertThat(query1.toString()).isEqualTo("{greet=hello world}");
        assertThat(query2.toString()).isEqualTo("{greet2=hello world2}");
    }

    @Test
    public void annotatedSchemaConfigurer() {
        // given
        GraphQL graphQL = GraphQL.newGraphQL(graphQLSchema).build();

        // when
        Map<String, Object> mutation = graphQL.execute("mutation { salut(name: \"dude\") }").getData();
        Map<String, Object> query = graphQL.execute("query { greeting(name: \"dude\") { value } }").getData();
        
        // then
        assertThat(mutation.toString()).isEqualTo("{salut=Salut, dude!}");
        assertThat(query.toString()).isEqualTo("{greeting={value=Hi, dude!}}");
    }
    

    @Test
    public void defaultConfigurationProperties() {
        // given
        assertThat(graphQLJpaQueryProperties.isDefaultDistinct()).isTrue();
        assertThat(graphQLJpaQueryProperties.isUseDistinctParameter()).isFalse();
        assertThat(graphQLJpaQueryProperties.isToManyDefaultOptional()).isTrue();
    }
        

}
