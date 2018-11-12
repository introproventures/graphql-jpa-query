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

package com.introproventures.graphql.jpa.query.schema;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaExecutor;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource({"classpath:hibernate.properties"})
public class StarwarsQueryExecutorTests {

    @SpringBootApplication
    static class Application {
        @Bean
        public GraphQLExecutor graphQLExecutor(final GraphQLSchemaBuilder graphQLSchemaBuilder) {
            return new GraphQLJpaExecutor(graphQLSchemaBuilder.build());
        }

        @Bean
        public GraphQLSchemaBuilder graphQLSchemaBuilder(final EntityManager entityManager) {

            return new GraphQLJpaSchemaBuilder(entityManager)
                .name("Starwars")
                .description("Starwars JPA test schema");
        }

    }


    @Autowired
    private GraphQLJpaExecutor executor;

    @Autowired
    private EntityManager em;

    @Test
    public void contextLoads() {
    }

    @Test
    @Transactional
    public void JPASampleTester() {
        // given:
        Query query = em.createQuery("select h, h.friends from Human h");

        // when:
        List<?> result = query.getResultList();

        // then:
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(13);
    }

    @Test
    public void getsNamesOfAllDroids() {
        //given:
        String query = "query HeroNameQuery { Droids { select {name } } }";

        String expected = "{Droids={select=[{name=C-3PO}, {name=R2-D2}]}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryForDroidByName() {
        //given:
        String query = "query { Droids( where: { name: { EQ: \"C-3PO\"}}) { select {name, primaryFunction } } }";

        String expected = "{Droids={select=[{name=C-3PO, primaryFunction=Protocol}]}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryManyToOneJoinById() {
        //given:
        String query = "query { Humans(where: {id: {EQ: \"1000\"}}) { select { name, homePlanet, favoriteDroid { name } } } }";


        String expected = "{Humans={select=[{name=Luke Skywalker, homePlanet=Tatooine, favoriteDroid={name=C-3PO}}]}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }

    @SuppressWarnings("serial")
	@Test
    public void queryManyToOneJoinByIdWithVariables() {
        //given:
        String query = "query($id: String!) { Humans { select { name, homePlanet, favoriteDroid(where: {id: {EQ: $id}}) { name} } } }";
        Map<String, Object> variables = new HashMap<String, Object>() {{
            put("id", "2001");
        }};

        String expected = "{Humans={select=[{name=Darth Vader, homePlanet=Tatooine, favoriteDroid={name=R2-D2}}]}}";

        //when:
        Object result = executor.execute(query,variables).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryOneToManyJoinByID() {
        //given:
        String query = "query { Humans(where:{id:{EQ: \"1000\"}}) { select {name, homePlanet, friends { name } } }}";


        String expected = "{Humans={select=["
            + "{name=Luke Skywalker, homePlanet=Tatooine, friends=[{name=C-3PO}, {name=Leia Organa}, {name=R2-D2}, {name=Han Solo}]}"
            + "]}}";



        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryWithParameter() {
        //given:
        String query = "query humanQuery($id: String!) { Humans(where:{id: {EQ: $id}}) { select {name, homePlanet } }}";
        Map<String, Object> variables = new HashMap<String, Object>() {{
            put("id", "1001");
        }};

        String expected = "{Humans={select=[{name=Darth Vader, homePlanet=Tatooine}]}}";

        //when:
        Object result = executor.execute(query, variables).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryWithIdArgument() {
        //given:
        String query = "query humanByIdQuery($id: String!) { Human(id: $id) { name, homePlanet } }";
        Map<String, Object> variables = new HashMap<String, Object>() {{
            put("id", "1001");
        }};

        String expected = "{Human={name=Darth Vader, homePlanet=Tatooine}}";

        //when:
        Object result = executor.execute(query, variables).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryWithAlias() {
        //given:
        String query = "query { luke: Human(id: \"1000\") { name, homePlanet } leia: Human(id: \"1003\") { name } }";

        String expected = "{luke={name=Luke Skywalker, homePlanet=Tatooine}, leia={name=Leia Organa}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryAllowsUseFragmentToAvoidDuplicatingContent() {
        //given:
        String query = "query UseFragment { luke: Human(id: \"1000\") { ...HumanFragment } leia: Human(id: \"1003\") { ...HumanFragment } }"
                      +"fragment HumanFragment on Human { name, homePlanet }";

        String expected = "{luke={name=Luke Skywalker, homePlanet=Tatooine}, leia={name=Leia Organa, homePlanet=Alderaan}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryDeepNesting() {
        //given:
        String query = "query { Droid(id: \"2001\") { name, friends { name, appearsIn, friends { name } } } }";


        String expected = "{Droid={name=R2-D2, friends=["
            + "{name=Leia Organa, appearsIn=[A_NEW_HOPE, EMPIRE_STRIKES_BACK, RETURN_OF_THE_JEDI, THE_FORCE_AWAKENS], friends=[{name=C-3PO}, {name=R2-D2}, {name=Han Solo}, {name=Luke Skywalker}]}, "
            + "{name=Han Solo, appearsIn=[A_NEW_HOPE, EMPIRE_STRIKES_BACK, RETURN_OF_THE_JEDI, THE_FORCE_AWAKENS], friends=[{name=R2-D2}, {name=Leia Organa}, {name=Luke Skywalker}]}, "
            + "{name=Luke Skywalker, appearsIn=[A_NEW_HOPE, EMPIRE_STRIKES_BACK, RETURN_OF_THE_JEDI, THE_FORCE_AWAKENS], friends=[{name=C-3PO}, {name=R2-D2}, {name=Leia Organa}, {name=Han Solo}]}"
            + "]}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }

    // Cannot simultaneously fetch multiple bags #2
    @Test
    public void queryDeepNestingPlural() {
        //given:
        String query = "query { Droids(where: {id: {EQ: \"2001\"}}) { select { name, friends { name, appearsIn, friends { name } } }  }}";

        String expected = "{Droids={select=[{name=R2-D2, friends=["
            + "{name=Leia Organa, appearsIn=[A_NEW_HOPE, EMPIRE_STRIKES_BACK, RETURN_OF_THE_JEDI, THE_FORCE_AWAKENS], friends=[{name=C-3PO}, {name=R2-D2}, {name=Han Solo}, {name=Luke Skywalker}]}, "
            + "{name=Han Solo, appearsIn=[A_NEW_HOPE, EMPIRE_STRIKES_BACK, RETURN_OF_THE_JEDI, THE_FORCE_AWAKENS], friends=[{name=R2-D2}, {name=Leia Organa}, {name=Luke Skywalker}]}, "
            + "{name=Luke Skywalker, appearsIn=[A_NEW_HOPE, EMPIRE_STRIKES_BACK, RETURN_OF_THE_JEDI, THE_FORCE_AWAKENS], friends=[{name=C-3PO}, {name=R2-D2}, {name=Leia Organa}, {name=Han Solo}]}]}"
            + "]}}";
        
        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryNestedDatabaseEnum() {
        //given:
        String query = "query { Humans { select { name, gender { id, code, parent { id } } } } }";

        String expected = "{Humans={select=["
        		+ "{name=Luke Skywalker, gender={id=0, code=Male, parent=null}}, "
        		+ "{name=Darth Vader, gender={id=0, code=Male, parent=null}}, "
        		+ "{name=Han Solo, gender={id=0, code=Male, parent=null}}, "
        		+ "{name=Leia Organa, gender={id=1, code=Female, parent=null}}, "
        		+ "{name=Wilhuff Tarkin, gender={id=0, code=Male, parent=null}}"
        		+ "]}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryWhereRoot() {
        //given:
        String query = "query { Humans( page: { start: 1, limit: 2 }) { pages, total, select { name } } }";

        String expected = "{Humans={pages=3, total=5, select=[{name=Luke Skywalker}, {name=Darth Vader}]}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }
    
    @Test
    public void queryPaginationWithoutRecords() {
        //given:
        String query = "query { Humans ( page: { start: 1, limit: 2 }) { pages, total } }";

        String expected = "{Humans={pages=3, total=5}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryOrderByFields() {
        //given:
        String query = "query { Humans { select {name(orderBy: DESC) homePlanet } } }";

        String expected = "{Humans={select=["
            + "{name=Wilhuff Tarkin, homePlanet=null}, "
            + "{name=Luke Skywalker, homePlanet=Tatooine}, "
            + "{name=Leia Organa, homePlanet=Alderaan}, "
            + "{name=Han Solo, homePlanet=null}, "
            + "{name=Darth Vader, homePlanet=Tatooine}"
            + "]}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryOrderByFieldsNested() {
        //given:
        String query = "query { Humans(where: {id: {EQ: \"1000\"}}) { select {name(orderBy: DESC) homePlanet friends { name(orderBy:DESC) } } } }";

        String expected = "{Humans={select=["
            + "{name=Luke Skywalker, homePlanet=Tatooine, "
	            + "friends=["
		            + "{name=R2-D2}, "
		            + "{name=Leia Organa}, "
		            + "{name=Han Solo}, "
		            + "{name=C-3PO}"
		        + "]"
	        + "}"
            + "]}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryOrderByDefaultId() {
        //given:
        String query = "query { Humans { select { id } } }";

        String expected = "{Humans={select=["
	            + "{id=1000}, "
	            + "{id=1001}, "
	            + "{id=1002}, "
	            + "{id=1003}, "
	            + "{id=1004}"
            + "]}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }


    @Test
    public void queryByCollectionOfEnumsAtRootLevel() {
        //given:
        String query = "query { Humans ( where: { appearsIn: {IN: [THE_FORCE_AWAKENS]}}) { select { name appearsIn } } }";


        String expected = "{Humans={select=["
            + "{name=Luke Skywalker, appearsIn=[A_NEW_HOPE, EMPIRE_STRIKES_BACK, RETURN_OF_THE_JEDI, THE_FORCE_AWAKENS]}, "
            + "{name=Han Solo, appearsIn=[A_NEW_HOPE, EMPIRE_STRIKES_BACK, RETURN_OF_THE_JEDI, THE_FORCE_AWAKENS]}, "
            + "{name=Leia Organa, appearsIn=[A_NEW_HOPE, EMPIRE_STRIKES_BACK, RETURN_OF_THE_JEDI, THE_FORCE_AWAKENS]}"
            + "]}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryByRestrictingSubObject() {
        //given:
        String query = "query { Humans { select { name gender(where:{ code: {EQ: \"Male\"}}) { description } } } }";

        String expected = "{Humans={select=["
            + "{name=Luke Skywalker, gender={description=Male}}, "
            + "{name=Darth Vader, gender={description=Male}}, "
            + "{name=Han Solo, gender={description=Male}}, "
            + "{name=Wilhuff Tarkin, gender={description=Male}}"
            + "]}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryForSearchingByIntTypeSequenceField() {
        //given:
        String query = "query { CodeLists(where:{sequence:{EQ: 2}}) { select {id description active type sequence } } }";

        String expected = "{CodeLists={select=[{id=1, description=Female, active=true, type=org.crygier.graphql.model.starwars.Gender, sequence=2}]}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryForSearchingByIntTypeSequenceInWhereField() {
        //given:
        String query = "query { CodeLists(where: {sequence: {EQ: 2}}) { select { id description active type sequence } } }";

        String expected = "{CodeLists={select=[{id=1, description=Female, active=true, type=org.crygier.graphql.model.starwars.Gender, sequence=2}]}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryForSearchingByBooleanTypeActiveField() {
        //given:
        String query = "query { CodeLists(where: { active: {EQ:true}}) { select {id description active type sequence } } }";

        String expected = "{CodeLists={select=["
            + "{id=0, description=Male, active=true, type=org.crygier.graphql.model.starwars.Gender, sequence=1}, "
            + "{id=1, description=Female, active=true, type=org.crygier.graphql.model.starwars.Gender, sequence=2}"
            + "]}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryForSearchingByBooleanTypeActiveFieldNotEqual() {
        //given:
        String query = "query { CodeLists(where: { active: {NE:true}}) { select {id description active type sequence } } }";

        String expected = "{CodeLists={select=[]}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryForSearchingByStringTypeDescriptionFieldNotEqual() {
        //given:
        String query = "query { CodeLists(where: { description: {NE:\"Male\"}}) { select {id description active type sequence } } }";

        String expected = "{CodeLists={select=["
                + "{id=1, description=Female, active=true, type=org.crygier.graphql.model.starwars.Gender, sequence=2}"
                + "]}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }


    @Test
    public void queryForSearchingByIntTypeSequenceInWhereFieldNotEqual() {
        //given:
        String query = "query { CodeLists(where: {sequence: {NE: 2}}) { select { id description active type sequence } } }";

        String expected = "{CodeLists={select=["
                + "{id=0, description=Male, active=true, type=org.crygier.graphql.model.starwars.Gender, sequence=1}"
                + "]}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryWithTypenameDeepNesting() {
        //given:
        String query = "query { Droid(id: \"2001\") { name, friends { name, appearsIn, friends { name __typename } __typename } __typename } }";


        String expected = "{Droid={name=R2-D2, friends=["
            + "{name=Leia Organa, appearsIn=[A_NEW_HOPE, EMPIRE_STRIKES_BACK, RETURN_OF_THE_JEDI, THE_FORCE_AWAKENS], friends=[{name=C-3PO, __typename=Character}, {name=R2-D2, __typename=Character}, {name=Han Solo, __typename=Character}, {name=Luke Skywalker, __typename=Character}], __typename=Character}, "
            + "{name=Han Solo, appearsIn=[A_NEW_HOPE, EMPIRE_STRIKES_BACK, RETURN_OF_THE_JEDI, THE_FORCE_AWAKENS], friends=[{name=R2-D2, __typename=Character}, {name=Leia Organa, __typename=Character}, {name=Luke Skywalker, __typename=Character}], __typename=Character}, "
            + "{name=Luke Skywalker, appearsIn=[A_NEW_HOPE, EMPIRE_STRIKES_BACK, RETURN_OF_THE_JEDI, THE_FORCE_AWAKENS], friends=[{name=C-3PO, __typename=Character}, {name=R2-D2, __typename=Character}, {name=Leia Organa, __typename=Character}, {name=Han Solo, __typename=Character}], __typename=Character}"
            + "], __typename=Droid}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }
    
    @Test
    public void queryWithTypenameSimple() {
        //given:
        String query = "query { CodeLists(where: {sequence: {NE: 2}}) { select { id description active type sequence __typename} } }";

        String expected = "{CodeLists={select=["
                + "{id=0, description=Male, active=true, type=org.crygier.graphql.model.starwars.Gender, sequence=1, __typename=CodeList}"
                + "]}}";

        //when:
        Object result = executor.execute(query).getData();

        //then:
        assertThat(result.toString()).isEqualTo(expected);
    }

}
