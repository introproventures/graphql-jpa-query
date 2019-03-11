package com.introproventures.graphql.jpa.query.schema;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaExecutor;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;

import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.validation.ValidationError;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.NONE)
@TestPropertySource({"classpath:hibernate.properties"})
public class CalculatedEntityTests {
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
        String query = "query GraphQLCalcFields { CalculatedEntities { select {id title fieldMem fieldFun logic customLogic } } }";

        String expected = "{CalculatedEntities={select=[{id=1, title=title 1, fieldMem=member, fieldFun=title 1 function, logic=true, customLogic=false}, {id=2, title=title 2, fieldMem=member, fieldFun=title 2 function, logic=true, customLogic=false}]}}";

        //when
        Object result = executor.execute(query).getData();

        //then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void testIgnoreFields() {
        String query = "query GraphQLCalcFields { CalculatedEntities { select {id title fieldMem fieldFun logic customLogic hideField hideFieldFunction } } }";

        //when
        List<GraphQLError> result = executor.execute(query).getErrors();

        //then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isExactlyInstanceOf(ValidationError.class)
        			             .extracting(ValidationError.class::cast)
        			             .extracting("errorType", "queryPath")
      				             .contains(ErrorType.ValidationError, Arrays.asList("CalculatedEntities", "select", "hideFieldFunction"));
    }

}
