package com.introproventures.graphql.jpa.query.schema;

import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaExecutor;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.NONE)
@TestPropertySource({"classpath:hibernate.properties"})
public class CalcEntityTests {
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

    @Test
    public void contextLoads() {
        Assert.isAssignable(GraphQLExecutor.class, executor.getClass());
    }

    @Test
    public void getAllRecords() {
        //given
        String query = "query GraphQLCalcFields { CalcEntities { select {id title fieldMem fieldFun logic customLogic } } }";

        String expected = "{CalcEntities={select=[{id=1, title=title 1, fieldMem=member, fieldFun=title 1 function, logic=true, customLogic=false}, {id=2, title=title 2, fieldMem=member, fieldFun=title 2 function, logic=true, customLogic=false}]}}";

        //when
        Object result = executor.execute(query).getData();

        //then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void testIgnoreFields() {
        String query = "query GraphQLCalcFields { CalcEntities { select {id title fieldMem fieldFun logic customLogic hideField hideFieldFunction } } }";

        String expected = "[ValidationError{validationErrorType=FieldUndefined, queryPath=[CalcEntities, select, hideField], message=Validation error of type FieldUndefined: Field 'hideField' in type 'CalcEntity' is undefined @ 'CalcEntities/select/hideField', locations=[SourceLocation{line=1, column=95}], description='Field 'hideField' in type 'CalcEntity' is undefined'}, ValidationError{validationErrorType=FieldUndefined, queryPath=[CalcEntities, select, hideFieldFunction], message=Validation error of type FieldUndefined: Field 'hideFieldFunction' in type 'CalcEntity' is undefined @ 'CalcEntities/select/hideFieldFunction', locations=[SourceLocation{line=1, column=105}], description='Field 'hideFieldFunction' in type 'CalcEntity' is undefined'}]";

        //when
        Object result = executor.execute(query).getErrors();

        //then
        assertThat(result.toString()).isEqualTo(expected);
    }

}
