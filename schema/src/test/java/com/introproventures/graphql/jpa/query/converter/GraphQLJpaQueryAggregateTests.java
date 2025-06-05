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

import static graphql.schema.GraphQLScalarType.newScalar;
import static org.assertj.core.api.Assertions.assertThat;

import com.introproventures.graphql.jpa.query.AbstractSpringBootTestSupport;
import com.introproventures.graphql.jpa.query.converter.model.TaskEntity;
import com.introproventures.graphql.jpa.query.converter.model.TaskVariableEntity;
import com.introproventures.graphql.jpa.query.converter.model.VariableValue;
import com.introproventures.graphql.jpa.query.schema.GraphQLExecutor;
import com.introproventures.graphql.jpa.query.schema.GraphQLSchemaBuilder;
import com.introproventures.graphql.jpa.query.schema.JavaScalars;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaExecutor;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;
import graphql.ExecutionResult;
import graphql.GraphQLError;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;

@SpringBootTest(
    properties = {
        "spring.sql.init.data-locations=GraphQLJpaAggregateTests.sql",
        "spring.datasource.url=jdbc:h2:mem:db;NON_KEYWORDS=VALUE;INIT=RUNSCRIPT FROM 'classpath:h2-init.sql'",
    }
)
public class GraphQLJpaQueryAggregateTests extends AbstractSpringBootTestSupport {

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
                .description("Custom Attribute Converter Schema")
                .enableAggregate(true)
                .scalar(
                    VariableValue.class,
                    newScalar()
                        .name("VariableValue")
                        .coercing(
                            new JavaScalars.GraphQLObjectCoercing() {
                                public Object serialize(final Object input) {
                                    return Optional
                                        .ofNullable(input)
                                        .filter(VariableValue.class::isInstance)
                                        .map(VariableValue.class::cast)
                                        .map(it -> Optional.ofNullable(it.getValue()).orElse(Optional.empty()))
                                        .orElseGet(() -> super.serialize(input));
                                }
                            }
                        )
                        .build()
                );
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
    public void criteriaTesterAggregateCountByName() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object> query = cb.createQuery(Object.class);
        Root<TaskVariableEntity> taskVariable = query.from(TaskVariableEntity.class);
        var taskJoin = taskVariable.join("task");

        Selection<?>[] selections = List.of(taskVariable.get("name"), cb.count(taskJoin)).toArray(Selection[]::new);
        Expression<?>[] groupings = List.of(taskVariable.get("name")).toArray(Expression[]::new);

        query.multiselect(selections).groupBy(groupings);

        // when:
        List<Object> result = entityManager.createQuery(query).getResultList();

