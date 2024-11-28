/*
 * Copyright 2017 IntroPro Ventures Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.introproventures.graphql.jpa.query.converter;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.introproventures.graphql.jpa.query.AbstractSpringBootTestSupport;
import com.introproventures.graphql.jpa.query.converter.model.JsonEntity;
import com.introproventures.graphql.jpa.query.converter.model.TaskEntity;
import com.introproventures.graphql.jpa.query.converter.model.TaskVariableEntity;
import com.introproventures.graphql.jpa.query.converter.model.VariableValue;
import com.introproventures.graphql.jpa.query.schema.GraphQLExecutor;
import com.introproventures.graphql.jpa.query.schema.GraphQLSchemaBuilder;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaExecutor;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import jakarta.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;

@SpringBootTest(
    properties = {
        "spring.sql.init.data-locations=GraphQLJpaConverterTests.sql",
        "spring.datasource.url=jdbc:h2:mem:db;NON_KEYWORDS=VALUE",
    }
)
public class GraphQLJpaConverterTests extends AbstractSpringBootTestSupport {

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

    @Autowired
    private GraphQLExecutor executor;

    @Autowired
    private EntityManager entityManager;

    @Test
    public void contextLoads() {}

    @Test
    @Transactional
    public void queryTester() {
        // given:
        Query query = entityManager.createQuery(
            "select json from JsonEntity json where cast(json.attributes as string) LIKE '%key%'"
        );

        // when:
        List<?> result = query.getResultList();

        // then:
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
    }

    @Test
    @Transactional
    public void criteriaTester() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<JsonEntity> criteria = builder.createQuery(JsonEntity.class);
        Root<JsonEntity> json = criteria.from(JsonEntity.class);

        JsonNode value = new ObjectMapper()
            .valueToTree(Collections.singletonMap("attr", new String[] { "1", "2", "3", "4", "5" }));
        criteria.select(json).where(builder.equal(json.get("attributes"), value));

        // when:
        List<?> result = entityManager.createQuery(criteria).getResultList();

        // then:
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
    }

    @Test
    @Transactional
    public void criteriaTester2() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<TaskVariableEntity> criteria = cb.createQuery(TaskVariableEntity.class);
        Root<TaskVariableEntity> taskVariable = criteria.from(TaskVariableEntity.class);

        Boolean object = Boolean.TRUE;

        VariableValue<Boolean> variableValue = new VariableValue<>(object);
        criteria
            .select(taskVariable)
            .where(
                cb.and(cb.equal(cb.lower(taskVariable.get("name")), "variable2")),
                cb.in(taskVariable.get("value")).value(variableValue)
            );

        // when:
        List<?> result = entityManager.createQuery(criteria).getResultList();

        // then:
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
    }

    @Test
    @Transactional
    public void criteriaTester3() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<TaskEntity> tasksQuery = cb.createQuery(TaskEntity.class);
        Root<TaskEntity> task = tasksQuery.from(TaskEntity.class);

        Subquery<TaskVariableEntity> subquery = tasksQuery.subquery(TaskVariableEntity.class);
        Root<TaskVariableEntity> taskVariables = subquery.from(TaskVariableEntity.class);

        Predicate isOwner = cb.in(taskVariables.get("task")).value(task);

        Predicate var1 = cb.and(
            cb.equal(taskVariables.get("name"), "variable2"),
            cb.equal(taskVariables.get("value"), new VariableValue<>(Boolean.TRUE))
        );

        Predicate var2 = cb.and(
            cb.equal(taskVariables.get("name"), "variable1"),
            cb.equal(taskVariables.get("value"), new VariableValue<>(new String("data")))
        );

        tasksQuery
            .select(task)
            .where(
                cb.and(
                    cb.exists(subquery.select(taskVariables).where(cb.and(isOwner, var1))),
                    cb.exists(subquery.select(taskVariables).where(cb.and(isOwner, var2)))
                )
            );
        // when:
        List<TaskEntity> result = entityManager.createQuery(tasksQuery).getResultList();

        // then:
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
    }

    @Test
    @Transactional
    public void criteriaTesterMultipleJoinWhereCriteria() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<TaskEntity> tasksQuery = cb.createQuery(TaskEntity.class);
        Root<TaskEntity> task = tasksQuery.from(TaskEntity.class);

        Join<TaskEntity, TaskVariableEntity> taskVariableEntityJoin1 = task.join("variables");

        Predicate var1 = cb.and(
            cb.equal(taskVariableEntityJoin1.get("name"), "variable2"),
            cb.equal(taskVariableEntityJoin1.get("value"), new VariableValue<>(Boolean.TRUE))
        );

        taskVariableEntityJoin1.on(var1);
        taskVariableEntityJoin1.alias("var1");

        Join<TaskEntity, TaskVariableEntity> taskVariableEntityJoin2 = task.join("variables");
        Predicate var2 = cb.and(
            cb.equal(taskVariableEntityJoin2.get("name"), "variable1"),
            cb.equal(taskVariableEntityJoin2.get("value"), new VariableValue<>(new String("data")))
        );

        taskVariableEntityJoin2.on(var2);
        taskVariableEntityJoin2.alias("var2");

        tasksQuery.select(task);
        // when:
        List<TaskEntity> result = entityManager.createQuery(tasksQuery).getResultList();

        // then:
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
    }

    @Test
    @Transactional
    public void criteriaTester4() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<TaskEntity> tasksQuery = cb.createQuery(TaskEntity.class);
        Root<TaskEntity> task = tasksQuery.from(TaskEntity.class);

        Subquery<TaskVariableEntity> subquery = tasksQuery.subquery(TaskVariableEntity.class);
        Root<TaskEntity> taskCorrelation = subquery.correlate(task);

        Join<TaskEntity, TaskVariableEntity> taskVariables = taskCorrelation.join("variables");

        Predicate var1 = cb.and(
            cb.equal(taskVariables.get("name"), "variable2"),
            cb.equal(taskVariables.get("value"), new VariableValue<>(Boolean.TRUE))
        );

        Predicate var2 = cb.and(
            cb.equal(taskVariables.get("name"), "variable1"),
            cb.equal(taskVariables.get("value"), new VariableValue<>(new String("data")))
        );

        tasksQuery
            .select(task)
            .where(
                cb.and(
                    cb.exists(subquery.select(taskVariables).where(var1)),
                    cb.exists(subquery.select(taskVariables).where(var2))
                )
            );
        // when:
        List<TaskEntity> result = entityManager.createQuery(tasksQuery).getResultList();

        // then:
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
    }

    @Test
    @Transactional
    public void criteriaTester5() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<TaskEntity> tasksQuery = cb.createQuery(TaskEntity.class);
        Root<TaskEntity> task = tasksQuery.from(TaskEntity.class);

        Subquery<TaskVariableEntity> subquery = tasksQuery.subquery(TaskVariableEntity.class);
        Root<TaskEntity> taskCorrelation = subquery.correlate(task);

        Join<TaskEntity, TaskVariableEntity> taskVariables = taskCorrelation.join("variables");

        Predicate var1 = cb.and(
            cb.equal(taskVariables.get("name"), "variable2"),
            cb.equal(taskVariables.get("value"), new VariableValue<>(Boolean.TRUE))
        );

        Predicate var2 = cb.and(
            cb.equal(taskVariables.get("name"), "variable1"),
            cb.equal(taskVariables.get("value"), new VariableValue<>(new String("data")))
        );

        tasksQuery
            .select(task)
            .where(
                cb.and(
                    cb.not(cb.exists(subquery.select(taskVariables).where(var1))),
                    cb.not(cb.exists(subquery.select(taskVariables).where(var2)))
                )
            );
        // when:
        List<TaskEntity> result = entityManager.createQuery(tasksQuery).getResultList();

        // then:
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(5);
    }

    @Test
    @Transactional
    public void criteriaTester6() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<TaskEntity> tasksQuery = cb.createQuery(TaskEntity.class);
        Root<TaskEntity> task = tasksQuery.from(TaskEntity.class);

        Subquery<TaskVariableEntity> subquery = tasksQuery.subquery(TaskVariableEntity.class);
        Root<TaskEntity> taskCorrelation = subquery.correlate(task);

        Join<TaskEntity, TaskVariableEntity> taskVariables = taskCorrelation.join("variables");

        Predicate var1 = cb.and(
            cb.equal(taskVariables.get("name"), "variable2"),
            cb.equal(taskVariables.get("value"), new VariableValue<>(Boolean.TRUE))
        );

        Predicate var2 = cb.and(
            cb.equal(taskVariables.get("name"), "variable1"),
            cb.equal(taskVariables.get("value"), new VariableValue<>(new String("data")))
        );

        tasksQuery
            .select(task)
            .where(
                cb.not(
                    cb.or(
                        cb.exists(subquery.select(taskVariables).where(var1)),
                        cb.exists(subquery.select(taskVariables).where(var2))
                    )
                )
            );
        // when:
        List<TaskEntity> result = entityManager.createQuery(tasksQuery).getResultList();

        // then:
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(5);
    }

    @Test // Problem with generating cast() in the where expression
    @Transactional
    public void criteriaTesterLike() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<JsonEntity> criteria = builder.createQuery(JsonEntity.class);
        Root<JsonEntity> json = criteria.from(JsonEntity.class);

        criteria.select(json).where(builder.like(json.get("attributes").as(String.class), "%key%"));

        // when:
        List<?> result = entityManager.createQuery(criteria).getResultList();

        // then:
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
    }

    @Test
    @Transactional
    public void criteriaTesterLocate() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<JsonEntity> criteria = builder.createQuery(JsonEntity.class);
        Root<JsonEntity> json = criteria.from(JsonEntity.class);

        criteria.select(json).where(builder.gt(builder.locate(json.<String>get("attributes"), "key"), 0));

        // when:
        List<?> result = entityManager.createQuery(criteria).getResultList();

        // then:
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
    }

    @Test
    public void queryJsonEntity() {
        //given
        String query =
            "query {" +
            "  JsonEntities {" +
            "    select {" +
            "      id" +
            "      firstName" +
            "      lastName" +
            "      attributes" +
            "    }" +
            "  }" +
            "}";

        String expected =
            "{JsonEntities={select=[" +
            "{id=1, firstName=john, lastName=doe, attributes={\"attr\":{\"key\":[\"1\",\"2\",\"3\",\"4\",\"5\"]}}}, " +
            "{id=2, firstName=joe, lastName=smith, attributes={\"attr\":[\"1\",\"2\",\"3\",\"4\",\"5\"]}}" +
            "]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryJsonEntityWhereSearchCriteria() {
        //given
        String query =
            "query {" +
            "  JsonEntities(where: {" +
            "attributes: {LOCATE: \"key\"}" +
            "}) {" +
            "    select {" +
            "      id" +
            "      firstName" +
            "      lastName" +
            "      attributes" +
            "    }" +
            "  }" +
            "}";

        String expected =
            "{JsonEntities={select=[" +
            "{id=1, firstName=john, lastName=doe, attributes={\"attr\":{\"key\":[\"1\",\"2\",\"3\",\"4\",\"5\"]}}}" +
            "]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryTaskVariablesWhereSearchCriteria() {
        //given
        String query =
            "query {" +
            "  TaskVariables(where: {" +
            "value: {LOCATE: \"true\"}" +
            "}) {" +
            "    select {" +
            "      id" +
            "      name" +
            "      value" +
            "    }" +
            "  }" +
            "}";

        String expected = "{TaskVariables={select=[{id=2, name=variable2, value=true}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryTaskVariablesWhereSearchCriteriaVariableBinding() {
        //given
        String query =
            "query($value: VariableValue!) {" +
            "  TaskVariables(where: {" +
            "value: {LOCATE: $value }" +
            "}) {" +
            "    select {" +
            "      id" +
            "      name" +
            "      value" +
            "    }" +
            "  }" +
            "}";

        Map<String, Object> variables = Collections.singletonMap("value", true);

        String expected = "{TaskVariables={select=[{id=2, name=variable2, value=true}]}}";

        //when
        Object result = executor.execute(query, variables).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryProcessVariablesWhereSearchCriteriaVariableBindings() {
        //given
        String query =
            "query($value: VariableValue!)  {" +
            " ProcessVariables(where: {" +
            "value: {LOCATE: $value}" +
            "}) {" +
            "    select {" +
            "      id" +
            "      name" +
            "      value" +
            "    }" +
            "  }" +
            "}";

        Map<String, Object> variables = Collections.singletonMap("value", "[\"1\",\"2\",\"3\",\"4\",\"5\"]");

        String expected = "{ProcessVariables={select=[{id=1, name=document, value={key=[1, 2, 3, 4, 5]}}]}}";

        //when
        Object result = executor.execute(query, variables).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryProcessVariablesWhereSearchCriteria() {
        //given
        String query =
            "query {" +
            " ProcessVariables(where: {" +
            "value: {LOCATE: \"[\\\"1\\\",\\\"2\\\",\\\"3\\\",\\\"4\\\",\\\"5\\\"]\"}" +
            "}) {" +
            "    select {" +
            "      id" +
            "      name" +
            "      value" +
            "    }" +
            "  }" +
            "}";

        String expected = "{ProcessVariables={select=[{id=1, name=document, value={key=[1, 2, 3, 4, 5]}}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryProcessVariablesWhereWithEQStringSearchCriteria() {
        //given
        String query =
            "query {" +
            " TaskVariables(where: {" +
            "name: {EQ: \"variable1\"}" +
            "value: {EQ: \"data\"}" +
            "}) {" +
            "    select {" +
            "      name" +
            "      value" +
            "    }" +
            "  }" +
            "}";

        String expected = "{TaskVariables={select=[{name=variable1, value=data}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryProcessVariablesWhereWithEQBooleanSearchCriteria() {
        //given
        String query =
            "query {" +
            " TaskVariables(where: {" +
            "name: {EQ: \"variable2\"}" +
            "value: {EQ: true}" +
            "}) {" +
            "    select {" +
            "      name" +
            "      value" +
            "    }" +
            "  }" +
            "}";

        String expected = "{TaskVariables={select=[{name=variable2, value=true}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryProcessVariablesWhereWithEQNullSearchCriteria() {
        //given
        String query =
            "query {" +
            " TaskVariables(where: {" +
            "name: {EQ: \"variable3\"}" +
            "value: {EQ: null}" +
            "}) {" +
            "    select {" +
            "      name" +
            "      value" +
            "    }" +
            "  }" +
            "}";

        String expected = "{TaskVariables={select=[{name=variable3, value=null}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryProcessVariablesWhereWithEQIntSearchCriteria() {
        //given
        String query =
            "query {" +
            " TaskVariables(where: {" +
            "name: {EQ: \"variable6\"}" +
            "value: {EQ: 12345}" +
            "}) {" +
            "    select {" +
            "      name" +
            "      value" +
            "    }" +
            "  }" +
            "}";

        String expected = "{TaskVariables={select=[{name=variable6, value=12345}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryProcessVariablesWhereWithEQDoubleSearchCriteria() {
        //given
        String query =
            "query {" +
            " TaskVariables(where: {" +
            "name: {EQ: \"variable5\"}" +
            "value: {EQ: 1.2345}" +
            "}) {" +
            "    select {" +
            "      name" +
            "      value" +
            "    }" +
            "  }" +
            "}";

        String expected = "{TaskVariables={select=[{name=variable5, value=1.2345}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryProcessVariablesWhereWithINSingleValueSearchCriteria() {
        //given
        String query =
            "query {" +
            " TaskVariables(where: {" +
            "value: {IN: 1.2345}" +
            "}) {" +
            "    select {" +
            "      name" +
            "      value" +
            "    }" +
            "  }" +
            "}";

        String expected = "{TaskVariables={select=[{name=variable5, value=1.2345}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryProcessVariablesWhereWithINListValueSearchCriteria() {
        //given
        String query =
            "query {" +
            " TaskVariables(where: {" +
            "value: {IN: [1.2345]}" +
            "}) {" +
            "    select {" +
            "      name" +
            "      value" +
            "    }" +
            "  }" +
            "}";

        String expected = "{TaskVariables={select=[{name=variable5, value=1.2345}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryProcessVariablesWhereWithNINListValueSearchCriteria() {
        //given
        String query =
            "query {" +
            " TaskVariables(where: {" +
            "value: {NIN: [null]}" +
            "}) {" +
            "    select {" +
            "      name" +
            "      value" +
            "    }" +
            "  }" +
            "}";

        String expected =
            "{TaskVariables={select=[" +
            "{name=variable1, value=data}, " +
            "{name=variable2, value=true}, " +
            "{name=variable4, value={key=data}}, " +
            "{name=variable5, value=1.2345}, " +
            "{name=variable6, value=12345}, " +
            "{name=variable7, value=[1, 2, 3, 4, 5]}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryProcessVariablesWhereWithNINSingleValueSearchCriteria() {
        //given
        String query =
            "query {" +
            " TaskVariables(where: {" +
            "value: {NIN: null}" +
            "}) {" +
            "    select {" +
            "      name" +
            "      value" +
            "    }" +
            "  }" +
            "}";

        String expected =
            "{TaskVariables={select=[" +
            "{name=variable1, value=data}, " +
            "{name=variable2, value=true}, " +
            "{name=variable4, value={key=data}}, " +
            "{name=variable5, value=1.2345}, " +
            "{name=variable6, value=12345}, " +
            "{name=variable7, value=[1, 2, 3, 4, 5]}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryProcessVariablesWhereWithNEValueSearchCriteria() {
        //given
        String query =
            "query {" +
            " TaskVariables(where: {" +
            "value: {NE: null}" +
            "}) {" +
            "    select {" +
            "      name" +
            "      value" +
            "    }" +
            "  }" +
            "}";

        String expected =
            "{TaskVariables={select=[" +
            "{name=variable1, value=data}, " +
            "{name=variable2, value=true}, " +
            "{name=variable4, value={key=data}}, " +
            "{name=variable5, value=1.2345}, " +
            "{name=variable6, value=12345}, " +
            "{name=variable7, value=[1, 2, 3, 4, 5]}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryProcessVariablesWhereWithINListTypedValueSearchCriteria() {
        //given
        String query =
            "query {" +
            " TaskVariables(where: {" +
            "value: {IN: [null, true, \"data\", 12345, 1.2345]}" +
            "}) {" +
            "    select {" +
            "      name" +
            "      value" +
            "    }" +
            "  }" +
            "}";

        String expected =
            "{TaskVariables={select=[" +
            "{name=variable1, value=data}, " +
            "{name=variable2, value=true}, " +
            "{name=variable3, value=null}, " +
            "{name=variable5, value=1.2345}, " +
            "{name=variable6, value=12345}" +
            "]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryTasksVariablesWhereWithExplicitANDEXISTSByNameAndValueCriteria() {
        //given
        String query =
            "query {" +
            "  Tasks(where: {" +
            "    status: {EQ: COMPLETED}" +
            "    AND: [{" +
            "      EXISTS: {" +
            "        variables: {" +
            "          name: {EQ: \"variable1\"}" +
            "          value: {EQ: \"data\"}" +
            "        }" +
            "      }" +
            "    } {" +
            "      EXISTS: {" +
            "        variables: {" +
            "          name: {EQ: \"variable2\"}" +
            "          value: {EQ: true}" +
            "        }" +
            "      }" +
            "    }]" +
            "  }) {" +
            "    select {" +
            "      id" +
            "      status" +
            "      variables {" +
            "        name" +
            "        value" +
            "      }" +
            "    }" +
            "  }" +
            "}";

        String expected =
            "{Tasks={select=[" +
            "{id=1, status=COMPLETED, variables=[" +
            "{name=variable2, value=true}, " +
            "{name=variable1, value=data}]}" +
            "]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryTasksVariablesWhereWithExplicitANDByMultipleNameAndValueCriteria() {
        //given
        String query =
            "query {" +
            "  Tasks(where: {" +
            "    status: {EQ: COMPLETED}" +
            "    AND: [" +
            "      {" +
            "        variables: {" +
            "          name: {EQ: \"variable1\"}" +
            "          value: {EQ: \"data\"} }" +
            "      }" +
            "      {" +
            "        variables: {" +
            "          name: {EQ: \"variable2\"}" +
            "          value: {EQ: true} }" +
            "      }" +
            "    ]" +
            "  }) {" +
            "    select {" +
            "      id" +
            "      status" +
            "      variables {" +
            "        name" +
            "        value" +
            "      }" +
            "    }" +
            "  }" +
            "}";

        String expected =
            "{Tasks={select=[" +
            "{id=1, status=COMPLETED, variables=[" +
            "{name=variable2, value=true}, " +
            "{name=variable1, value=data}]}" +
            "]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryTasksVariablesWhereWithEXISTSByNameAndValueCriteria() {
        //given
        String query =
            "query {" +
            "  Tasks(where: {" +
            "    status: {EQ: COMPLETED}" +
            "    EXISTS: [{" +
            "      variables: {" +
            "        name: {EQ: \"variable1\"}" +
            "        value: {EQ: \"data\"}" +
            "      }" +
            "    } {" +
            "      variables: {" +
            "        name: {EQ: \"variable2\"}" +
            "        value: {EQ: true}" +
            "      }" +
            "    }]" +
            "  }) {" +
            "    select {" +
            "      id" +
            "      status" +
            "      variables {" +
            "        name" +
            "        value" +
            "      }" +
            "    }" +
            "  }" +
            "}";

        String expected =
            "{Tasks={select=[" +
            "{id=1, status=COMPLETED, variables=[" +
            "{name=variable2, value=true}, " +
            "{name=variable1, value=data}]}" +
            "]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void testGraphqlTasksQueryWithEQNullValues() {
        // @formatter:off
        String query =
                "query {" +
                        "  Tasks(" +
                        "    page: { start: 1, limit: 100 }" +
                        "    where: { status: { IN: [CREATED, ASSIGNED] }, dueDate: { EQ: null } }" +
                        "  ) {" +
                        "    select {" +
                        "      id" +
                        "      businessKey" +
                        "      name" +
                        "      status" +
                        "      priority(orderBy: DESC)" +
                        "      dueDate(orderBy: ASC)" +
                        "      assignee" +
                        "    }" +
                        "  }" +
                        "}";
        // @formatter:on

        Object result = executor.execute(query).getData();

        String expected =
            "{Tasks={select=[" +
            "{id=2, businessKey=null, name=task2, status=CREATED, priority=10, dueDate=null, assignee=assignee}, " +
            "{id=4, businessKey=null, name=task4, status=CREATED, priority=10, dueDate=null, assignee=assignee}, " +
            "{id=6, businessKey=bk6, name=task6, status=ASSIGNED, priority=10, dueDate=null, assignee=assignee}, " +
            "{id=3, businessKey=null, name=task3, status=CREATED, priority=5, dueDate=null, assignee=assignee}" +
            "]}}";

        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void testGraphqlTasksQueryWithNENullValues() {
        // @formatter:off
        String query =
                "query {" +
                        "  Tasks(" +
                        "    page: { start: 1, limit: 100 }" +
                        "    where: { status: { IN: [ASSIGNED, COMPLETED] }, businessKey: { NE: null } }" +
                        "  ) {" +
                        "    select {" +
                        "      id" +
                        "      businessKey" +
                        "      name" +
                        "      status" +
                        "      priority(orderBy: DESC)" +
                        "      dueDate(orderBy: ASC)" +
                        "      assignee" +
                        "    }" +
                        "  }" +
                        "}";
        // @formatter:on

        Object result = executor.execute(query).getData();

        String expected =
            "{Tasks={select=[" +
            "{id=6, businessKey=bk6, name=task6, status=ASSIGNED, priority=10, dueDate=null, assignee=assignee}, " +
            "{id=1, businessKey=bk1, name=task1, status=COMPLETED, priority=5, dueDate=null, assignee=assignee}" +
            "]}}";

        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void testGraphqlTasksQueryWithEmbeddedVariablesWhereCriteria() throws InterruptedException {
        // @formatter:off
        String query =
            "query getTasks {" +
                "  Tasks(where: {businessKey: {EQ: \"bk1\"}}) {" +
                "    select {" +
                "      businessKey" +
                "      variables(where: {name: {EQ: \"variable1\"}}) {" +
                "        name" +
                "        value" +
                "      }" +
                "    }" +
                "  }" +
                "}";
        // @formatter:on

        Object result = executor.execute(query).getData();

        String expected = "{Tasks={select=[{businessKey=bk1, variables=[{name=variable1, value=data}]}]}}";

        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    void testGraphqlTasksQueryWithAliases() {
        // @formatter:off
        String query ="""
            query {
              page1: Tasks(page: {start: 1, limit: 2}) {
                select {
                  id
                  name
                }
              }
              page2: Tasks(page: {start: 3, limit: 2}) {
                select {
                  id
                  name
                }
              }
            }
            """;
        // @formatter:on

        Object result = executor.execute(query).getData();

        String expected =
            "{page1={select=[{id=1, name=task1}, {id=2, name=task2}]}, page2={select=[{id=5, name=task5}, {id=6, name=task6}]}}";

        assertThat(result.toString()).isEqualTo(expected);
    }
}
