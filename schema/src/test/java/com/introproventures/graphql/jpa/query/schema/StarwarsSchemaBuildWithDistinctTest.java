package com.introproventures.graphql.jpa.query.schema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.introproventures.graphql.jpa.query.AbstractSpringBootTestSupport;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaExecutor;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;
import graphql.schema.GraphQLSchema;
import jakarta.persistence.EntityManager;
import java.util.LinkedHashMap;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;

@SpringBootTest
public class StarwarsSchemaBuildWithDistinctTest extends AbstractSpringBootTestSupport {

    @SpringBootConfiguration
    @EnableAutoConfiguration
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

    @BeforeEach
    public void setup() {}

    @Test
    public void correctlyDerivesPageableSchemaFromGivenEntities() {
        //when
        GraphQLSchema schema = builder.build();

        // then
        assertThat(schema).describedAs("Ensure the schema is generated").isNotNull();

        //then
        assertThat(schema.getQueryType().getFieldDefinition("CodeLists").getArguments())
            .describedAs("Ensure query has three arguments")
            .hasSize(3);
    }

    @Test
    public void distinctFalse() {
        //given
        String query = "{ Books(distinct: false) { select { genre } }}";

        //when
        LinkedHashMap select = (LinkedHashMap) ((LinkedHashMap) executor.execute(query).getData()).get("Books");
        List books = (List) select.get("select");

        assertTrue(books.size() > 2);
    }
}
