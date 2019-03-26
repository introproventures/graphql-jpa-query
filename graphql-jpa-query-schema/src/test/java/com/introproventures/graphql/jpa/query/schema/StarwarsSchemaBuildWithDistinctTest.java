package com.introproventures.graphql.jpa.query.schema;

import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaExecutor;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;
import graphql.schema.GraphQLSchema;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;

import java.util.LinkedHashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment= SpringBootTest.WebEnvironment.NONE
)
public class StarwarsSchemaBuildWithDistinctTest {
    @SpringBootApplication
    static class TestConfiguration {
        @Bean
        public GraphQLJpaSchemaBuilder graphQLSchemaBuilder(EntityManager entityManager) {
            return new GraphQLJpaSchemaBuilder(entityManager)
                    .name("Starwars")
                    .description("Starwars Universe Schema")
                    .useDistinctParameter(true);
        }

        @Bean
        public GraphQLExecutor graphQLExecutor(final GraphQLSchemaBuilder graphQLSchemaBuilder) {
            return new GraphQLJpaExecutor(graphQLSchemaBuilder.build());
        }
    }

    @Autowired
    private GraphQLJpaSchemaBuilder builder;

    @Autowired
    private GraphQLExecutor executor;

    @Before
    public void setup() {
    }

    @Test
    public void correctlyDerivesPageableSchemaFromGivenEntities() {
        //when
        GraphQLSchema schema = builder.build();

        // then
        assertThat(schema)
                .describedAs("Ensure the schema is generated")
                .isNotNull();


        //then
        assertThat(schema.getQueryType()
                .getFieldDefinition("CodeLists").getArguments()
        )
                .describedAs("Ensure query has three arguments")
                .hasSize(3);
    }

    @Test
    public void distinctFalse() {
        //given
        String query = "{ Books(distinct: false) { select { genre } }}";

        //when
        LinkedHashMap select = (LinkedHashMap)((LinkedHashMap)executor.execute(query).getData()).get("Books");
        List books = (List)select.get("select");

        org.junit.Assert.assertTrue(books.size() > 2);
    }
}
