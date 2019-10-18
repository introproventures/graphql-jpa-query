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

import java.util.Collections;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.transaction.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.introproventures.graphql.jpa.query.converter.model.JsonEntity;
import com.introproventures.graphql.jpa.query.converter.model.TaskEntity;
import com.introproventures.graphql.jpa.query.converter.model.TaskVariableEntity;
import com.introproventures.graphql.jpa.query.converter.model.VariableValue;

import org.junit.Test;


public class GraphQLJpaConverterTests extends BaseConverterTest {

    @Test
    @Transactional
    public void queryTester() {
        // given:
        Query query = entityManager.createQuery("select json from JsonEntity json where json.attributes LIKE '%key%'");

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

        JsonNode value = new ObjectMapper().valueToTree(Collections.singletonMap("attr",
                                                                                 new String[] {"1","2","3","4","5"}));
        criteria.select(json)
                .where(builder.equal(json.get("attributes"), value));
        
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

        VariableValue<Boolean> variableValue = new VariableValue<>(Boolean.TRUE);
        criteria.select(taskVariable)
                .where(cb.and(cb.equal(cb.lower(taskVariable.get("name")), "variable2")),
                              cb.in(taskVariable.get("value")).value(variableValue));

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
        
        Subquery<TaskVariableEntity> subQuery = tasksQuery.subquery(TaskVariableEntity.class);
        Root<TaskVariableEntity> taskVariables = subQuery.from(TaskVariableEntity.class);
        
        Predicate isOwner = cb.in(taskVariables.get("task")).value(task);

        Predicate var1 = cb.and(cb.equal(taskVariables.get("name"), "variable2"), 
                                cb.equal(taskVariables.get("value"), new VariableValue<>(Boolean.TRUE)));

        Predicate var2 = cb.and(cb.equal(taskVariables.get("name"), "variable1"), 
                                cb.equal(taskVariables.get("value"), new VariableValue<>("data")));
        
        tasksQuery.select(task)
                  .where(cb.and(cb.exists(subQuery.select(taskVariables).where(cb.and(isOwner, var1))),
                                cb.exists(subQuery.select(taskVariables).where(cb.and(isOwner, var2)))));
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
        
        Subquery<TaskVariableEntity> subQuery = tasksQuery.subquery(TaskVariableEntity.class);
        Root<TaskEntity> taskCorrelation = subQuery.correlate(task);
        
        Join<TaskEntity, TaskVariableEntity> taskVariables = taskCorrelation.join("variables");
        
        Predicate var1 = cb.and(cb.equal(taskVariables.get("name"), "variable2"), 
                                cb.equal(taskVariables.get("value"), new VariableValue<>(Boolean.TRUE)));

        Predicate var2 = cb.and(cb.equal(taskVariables.get("name"), "variable1"), 
                                cb.equal(taskVariables.get("value"), new VariableValue<>("data")));
        
        tasksQuery.select(task)
                  .where(cb.and(cb.exists(subQuery.select(taskVariables).where(var1)),
                                cb.exists(subQuery.select(taskVariables).where(var2))));
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
        
        Subquery<TaskVariableEntity> subQuery = tasksQuery.subquery(TaskVariableEntity.class);
        Root<TaskEntity> taskCorrelation = subQuery.correlate(task);
        
        Join<TaskEntity, TaskVariableEntity> taskVariables = taskCorrelation.join("variables");
        
        Predicate var1 = cb.and(cb.equal(taskVariables.get("name"), "variable2"), 
                                cb.equal(taskVariables.get("value"), new VariableValue<>(Boolean.TRUE)));

        Predicate var2 = cb.and(cb.equal(taskVariables.get("name"), "variable1"), 
                                cb.equal(taskVariables.get("value"), new VariableValue<>("data")));
        
        tasksQuery.select(task)
                  .where(cb.and(cb.not(cb.exists(subQuery.select(taskVariables).where(var1))),
                                cb.not(cb.exists(subQuery.select(taskVariables).where(var2)))
                                )
                         );
        // when:
        List<TaskEntity> result = entityManager.createQuery(tasksQuery).getResultList();

        // then:
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(4);
    }    
    
    @Test
    @Transactional
    public void criteriaTester6() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<TaskEntity> tasksQuery = cb.createQuery(TaskEntity.class);
        Root<TaskEntity> task = tasksQuery.from(TaskEntity.class);
        
        Subquery<TaskVariableEntity> subQuery = tasksQuery.subquery(TaskVariableEntity.class);
        Root<TaskEntity> taskCorrelation = subQuery.correlate(task);
        
        Join<TaskEntity, TaskVariableEntity> taskVariables = taskCorrelation.join("variables");
        
        Predicate var1 = cb.and(cb.equal(taskVariables.get("name"), "variable2"), 
                                cb.equal(taskVariables.get("value"), new VariableValue<>(Boolean.TRUE)));

        Predicate var2 = cb.and(cb.equal(taskVariables.get("name"), "variable1"), 
                                cb.equal(taskVariables.get("value"), new VariableValue<>("data")));
        
        tasksQuery.select(task)
                  .where(cb.not(cb.or(cb.exists(subQuery.select(taskVariables).where(var1)),
                                      cb.exists(subQuery.select(taskVariables).where(var2)))
                                )
                         );
        // when:
        List<TaskEntity> result = entityManager.createQuery(tasksQuery).getResultList();

        // then:
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(4);
    }       
    
    @Test // Problem with generating cast() in the where expression
    @Transactional
    public void criteriaTesterLike() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<JsonEntity> criteria = builder.createQuery(JsonEntity.class);
        Root<JsonEntity> json = criteria.from(JsonEntity.class);

        criteria.select(json)
                .where(builder.like(json.get("attributes").as(String.class), "%key%"));
        
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

        criteria.select(json)
                .where(builder.gt(builder.locate(json.get("attributes"),"key"), 0));
        
        // when:
        List<?> result = entityManager.createQuery(criteria).getResultList();

        // then: 
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
    }
}