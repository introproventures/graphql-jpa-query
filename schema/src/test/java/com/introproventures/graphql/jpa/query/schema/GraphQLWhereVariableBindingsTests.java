package com.introproventures.graphql.jpa.query.schema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.BDDAssertions.then;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.introproventures.graphql.jpa.query.AbstractSpringBootTestSupport;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaExecutor;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;
import graphql.ExecutionResult;
import jakarta.persistence.EntityManager;
import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;

@SpringBootTest
public class GraphQLWhereVariableBindingsTests extends AbstractSpringBootTestSupport {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class Application {

        @Bean
        public GraphQLExecutor graphQLExecutor(final GraphQLSchemaBuilder graphQLSchemaBuilder) {
            return new GraphQLJpaExecutor(graphQLSchemaBuilder.build());
        }

        @Bean
        public GraphQLSchemaBuilder graphQLSchemaBuilder(final EntityManager entityManager) {
            return new GraphQLJpaSchemaBuilder(entityManager).name("GraphQLBooks").description("Books JPA test schema");
        }
    }

    @Autowired
    private GraphQLExecutor executor;

    @Test
    public void queryWithSimpleEqualsVariableBinding() throws IOException {
        //given
        String query =
            "" +
            "query($where: BooksCriteriaExpression) {" +
            "   Books(where: $where) {" +
            "       select {" +
            "           id" +
            "           title" +
            "           genre" +
            "       }" +
            "   }" +
            "}";
        String variables =
            "" +
            "{" +
            "   \"where\": {" +
            "       \"title\": {" +
            "           \"EQ\": \"War and Peace\"" +
            "       }" +
            "   }" +
            "}";

        //when
        ExecutionResult executionResult = executor.execute(query, getVariablesMap(variables));

        // then
        then(executionResult.getErrors()).isEmpty();
        Map<String, Object> result = executionResult.getData();
        then(result)
            .extracting("Books")
            .extracting("select")
            .asList()
            .hasSize(1)
            .extracting("id", "title", "genre")
            .containsOnly(tuple(2L, "War and Peace", "NOVEL"));
    }

    @Test
    public void queryWithNestedWhereClauseVariableBinding() throws IOException {
        //given
        String query =
            "" +
            "query($where: BooksCriteriaExpression) {" +
            "   Books(where: $where) {" +
            "       select {" +
            "           author {" +
            "               id" +
            "               name" +
            "           }" +
            "           title" +
            "       }" +
            "   }" +
            "}";
        String variables =
            "" +
            "{" +
            "   \"where\": {" +
            "       \"author\": {" +
            "           \"name\": {" +
            "               \"EQ\": \"Leo Tolstoy\"" +
            "           }" +
            "       }" +
            "   }" +
            "}";

        //when
        ExecutionResult executionResult = executor.execute(query, getVariablesMap(variables));

        // then
        then(executionResult.getErrors()).isEmpty();
        Map<String, Object> result = executionResult.getData();
        then(result)
            .extracting("Books")
            .extracting("select")
            .asList()
            .hasSize(2)
            .extracting("title")
            .containsOnly("War and Peace", "Anna Karenina");
        then(result)
            .extracting("Books")
            .extracting("select")
            .asList()
            .hasSize(2)
            .extracting("author")
            .extracting("id", "name")
            .containsOnly(tuple(1L, "Leo Tolstoy"));
    }

    @Test
    public void queryWithInVariableBinding() throws IOException {
        //given
        String query =
            "" +
            "query($where: BooksCriteriaExpression) {" +
            "   Books(where: $where) {" +
            "       select {" +
            "           id" +
            "           title" +
            "           genre" +
            "       }" +
            "   }" +
            "}";
        String variables =
            "" +
            "{" +
            "   \"where\": {" +
            "       \"genre\": {" +
            "           \"IN\": [\"PLAY\"]" +
            "       }" +
            "   }" +
            "}";

        //when
        ExecutionResult executionResult = executor.execute(query, getVariablesMap(variables));

        // then
        then(executionResult.getErrors()).isEmpty();
        Map<String, Object> result = executionResult.getData();
        then(result)
            .extracting("Books")
            .extracting("select")
            .asList()
            .hasSize(3)
            .extracting("title", "genre")
            .containsOnly(
                tuple("The Cherry Orchard", "PLAY"),
                tuple("The Seagull", "PLAY"),
                tuple("Three Sisters", "PLAY")
            );
    }

    @Test
    public void queryWithMultipleRestrictionForOneProperty() throws IOException {
        //given
        String query =
            "" +
            "query($where: BooksCriteriaExpression) {" +
            "   Books(where: $where) {" +
            "       select {" +
            "           id" +
            "           title" +
            "       }" +
            "   }" +
            "}";
        String variables =
            "" +
            "{" +
            "   \"where\": {" +
            "       \"id\": {" +
            "           \"GE\": 5," +
            "           \"LE\": 7" +
            "       }" +
            "   }" +
            "}";

        //when
        ExecutionResult executionResult = executor.execute(query, getVariablesMap(variables));

        // then
        then(executionResult.getErrors()).isEmpty();
        Map<String, Object> result = executionResult.getData();
        then(result)
            .extracting("Books")
            .extracting("select")
            .asList()
            .extracting("id", "title")
            .containsOnly(tuple(5L, "The Cherry Orchard"), tuple(6L, "The Seagull"), tuple(7L, "Three Sisters"));
    }

