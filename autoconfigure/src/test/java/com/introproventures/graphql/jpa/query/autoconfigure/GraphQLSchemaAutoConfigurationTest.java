package com.introproventures.graphql.jpa.query.autoconfigure;

import static graphql.annotations.AnnotationsSchemaCreator.newAnnotationsSchema;
import static graphql.schema.FieldCoordinates.coordinates;
import static graphql.schema.GraphQLCodeRegistry.newCodeRegistry;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;
import static graphql.schema.GraphQLSchema.newSchema;
import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

import com.introproventures.graphql.jpa.query.autoconfigure.support.AdditionalGraphQLType;
import com.introproventures.graphql.jpa.query.autoconfigure.support.MutationRoot;
import com.introproventures.graphql.jpa.query.autoconfigure.support.QueryRoot;
import com.introproventures.graphql.jpa.query.autoconfigure.support.SubscriptionRoot;
import com.introproventures.graphql.jpa.query.autoconfigure.support.TestEntity;
import com.introproventures.graphql.jpa.query.schema.JavaScalars;
import com.introproventures.graphql.jpa.query.schema.JavaScalarsWiringPostProcessor;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.Scalars;
import graphql.annotations.AnnotationsSchemaCreator;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLInvokeDetached;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.directives.definition.GraphQLDirectiveDefinition;
import graphql.scalars.ExtendedScalars;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLNamedType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.StaticDataFetcher;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
public class GraphQLSchemaAutoConfigurationTest {

    @Autowired
    private GraphQLSchema graphQLSchema;

    @Autowired
    private GraphQLJpaSchemaBuilder graphQLJpaSchemaBuilder;

    @Autowired
    private Supplier<EntityManager> entityManagerSupplier;

    @Autowired
    private QueryExecutionStrategyProvider queryExecutionStrategy;

    @Autowired
    private MutationExecutionStrategyProvider mutationExecutionStrategy;

    @Autowired
    private SubscriptionExecutionStrategyProvider subscriptionExecutionStrategy;

    @Autowired
    private GraphQLJpaQueryProperties graphQLJpaQueryProperties;

    @SpringBootApplication
    @EnableGraphQLJpaQuerySchema(basePackageClasses = TestEntity.class)
    static class Application {

        @PersistenceContext
        EntityManager entityManager;

        @Bean
        @GraphQLSchemaEntityManager
        Supplier<EntityManager> persistentContextEntityManager() {
            return () -> entityManager;
        }

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

                if (!StringUtils.isEmpty(directivesPackage)) {
                    Reflections reflections = new Reflections(directivesPackage);

                    Set<Class<?>> directiveDeclarations = reflections.getTypesAnnotatedWith(
                        GraphQLDirectiveDefinition.class
                    );

                    builder.directives(directiveDeclarations);
                }

                if (additionalGraphQLTypes != null) {
                    builder.additionalTypes(
                        additionalGraphQLTypes.stream().map(AdditionalGraphQLType::getClass).collect(Collectors.toSet())
                    );
                }

                registry.register(builder.build());
            }

            @GraphQLName("null")
            class NullQuery implements QueryRoot {}
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