        // then:
        assertThat(result)
            .isNotEmpty()
            .hasSize(7)
            .contains(
                new Object[] { "variable1", 1L },
                new Object[] { "variable2", 1L },
                new Object[] { "variable3", 1L },
                new Object[] { "variable4", 1L },
                new Object[] { "variable5", 2L },
                new Object[] { "variable6", 1L },
                new Object[] { "variable7", 1L }
            );
    }

    @Test
    @Transactional
    public void criteriaTesterAggregateCountByNameMapProjection() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Map> query = cb.createQuery(Map.class);
        Root<TaskVariableEntity> taskVariable = query.from(TaskVariableEntity.class);
        var taskJoin = taskVariable.join("task");

        Selection<?>[] selections = List
            .of(taskVariable.get("name").alias("by"), cb.count(taskJoin).alias("count"))
            .toArray(Selection[]::new);
        Expression<?>[] groupings = List.of(taskVariable.get("name")).toArray(Expression[]::new);

        query.multiselect(selections).groupBy(groupings);

        // when:
        List<Map> result = entityManager.createQuery(query).getResultList();

        // then:
        assertThat(result)
            .isNotEmpty()
            .hasSize(7)
            .contains(
                Map.of("by", "variable1", "count", 1L),
                Map.of("by", "variable2", "count", 1L),
                Map.of("by", "variable3", "count", 1L),
                Map.of("by", "variable4", "count", 1L),
                Map.of("by", "variable5", "count", 2L),
                Map.of("by", "variable6", "count", 1L),
                Map.of("by", "variable7", "count", 1L)
            );
    }

    @Test
    @Transactional
    public void criteriaTesterTaskAggregateCount() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object> query = cb.createQuery(Object.class);
        Root<TaskEntity> tasks = query.from(TaskEntity.class);
        //var variablesJoin = tasks.join("variables");

        Selection<?>[] selections = List.of(cb.count(tasks)).toArray(Selection[]::new);
        Expression<?>[] groupings = List.of().toArray(Expression[]::new);

        query.multiselect(selections).groupBy(groupings);

        // when:
        List<Object> result = entityManager.createQuery(query).getResultList();

        // then:
        assertThat(result).isNotEmpty().hasSize(1).contains(6L);
    }

    @Test
    @Transactional
    public void criteriaTesterTaskVariablesAggregateCount() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object> query = cb.createQuery(Object.class);
        Root<TaskEntity> tasks = query.from(TaskEntity.class);
        var variablesJoin = tasks.join("variables");

        Selection<?>[] selections = List.of(cb.count(variablesJoin)).toArray(Selection[]::new);
        Expression<?>[] groupings = List.of().toArray(Expression[]::new);

        query.distinct(true).multiselect(selections).groupBy(groupings);

        // when:
        List<Object> result = entityManager.createQuery(query).getResultList();

        // then:
        assertThat(result).isNotEmpty().hasSize(1).contains(8L);
    }

    @Test
    @Transactional
    public void criteriaTesterTaskVariablesAggregateCountByName() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object> query = cb.createQuery(Object.class);
        Root<TaskEntity> tasks = query.from(TaskEntity.class);
        var variablesJoin = tasks.join("variables");

        Selection<?>[] selections = List
            .of(variablesJoin.get("name"), cb.count(variablesJoin))
            .toArray(Selection[]::new);
        Expression<?>[] groupings = List.of(variablesJoin.get("name")).toArray(Expression[]::new);

        query.distinct(true).multiselect(selections).groupBy(groupings);

        // when:
        List<Object> result = entityManager.createQuery(query).getResultList();

        // then:
        assertThat(result)
            .isNotEmpty()
            .hasSize(7)
            .contains(
                new Object[] { "variable1", 1L },
                new Object[] { "variable2", 1L },
                new Object[] { "variable3", 1L },
                new Object[] { "variable4", 1L },
                new Object[] { "variable5", 2L },
                new Object[] { "variable6", 1L },
                new Object[] { "variable7", 1L }
            );
    }

    @Test
    public void queryTasksVariablesWhereWithExplicitANDByMultipleNameAndValueCriteria2() {
        //given
        String query =
            "query {" +
            "  Tasks(where: {" +
            "    status: {EQ: COMPLETED}" +
            "    AND: [" +
            "      {  " +
            "         variables: {" +
            "           name: {EQ: \"variable1\"}" +
            "        value: {EQ: \"data\"}" +
            "        }" +
            "      }" +
            "    ]" +
            "  }) {" +
            "    select {" +
            "      id" +
            "      status" +
            "      variables(where: {name: {IN: [\"variable2\",\"variable1\"]}} ) {" +
            "        name" +
            "        value" +
            "      }" +
            "    }" +
            "  }" +
            "}";

        String expected =
            "{Tasks={select=[" +
            "{id=1, status=COMPLETED, variables=[" +
            "{name=variable1, value=data}, " +
            "{name=variable2, value=true}]}" +
            "]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryTasksVariablesAggregateCount() {
        //given
        String query =
            """
                query {
                  Tasks(
                    where: {
                      status: { EQ: COMPLETED }
                    }
                  ) {
                    select {
                      id
                      status
                    }
                    aggregate {
                      count
                    }
                  }
                }
            """;

        String expected = "{Tasks={select=[{id=1, status=COMPLETED}, {id=5, status=COMPLETED}], aggregate={count=2}}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryTasksVariablesNestedAggregateCount() {
        //given
        String query =
            """
                query {
                  Tasks(
                    where: {
                      status: { EQ: COMPLETED }
                    }
                  ) {
                    select {
                      id
                      status
                    }
                    aggregate {
                      count
                      variables: count(of: variables)
                    }
                  }
                }
            """;

        String expected =
            "{Tasks={select=[{id=1, status=COMPLETED}, {id=5, status=COMPLETED}], aggregate={count=2, variables=2}}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryVariablesTaskNestedAggregateCount() {
        //given
        String query =
            """
                query {
                  TaskVariables {
                    aggregate {
                      count
                      tasks: count(of: task)
                    }
                  }
                }
            """;

        String expected = "{TaskVariables={aggregate={count=8, tasks=8}}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryVariablesTaskNestedAggregateCountWhere() {
        //given
        String query =
            """
                query {
                  TaskVariables(where:{task: {status: {EQ: COMPLETED}}}) {
                    aggregate {
                      count
                      tasks: count(of: task)
                    }
                  }
                }
            """;

        String expected = "{TaskVariables={aggregate={count=2, tasks=2}}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryVariablesTaskNestedAggregateCountGroupBy() {
        //given
        String query =
            """
            query {
              TaskVariables {
                aggregate {
                  # Aggregate by group of fields
                  group {
                    by(field: name)
                    count
                  }
                }
              }
            }
            """;

        String expected =
            "{TaskVariables={aggregate={group=[{by=variable1, count=1}, {by=variable2, count=1}, {by=variable3, count=1}, {by=variable4, count=1}, {by=variable5, count=2}, {by=variable6, count=1}, {by=variable7, count=1}]}}}";

        //when
        ExecutionResult result = executor.execute(query);

        // then
        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getData().toString()).isEqualTo(expected);
    }

    @Test
    public void queryVariablesTaskNestedAggregateCountGroupByMultipleFields() {
        //given
        String query =
            """
            query {
              TaskVariables {
                aggregate {
                  # Aggregate by group of fields
                  group {
                    name: by(field: name)
                    value: by(field: value)
                    count
                  }
                }
              }
            }
            """;

        String expected =
            "{TaskVariables={aggregate={group=[{name=variable1, value=data, count=1}, {name=variable2, value=true, count=1}, {name=variable3, value=null, count=1}, {name=variable4, value={key=data}, count=1}, {name=variable5, value=1.2345, count=2}, {name=variable6, value=12345, count=1}, {name=variable7, value=[1, 2, 3, 4, 5], count=1}]}}}";

        //when
        ExecutionResult result = executor.execute(query);

        // then
        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getData().toString()).isEqualTo(expected);
    }

    @Test
    public void queryVariablesTaskNestedAggregateCountGroupByEmptyFields() {
        //given
        String query =
            """
            query {
              TaskVariables {
                aggregate {
                  # Aggregate by group of fields
                  group {
                    count
                  }
                }
              }
            }
            """;

        String expected =
            "{TaskVariables={aggregate={group=[{name=variable1, value=data, count=1}, {name=variable2, value=true, count=1}, {name=variable3, value=null, count=1}, {name=variable4, value={key=data}, count=1}, {name=variable5, value=1.2345, count=2}, {name=variable6, value=12345, count=1}, {name=variable7, value=[1, 2, 3, 4, 5], count=1}]}}}";

        //when
        ExecutionResult result = executor.execute(query);

        // then
        assertThat(result.getErrors())
            .isNotEmpty()
            .extracting(GraphQLError::getMessage)
            .anyMatch(message -> message.contains("At least one field is required for aggregate group"));
    }

    @Test
    public void queryVariablesTaskNestedAggregateCountGroupByMissingCount() {
        //given
        String query =
            """
            query {
              TaskVariables {
                aggregate {
                  # Aggregate by group of fields
                  group {
                    by(field: name)
                  }
                }
              }
            }
            """;

        //when
        ExecutionResult result = executor.execute(query);

        // then
        assertThat(result.getErrors())
            .isNotEmpty()
            .extracting(GraphQLError::getMessage)
            .anyMatch(message -> message.contains("Missing aggregate count for group"));
    }

    @Test
    public void queryVariablesTaskNestedAggregateCountByMultipleFields() {
        //given
        String query =
            """
            query {
              TaskVariables(
                # Apply filter criteria
                where: {name: {IN: ["variable1", "variable5"]}}
              ) {
                aggregate {
                  # count by variables
                  variables: count
                  # Count by associated tasks
                  tasks: count(of: task)
                }
              }
            }
            """;

        String expected = "{TaskVariables={aggregate={variables=3, tasks=3}}}";

        //when
        ExecutionResult result = executor.execute(query);

        // then
        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getData().toString()).isEqualTo(expected);
    }

    @Test
    public void queryVariablesTaskNestedAggregateCountByMultipleEntities() {
        //given
        String query =
            """
            query {
                TaskVariables
                  # Apply filter criteria
                  (where: {name: {IN: ["variable1", "variable5"]}})
                {
                  aggregate {
                    # count by variables
                    totalVariables: count
                    # Count by associated tasks
                    totalTasks: count(of: task)
                    # Group by task variable entity fields
                    groupByNameValue: group {
                      # Use aliases to group by multiple fields
                      name: by(field: name)
                      value: by(field: value)
                      # Count aggregate
                      count
                    }
                    # Group by associated tasks
                    groupTasksByVariableName: group {
                      variable: by(field: name)
                      count(of: task)
                    }
                  }
                }
                Tasks {
                  aggregate {
                    totalTasks: count
                    totalVariables: count(of: variables)
                    groupByStatus: group {
                      status: by(field: status)
                      count
                    }
                  }
                }
            }
            """;

        String expected =
            "{TaskVariables={aggregate={totalVariables=3, totalTasks=3, groupByNameValue=[{name=variable1, value=data, count=1}, {name=variable5, value=1.2345, count=2}], groupTasksByVariableName=[{variable=variable1, count=1}, {variable=variable5, count=2}]}}, Tasks={aggregate={totalTasks=6, totalVariables=8, groupByStatus=[{status=ASSIGNED, count=1}, {status=COMPLETED, count=2}, {status=CREATED, count=3}]}}}";

        //when
        ExecutionResult result = executor.execute(query);

        // then
        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getData().toString()).isEqualTo(expected);
    }

    @Test
    public void queryVariablesTaskNestedAggregateCountByNestedAssociation() {
        //given
        String query =
            """
                query {
                  TaskVariables(
                    # Apply filter criteria
                    where: {name: {IN: ["variable1", "variable5"]}}
                  ) {
                    aggregate {
                      # count by variables
                      variables: count
                      # Count by associated tasks
                      by {
                        tasks: task {
                          by(field: status)
                          count
                        }
                      }
                    }
                  }
                }
            """;

        String expected =
            "{TaskVariables={aggregate={variables=3, by={tasks=[{by=COMPLETED, count=1}, {by=CREATED, count=2}]}}}}";

        //when
        ExecutionResult result = executor.execute(query);

        // then
        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getData().toString()).isEqualTo(expected);
    }

    @Test
    public void queryVariablesTaskNestedAggregateCountByNestedAssociationAlias() {
        //given
        String query =
            """
                query {
                  TaskVariables(
                    # Apply filter criteria
                    where: {name: {IN: ["variable1", "variable5"]}}
                  ) {
                    aggregate {
                      # count by variables
                      variables: count
                      # Count by associated tasks
                      by {
                        tasks: task {
                          status: by(field: status)
                          count
                        }
                      }
                    }
                  }
                }
            """;

        String expected =
            "{TaskVariables={aggregate={variables=3, by={tasks=[{status=COMPLETED, count=1}, {status=CREATED, count=2}]}}}}";

        //when
        ExecutionResult result = executor.execute(query);

        // then
        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getData().toString()).isEqualTo(expected);
    }

    @Test
    public void queryVariablesTaskNestedAggregateCountByNestedAssociationMultipleAliases() {
        //given
        String query =
            """
                query {
                  TaskVariables(
                    # Apply filter criteria
                    where: {name: {IN: ["variable1", "variable5"]}}
                  ) {
                    aggregate {
                      # count by variables
                      variables: count
                      # Count by associated tasks
                      groupByVariableName: group {
                        name: by(field: name)
                        count
                      }
                      by {
                        groupByTaskStatus: task {
                          status: by(field: status)
                          count
                        }
                        # Count by associated tasks
                        groupByTaskAssignee: task {
                          assignee: by(field: assignee)
                          count
                        }
                      }
                    }
                  }
                }
            """;

        String expected =
            "{TaskVariables={aggregate={variables=3, groupByVariableName=[{name=variable1, count=1}, {name=variable5, count=2}], by={groupByTaskStatus=[{status=COMPLETED, count=1}, {status=CREATED, count=2}], groupByTaskAssignee=[{assignee=assignee, count=3}]}}}}";

        //when
        ExecutionResult result = executor.execute(query);

        // then
        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getData().toString()).isEqualTo(expected);
    }
}