    @Test
    public void queryWithPropertyWhereVariableBinding() throws IOException {
        //given
        String query =
            "" +
            "query($booksWhereClause: BooksCriteriaExpression) {" +
            "   Authors {" +
            "       select {" +
            "           name" +
            "           books(where: $booksWhereClause) {" +
            "               genre" +
            "           }" +
            "       }" +
            "   }" +
            "}";
        String variables =
            "" +
            "{" +
            "   \"booksWhereClause\": {" +
            "       \"genre\": {" +
            "           \"IN\": [\"NOVEL\"]" +
            "       }" +
            "   }" +
            "}";

        //when
        ExecutionResult executionResult = executor.execute(query, getVariablesMap(variables));

        // then
        then(executionResult.getErrors()).isEmpty();
        Map<String, Object> result = executionResult.getData();

        String expected =
            "{Authors={select=[" +
            "{name=Leo Tolstoy, books=[{genre=NOVEL}, {genre=NOVEL}]}, " +
            "{name=Anton Chekhov, books=[]}, " +
            "{name=Igor Dianov, books=[]}" +
            "]}}";

        then(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryWithRestrictionsForMultipleProperties() throws IOException {
        //given
        String query =
            "" +
            "query($where: BooksCriteriaExpression) {" +
            "   Books(where: $where) {" +
            "       select {" +
            "           id" +
            "           title" +
            "       }" +
            "   }" +
            "}";
        String variables =
            "" +
            "{" +
            "   \"where\": {" +
            "       \"title\": {" +
            "           \"LIKE\": \"The\"" +
            "       }," +
            "		\"id\": {" +
            "			\"LT\": 6" +
            "		}" +
            "   }" +
            "}";

        //when
        ExecutionResult executionResult = executor.execute(query, getVariablesMap(variables));

        // then
        then(executionResult.getErrors()).isEmpty();
        Map<String, Object> result = executionResult.getData();
        then(result)
            .extracting("Books")
            .extracting("select")
            .asList()
            .extracting("id", "title")
            .containsOnly(tuple(5L, "The Cherry Orchard"));
    }

    @Test
    public void queryBooksWithWhereVariableCriteriaEnumListExpression() throws IOException {
        //given
        String query =
            "query($where: BooksCriteriaExpression) {" +
            "  Books (where: $where) {" +
            "    select {" +
            "      id" +
            "      title" +
            "      genre" +
            "    }" +
            "  }" +
            "}";

        String variables =
            "{" +
            "  \"where\": {" +
            "      \"genre\": {" +
            "          \"IN\": [\"NOVEL\"]" +
            "       }" +
            "   } " +
            "}";

        String expected =
            "{Books={select=[" +
            "{id=2, title=War and Peace, genre=NOVEL}, " +
            "{id=3, title=Anna Karenina, genre=NOVEL}" +
            "]}}";

        //when
        Object result = executor.execute(query, getVariablesMap(variables)).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryBooksWithWhereVariableCriteriaEnumExpression() throws IOException {
        //given
        String query =
            "query($where: BooksCriteriaExpression) {" +
            "  Books (where: $where) {" +
            "    select {" +
            "      id" +
            "      title" +
            "      genre" +
            "    }" +
            "  }" +
            "}";

        String variables =
            "{" + "  \"where\": {" + "      \"genre\": {" + "          \"IN\": \"NOVEL\"" + "       }" + "   } " + "}";

        String expected =
            "{Books={select=[" +
            "{id=2, title=War and Peace, genre=NOVEL}, " +
            "{id=3, title=Anna Karenina, genre=NOVEL}" +
            "]}}";

        //when
        Object result = executor.execute(query, getVariablesMap(variables)).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryBooksWithWhereVariableCriteriaEQEnumExpression() throws IOException {
        //given
        String query =
            "query($where: BooksCriteriaExpression) {" +
            "  Books (where: $where) {" +
            "    select {" +
            "      id" +
            "      title" +
            "      genre" +
            "    }" +
            "  }" +
            "}";

        String variables =
            "{" + "  \"where\": {" + "      \"genre\": {" + "          \"EQ\": \"NOVEL\"" + "       }" + "   } " + "}";

        String expected =
            "{Books={select=[" +
            "{id=2, title=War and Peace, genre=NOVEL}, " +
            "{id=3, title=Anna Karenina, genre=NOVEL}" +
            "]}}";

        //when
        Object result = executor.execute(query, getVariablesMap(variables)).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getVariablesMap(String variables) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return (Map<String, Object>) mapper.readValue(variables, Map.class);
    }
}
