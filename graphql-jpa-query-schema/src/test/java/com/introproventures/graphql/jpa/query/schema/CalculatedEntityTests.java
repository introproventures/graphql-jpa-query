package com.introproventures.graphql.jpa.query.schema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.util.Lists.list;

import java.util.Optional;

import javax.persistence.EntityManager;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.util.Assert;

import com.introproventures.graphql.jpa.query.AbstractSpringBootTestSupport;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaExecutor;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;

import graphql.ExecutionResult;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLSchema;
import graphql.validation.ValidationErrorType;

@SpringBootTest
public class CalculatedEntityTests extends AbstractSpringBootTestSupport {
    @SpringBootApplication
    static class Application {
        @Bean
        public GraphQLExecutor graphQLExecutor(final GraphQLSchemaBuilder graphQLSchemaBuilder) {
            return new GraphQLJpaExecutor(graphQLSchemaBuilder.build());
        }

        @Bean
        public GraphQLSchemaBuilder graphQLSchemaBuilder(final EntityManager entityManager) {

            return new GraphQLJpaSchemaBuilder(entityManager)
                    .name("GraphQLCalcFields")
                    .description("CalcFields JPA test schema");
        }

    }

    @Autowired
    private GraphQLExecutor executor;

    @Autowired
    private GraphQLSchemaBuilder schemaBuilder;

    @Test
    public void contextLoads() {
        Assert.isAssignable(GraphQLExecutor.class, executor.getClass());
    }

    @Test
    public void getAllRecords() {
        //given
        String query = "query GraphQLCalcFields { CalculatedEntities { select {id title fieldMem fieldFun logic customLogic } } }";

        String expected = "{CalculatedEntities={select=[{id=1, title=title 1, fieldMem=member, fieldFun=title 1 function, logic=true, customLogic=false}, {id=2, title=title 2, fieldMem=member, fieldFun=title 2 function, logic=true, customLogic=false}]}}";

        //when
        Object result = executor.execute(query).getData();

        //then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void testIgnoreFields() {
        String query = "" +
                "query GraphQLCalcFields { " +
                "   CalculatedEntities { " +
                "       select {" +
                "           id" +
                "           title" +
                "           fieldMem" +
                "           fieldFun" +
                "           logic" +
                "           age" +
                "           customLogic" +
                "           hideField" +
                "           hideFieldFunction" +
                "           propertyIgnoredOnGetter" +
                "           ignoredTransientValue" +
                "           transientModifier" +
                "           transientModifierGraphQLIgnore" +
                "           parentField" +
                "           parentTransientModifier" +
                "           parentTransient" +
                "           parentTransientGetter" +
                "           parentGraphQLIngore" +
                "           parentGraphQLIgnoreGetter" +
                "           parentTransientGraphQLIgnore" +
                "           parentTransientModifierGraphQLIgnore" +
                "           parentTransientGraphQLIgnoreGetter" +
                "           Uppercase" +
                "           UppercaseGetter" +
                "           UppercaseGetterIgnore" +
                "           protectedGetter" +
                "       } " +
                "   } " +
                "}";

        //when
        ExecutionResult result = executor.execute(query);

        //then
        assertThat(result.getErrors())
                .isNotEmpty()
                .extracting("validationErrorType", "queryPath")
                .containsOnly(
                        tuple(ValidationErrorType.FieldUndefined, list("CalculatedEntities", "select", "hideField")),
                        tuple(ValidationErrorType.FieldUndefined, list("CalculatedEntities", "select", "hideFieldFunction")),
                        tuple(ValidationErrorType.FieldUndefined, list("CalculatedEntities", "select", "propertyIgnoredOnGetter")),
                        tuple(ValidationErrorType.FieldUndefined, list("CalculatedEntities", "select", "ignoredTransientValue")),
                        tuple(ValidationErrorType.FieldUndefined, list("CalculatedEntities", "select", "parentGraphQLIngore")),
                        tuple(ValidationErrorType.FieldUndefined, list("CalculatedEntities", "select", "parentGraphQLIgnoreGetter")),
                        tuple(ValidationErrorType.FieldUndefined, list("CalculatedEntities", "select", "parentTransientGraphQLIgnore")),
                        tuple(ValidationErrorType.FieldUndefined, list("CalculatedEntities", "select", "parentTransientModifierGraphQLIgnore")),
                        tuple(ValidationErrorType.FieldUndefined, list("CalculatedEntities", "select", "parentTransientGraphQLIgnoreGetter")),
                        tuple(ValidationErrorType.FieldUndefined, list("CalculatedEntities", "select", "transientModifierGraphQLIgnore")),
                        tuple(ValidationErrorType.FieldUndefined, list("CalculatedEntities", "select", "transientModifierGraphQLIgnore")),
                        tuple(ValidationErrorType.FieldUndefined, list("CalculatedEntities", "select", "UppercaseGetterIgnore"))
                );
    }

    @Test
    public void shouldInheritMethodDescriptionFromBaseClass() {
        //when
        GraphQLSchema schema = schemaBuilder.build();

        //then
        Optional<GraphQLFieldDefinition> field = getFieldForType("parentTransientGetter",
                                                                 "CalculatedEntity",
                                                                 schema);
        then(field)
                .isPresent().get()
                .extracting("description")
                .isNotNull()
                .isEqualTo("getParentTransientGetter");
    }

    private Optional<GraphQLFieldDefinition> getFieldForType(String fieldName,
                                                             String type,
                                                             GraphQLSchema schema) {
        return schema.getQueryType()
                .getFieldDefinition(type)
                .getType()
                .getChildren()
                .stream()
                .map(GraphQLFieldDefinition.class::cast)
                .filter(graphQLFieldDefinition -> graphQLFieldDefinition.getName().equals(fieldName))
                .findFirst();
    }

}
