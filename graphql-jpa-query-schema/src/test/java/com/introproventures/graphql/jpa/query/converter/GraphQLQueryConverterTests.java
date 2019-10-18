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
import java.util.Map;

import org.junit.Test;


public class GraphQLQueryConverterTests extends BaseConverterTest {

    @Test
    public void queryJsonEntity() {
        //given
        String query = "query {" + 
                "  JsonEntities {" + 
                "    select {" + 
                "      id" + 
                "      firstName" + 
                "      lastName" + 
                "      attributes" + 
                "    }" + 
                "  }" + 
                "}";
        
        String expected = "{JsonEntities={select=["
                + "{id=1, firstName=john, lastName=doe, attributes={\"attr\":{\"key\":[\"1\",\"2\",\"3\",\"4\",\"5\"]}}}, "
                + "{id=2, firstName=joe, lastName=smith, attributes={\"attr\":[\"1\",\"2\",\"3\",\"4\",\"5\"]}}"
                + "]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }     
    
    @Test
    public void queryJsonEntityWhereSearchCriteria() {
        //given
        String query = "query {" + 
                "  JsonEntities(where: {" 
                +     "attributes: {LOCATE: \"key\"}" 
                + "}) {" + 
                "    select {" + 
                "      id" + 
                "      firstName" + 
                "      lastName" + 
                "      attributes" + 
                "    }" + 
                "  }" + 
                "}";
        
        String expected = "{JsonEntities={select=["
                + "{id=1, firstName=john, lastName=doe, attributes={\"attr\":{\"key\":[\"1\",\"2\",\"3\",\"4\",\"5\"]}}}"
                + "]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }
    
    @Test
    public void queryTaskVariablesWhereSearchCriteria() {
        //given
        String query = "query {" + 
                "  TaskVariables(where: {" 
                +     "value: {LOCATE: \"true\"}" 
                + "}) {" + 
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
        String query = "query($value: VariableValue!) {" + 
                "  TaskVariables(where: {" 
                +     "value: {LOCATE: $value }" 
                + "}) {" + 
                "    select {" + 
                "      id" + 
                "      name" + 
                "      value" + 
                "    }" + 
                "  }" + 
                "}";
        
        Map<String,Object> variables = Collections.singletonMap("value", true);
        
        String expected = "{TaskVariables={select=[{id=2, name=variable2, value=true}]}}";

        //when
        Object result = executor.execute(query, variables).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }
    

    @Test
    public void queryProcessVariablesWhereSearchCriteriaVariableBindings() {
        //given
        String query = "query($value: VariableValue!)  {" + 
                " ProcessVariables(where: {" 
                +     "value: {LOCATE: $value}" 
                + "}) {" + 
                "    select {" + 
                "      id" + 
                "      name" + 
                "      value" + 
                "    }" + 
                "  }" + 
                "}";

        Map<String,Object> variables = Collections.singletonMap("value", "[\"1\",\"2\",\"3\",\"4\",\"5\"]");
        
        String expected = "{ProcessVariables={select=[{id=1, name=document, value={key=[1, 2, 3, 4, 5]}}]}}";

        //when
        Object result = executor.execute(query, variables).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryProcessVariablesWhereSearchCriteria() {
        //given
        String query = "query {" + 
                " ProcessVariables(where: {" 
                +     "value: {LOCATE: \"[\\\"1\\\",\\\"2\\\",\\\"3\\\",\\\"4\\\",\\\"5\\\"]\"}" 
                + "}) {" + 
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
        String query = "query {" + 
                " TaskVariables(where: {" 
                +     "name: {EQ: \"variable1\"}" 
                +     "value: {EQ: \"data\"}" 
                + "}) {" + 
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
        String query = "query {" + 
                " TaskVariables(where: {" 
                +     "name: {EQ: \"variable2\"}" 
                +     "value: {EQ: true}" 
                + "}) {" + 
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
        String query = "query {" + 
                " TaskVariables(where: {" 
                +     "name: {EQ: \"variable3\"}"
                +     "value: {EQ: null}"
                + "}) {" + 
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
        String query = "query {" + 
                " TaskVariables(where: {" 
                +     "name: {EQ: \"variable6\"}" 
                +     "value: {EQ: 12345}" 
                + "}) {" + 
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
        String query = "query {" + 
                " TaskVariables(where: {" 
                +     "name: {EQ: \"variable5\"}" 
                +     "value: {EQ: 1.2345}" 
                + "}) {" + 
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
        String query = "query {" + 
                " TaskVariables(where: {" 
                +     "value: {IN: 1.2345}" 
                + "}) {" + 
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
        String query = "query {" + 
                " TaskVariables(where: {" 
                +     "value: {IN: [1.2345]}" 
                + "}) {" + 
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
        String query = "query {" + 
                " TaskVariables(where: {" 
                +     "value: {NIN: [null]}" 
                + "}) {" + 
                "    select {" + 
                "      name" + 
                "      value" + 
                "    }" + 
                "  }" + 
                "}";
        
        String expected = "{TaskVariables={select=["
                + "{name=variable1, value=data}, "
                + "{name=variable2, value=true}, "
                + "{name=variable4, value={key=data}}, "
                + "{name=variable5, value=1.2345}, "
                + "{name=variable6, value=12345}, "
                + "{name=variable7, value=[1, 2, 3, 4, 5]}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }
    
    @Test
    public void queryProcessVariablesWhereWithNINSingleValueSearchCriteria() {
        //given
        String query = "query {" + 
                " TaskVariables(where: {" 
                +     "value: {NIN: null}" 
                + "}) {" + 
                "    select {" + 
                "      name" + 
                "      value" + 
                "    }" + 
                "  }" + 
                "}";
        
        String expected = "{TaskVariables={select=["
                + "{name=variable1, value=data}, "
                + "{name=variable2, value=true}, "
                + "{name=variable4, value={key=data}}, "
                + "{name=variable5, value=1.2345}, "
                + "{name=variable6, value=12345}, "
                + "{name=variable7, value=[1, 2, 3, 4, 5]}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }
    
    @Test
    public void queryProcessVariablesWhereWithNEValueSearchCriteria() {
        //given
        String query = "query {" + 
                " TaskVariables(where: {" 
                +     "value: {NE: null}" 
                + "}) {" + 
                "    select {" + 
                "      name" + 
                "      value" + 
                "    }" + 
                "  }" + 
                "}";
        
        String expected = "{TaskVariables={select=["
                + "{name=variable1, value=data}, "
                + "{name=variable2, value=true}, "
                + "{name=variable4, value={key=data}}, "
                + "{name=variable5, value=1.2345}, "
                + "{name=variable6, value=12345}, "
                + "{name=variable7, value=[1, 2, 3, 4, 5]}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }        
     
    @Test
    public void queryProcessVariablesWhereWithINListTypedValueSearchCriteria() {
        //given
        String query = "query {" + 
                " TaskVariables(where: {" 
                +     "value: {IN: [null, true, \"data\", 12345, 1.2345]}" 
                + "}) {" + 
                "    select {" + 
                "      name" + 
                "      value" + 
                "    }" + 
                "  }" + 
                "}";
        
        String expected = "{TaskVariables={select=["
                + "{name=variable1, value=data}, "
                + "{name=variable2, value=true}, "
                + "{name=variable3, value=null}, "
                + "{name=variable5, value=1.2345}, "
                + "{name=variable6, value=12345}"
                + "]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }     
    
    
    @Test
    public void queryTasksVariablesWhereWithExplicitANDEXISTSByNameAndValueCriteria() {
        //given
        String query = "query {" + 
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
        
        String expected = "{Tasks={select=["
                + "{id=1, status=COMPLETED, variables=["
                +   "{name=variable2, value=true}, "
                +   "{name=variable1, value=data}]}"
                + "]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }       


    @Test
    public void queryTasksVariablesWhereWithEXISTSByNameAndValueCriteria() {
        //given
        String query = "query {" + 
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
        
        String expected = "{Tasks={select=["
                + "{id=1, status=COMPLETED, variables=["
                +   "{name=variable2, value=true}, "
                +   "{name=variable1, value=data}]}"
                + "]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }       


}