            @GraphQLField
            @GraphQLInvokeDetached
            public Long count() {
                return Long.valueOf(1);
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
                GraphQLObjectType mutation = newObject()
                    .name("mutation")
                    .field(
                        GraphQLFieldDefinition
                            .newFieldDefinition()
                            .name("greet")
                            .type(Scalars.GraphQLString)
                            .dataFetcher(new StaticDataFetcher("hello world"))
                    )
                    .field(GraphQLFieldDefinition.newFieldDefinition().name("greet2").type(Scalars.GraphQLString))
                    .field(GraphQLFieldDefinition.newFieldDefinition().name("count1").type(ExtendedScalars.GraphQLLong))
                    .build();

                GraphQLCodeRegistry codeRegistry = newCodeRegistry()
                    .dataFetcher(coordinates(mutation.getName(), "greet2"), new StaticDataFetcher("hello world2"))
                    .build();

                GraphQLSchema graphQLSchema = newSchema()
                    .query(
                        newObject()
                            .name("null")
                            .field(GraphQLFieldDefinition.newFieldDefinition().name("null").type(Scalars.GraphQLString))
                    )
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
                GraphQLObjectType query = newObject()
                    .name("query")
                    .field(
                        newFieldDefinition()
                            .name("hello")
                            .type(Scalars.GraphQLString)
                            .dataFetcher(new StaticDataFetcher("world"))
                    )
                    .field(newFieldDefinition().name("hello2").type(Scalars.GraphQLString))
                    .field(
                        newFieldDefinition()
                            .name("hello3")
                            .type(
                                newObject()
                                    .name("Hello3")
                                    .field(newFieldDefinition().name("canada").type(Scalars.GraphQLString))
                                    .field(newFieldDefinition().name("america").type(Scalars.GraphQLString))
                            )
                    )
                    .build();

                GraphQLCodeRegistry codeRegistry = newCodeRegistry()
                    .dataFetcher(coordinates(query.getName(), "hello2"), new StaticDataFetcher("world2"))
                    .dataFetcher(coordinates(query.getName(), "hello3"), new StaticDataFetcher(emptyMap()))
                    .dataFetcher(coordinates("Hello3", "america"), new StaticDataFetcher("Hi!"))
                    .dataFetcher(coordinates("Hello3", "canada"), new StaticDataFetcher("Eh?"))
                    .build();

                GraphQLSchema graphQLSchema = newSchema().query(query).codeRegistry(codeRegistry).build();

                registry.register(graphQLSchema);
            }
        }

        @Component
        static class GraphQLSchemaGeneratorConfigurer implements GraphQLSchemaConfigurer {

            @Value("classpath:activiti.graphqls")
            private Resource schemaResource;

            @Override
            public void configure(GraphQLShemaRegistration registry) {
                File schemaFile = null;
                try {
                    schemaFile = schemaResource.getFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(schemaFile);

                Supplier<Map> generator = () ->
                    new LinkedHashMap() {
                        {
                            put("id", UUID.randomUUID());
                            put("timestamp", Instant.now().toEpochMilli());
                            put("entity", emptyMap());
                        }
                    };

                DataFetcher<Flux<List<Map>>> dataFetcher = new StaticDataFetcher(
                    Flux
                        .fromStream(Stream.generate(generator))
                        .delayElements(Duration.ofMillis(10))
                        .take(100)
                        .buffer(10)
                );

                GraphQLCodeRegistry codeRegistry = newCodeRegistry()
                    .dataFetcher(coordinates("Subscription", "engineEvents"), dataFetcher)
                    .build();

                RuntimeWiring.Builder wiring = newRuntimeWiring()
                    .codeRegistry(codeRegistry)
                    .scalar(
                        GraphQLScalarType
                            .newScalar()
                            .name("ObjectScalar")
                            .description("An object scalar")
                            .coercing(new JavaScalars.GraphQLObjectCoercing())
                            .build()
                    )
                    .scalar(ExtendedScalars.GraphQLLong)
                    .transformer(new JavaScalarsWiringPostProcessor());

                registry.register(new SchemaGenerator().makeExecutableSchema(typeRegistry, wiring.build()));
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
    public void enableGraphQLJpaQuerySchema() {
        assertThat(graphQLSchema.getAllTypesAsList()).extracting(GraphQLNamedType::getName).contains("TestEntity");
    }

    @Test
    public void directivesSupport() {
        assertThat(graphQLSchema.getDirectives())
            .extracting(GraphQLDirective::getName)
            .containsOnly("include", "skip", "specifiedBy", "deprecated");
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
    public void schemaGeneratorConfigurer() {
        // given
        GraphQL graphQL = GraphQL.newGraphQL(graphQLSchema).build();

        // when
        ExecutionResult result = graphQL.execute("subscription { engineEvents { id, timestamp, entity } }");

        Publisher<ExecutionResult> source = result.getData();

        // then
        StepVerifier.create(source).expectSubscription().expectNextCount(10).verifyComplete();
    }

    @Test
    void defaultConfigurationProperties() {
        // given
        assertThat(graphQLJpaQueryProperties.isDefaultDistinct()).isTrue();
        assertThat(graphQLJpaQueryProperties.isUseDistinctParameter()).isFalse();
        assertThat(graphQLJpaQueryProperties.isToManyDefaultOptional()).isTrue();
    }

    @Test
    void configuresSharedEntityManager() {
        // given
        assertThat(graphQLJpaSchemaBuilder.getEntityManager()).isEqualTo(entityManagerSupplier.get());
    }
}
