package com.introproventures.graphql.jpa.query.localdatetime;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.TimeZone;

import javax.persistence.EntityManager;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;

import com.introproventures.graphql.jpa.query.AbstractSpringBootTestSupport;
import com.introproventures.graphql.jpa.query.schema.GraphQLExecutor;
import com.introproventures.graphql.jpa.query.schema.GraphQLSchemaBuilder;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaExecutor;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;

@SpringBootTest(properties = "spring.datasource.data=LocalDatetTmeData.sql")
public class GraphQLLocalDateTimeTest extends AbstractSpringBootTestSupport {

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

    @BeforeClass
    public static void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
    
    @Test
    public void queryLocalDateWithEqualTest() {
        //given
        String query = "query{" +
                "  localDates" +
                "  (where:{" +
                "    localDate:{" +
                "      EQ:\"2019-08-06\"" +
                "    }" +
                "  })" +
                "{" +
                "    select{" +
                "       id" +
                "       localDate" +
                "      description" +
                "    }" +
                "  }" +
                "}";

        String expected = "{localDates={select=[{id=6, localDate=2019-08-06, description=Add test for LocalDate.}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryLocalDateWithBetweenTest() {
        //given
        String query = "query{" +
                "  localDates" +
                "  (where:{" +
                "    localDate:{" +
                "      BETWEEN:[\"2019-08-05\",\"2019-08-06\"]" +
                "    }" +
                "  })" +
                "{" +
                "    select{" +
                "       id" +
                "       localDate" +
                "       description" +
                "    }" +
                "  }" +
                "}";

        String expected = "{localDates={select=[" +
                "{id=5, localDate=2019-08-05, description=Add test for LocalDate.}, " +
                "{id=6, localDate=2019-08-06, description=Add test for LocalDate.}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryLocalDateWithBetweenDuplicateDateTest() {
        //given
        String query = "query{" +
                "  localDates" +
                "  (where:{" +
                "    localDate:{" +
                "      BETWEEN:[\"2019-08-05\",\"2019-08-05\"]" +
                "    }" +
                "  })" +
                "{" +
                "    select{" +
                "       id" +
                "       localDate" +
                "       description" +
                "    }" +
                "  }" +
                "}";

        String expected = "{localDates={select=[{id=5, localDate=2019-08-05, description=Add test for LocalDate.}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryLocalDateWithGreaterThanOrEqualTest() {
        //given
        String query = "query{" +
                "  localDates" +
                "  (where:{" +
                "    localDate:{" +
                "      GE:\"2019-08-06\"" +
                "    }" +
                "  })" +
                "{" +
                "    select{" +
                "       id" +
                "       localDate" +
                "       description" +
                "    }" +
                "  }" +
                "}";

        String expected = "{localDates={select=[" +
                "{id=6, localDate=2019-08-06, description=Add test for LocalDate.}, " +
                "{id=7, localDate=2019-08-07, description=Add test for LocalDate.}, " +
                "{id=8, localDate=2019-08-08, description=Add test for LocalDate.}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryLocalDateWithGreaterThanTest() {
        //given
        String query = "query{" +
                "  localDates" +
                "  (where:{" +
                "    localDate:{" +
                "      GT:\"2019-08-05\"" +
                "    }" +
                "  })" +
                "{" +
                "    select{" +
                "       id" +
                "       localDate" +
                "       description" +
                "    }" +
                "  }" +
                "}";

        String expected = "{localDates={select=[" +
                "{id=6, localDate=2019-08-06, description=Add test for LocalDate.}, " +
                "{id=7, localDate=2019-08-07, description=Add test for LocalDate.}, " +
                "{id=8, localDate=2019-08-08, description=Add test for LocalDate.}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryLocalDateWithNotBetweenTest() {
        //given
        String query = "query{" +
                "  localDates" +
                "  (where:{" +
                "    localDate:{" +
                "      NOT_BETWEEN:[\"2019-08-04\",\"2019-08-05\"]" +
                "    }" +
                "  })" +
                "{" +
                "    select{" +
                "       id" +
                "       localDate" +
                "       description" +
                "    }" +
                "  }" +
                "}";

        String expected = "{localDates={select=[" +
                "{id=1, localDate=2019-08-01, description=Add test for LocalDate.}, " +
                "{id=2, localDate=2019-08-02, description=Add test for LocalDate.}, " +
                "{id=3, localDate=2019-08-03, description=Add test for LocalDate.}, " +
                "{id=6, localDate=2019-08-06, description=Add test for LocalDate.}, " +
                "{id=7, localDate=2019-08-07, description=Add test for LocalDate.}, " +
                "{id=8, localDate=2019-08-08, description=Add test for LocalDate.}" +
                "]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryLocalDateWithNotEqualTest() {
        //given
        String query = "query{" +
                "  localDates" +
                "  (where:{" +
                "    localDate:{" +
                "      NE:\"2019-08-04\"" +
                "    }" +
                "  })" +
                "{" +
                "    select{" +
                "       id" +
                "       localDate" +
                "       description" +
                "    }" +
                "  }" +
                "}";

        String expected = "{localDates={select=[" +
                "{id=1, localDate=2019-08-01, description=Add test for LocalDate.}, " +
                "{id=2, localDate=2019-08-02, description=Add test for LocalDate.}, " +
                "{id=3, localDate=2019-08-03, description=Add test for LocalDate.}, " +
                "{id=5, localDate=2019-08-05, description=Add test for LocalDate.}, " +
                "{id=6, localDate=2019-08-06, description=Add test for LocalDate.}, " +
                "{id=7, localDate=2019-08-07, description=Add test for LocalDate.}, " +
                "{id=8, localDate=2019-08-08, description=Add test for LocalDate.}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryLocalDateWithLessThanOrEqualTest() {
        //given
        String query = "query{" +
                "  localDates" +
                "  (where:{" +
                "    localDate:{" +
                "      LE:\"2019-08-06\"" +
                "    }" +
                "  })" +
                "{" +
                "    select{" +
                "       id" +
                "       localDate" +
                "       description" +
                "    }" +
                "  }" +
                "}";

        String expected = "{localDates={select=[" +
                "{id=1, localDate=2019-08-01, description=Add test for LocalDate.}, " +
                "{id=2, localDate=2019-08-02, description=Add test for LocalDate.}, " +
                "{id=3, localDate=2019-08-03, description=Add test for LocalDate.}, " +
                "{id=4, localDate=2019-08-04, description=Add test for LocalDate.}, " +
                "{id=5, localDate=2019-08-05, description=Add test for LocalDate.}, " +
                "{id=6, localDate=2019-08-06, description=Add test for LocalDate.}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryLocalDateWithLessThanTest() {
        //given
        String query = "query{" +
                "  localDates" +
                "  (where:{" +
                "    localDate:{" +
                "      LT:\"2019-08-02\"" +
                "    }" +
                "  })" +
                "{" +
                "    select{" +
                "       id" +
                "       localDate" +
                "       description" +
                "    }" +
                "  }" +
                "}";

        String expected = "{localDates={select=[{id=1, localDate=2019-08-01, description=Add test for LocalDate.}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryLocalDateTimeWithGreaterThanTest() {
        //given
        String query = "query{" +
                "  localDates" +
                "  (where:{" +
                "    localDateTime:{" +
                "      GT:\"2019-08-06T07:00:00.00\"" +
                "    }" +
                "  })" +
                "{" +
                "    select{" +
                "      id" +
                "      localDateTime" +
                "      description" +
                "    }" +
                "  }" +
                "}";

        String expected = "{localDates={select=[" +
                "{id=6, localDateTime=2019-08-06T10:58:08, description=Add test for LocalDate.}, " +
                "{id=7, localDateTime=2019-08-07T10:58:08, description=Add test for LocalDate.}, " +
                "{id=8, localDateTime=2019-08-08T10:58:08, description=Add test for LocalDate.}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryLocalDateTimeWithLessThanTest() {
        //given
        String query = "query{" +
                "  localDates" +
                "  (where:{" +
                "    localDateTime:{" +
                "      LT:\"2019-08-02T07:00:00.00\"" +
                "    }" +
                "  })" +
                "{" +
                "    select{" +
                "      id" +
                "      localDateTime" +
                "      description" +
                "    }" +
                "  }" +
                "}";

        String expected = "{localDates={select=[{id=1, localDateTime=2019-08-01T10:58:08, description=Add test for LocalDate.}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryLocalDateTimeWithEqualTest() {
        //given
        String query = "query{" +
                "  localDates" +
                "  (where:{" +
                "    localDateTime:{" +
                "      EQ:\"2019-08-06T10:58:08\"" +
                "    }" +
                "  })" +
                "{" +
                "    select{" +
                "      id" +
                "      localDateTime" +
                "      description" +
                "    }" +
                "  }" +
                "}";

        String expected = "{localDates={select=[{id=6, localDateTime=2019-08-06T10:58:08, description=Add test for LocalDate.}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryLocalDateTimeWithBetweenTest() {
        //given
        String query = "query{" +
                "  localDates" +
                "  (where:{" +
                "    localDateTime:{" +
                "      BETWEEN:[\"2019-08-05T10:58:08\",\"2019-08-06T13:58:08\"]" +
                "    }" +
                "  })" +
                "{" +
                "    select{" +
                "      id" +
                "      localDateTime" +
                "      description" +
                "    }" +
                "  }" +
                "}";

        String expected = "{localDates={select=[" +
                "{id=5, localDateTime=2019-08-05T10:58:08, description=Add test for LocalDate.}, " +
                "{id=6, localDateTime=2019-08-06T10:58:08, description=Add test for LocalDate.}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryLocalDateTimeWithLessThanOrEqualTest() {
        //given
        String query = "query{" +
                "  localDates" +
                "  (where:{" +
                "    localDateTime:{" +
                "      LE:\"2019-08-02T07:00:00.00\"" +
                "    }" +
                "  })" +
                "{" +
                "    select{" +
                "      id" +
                "      localDateTime" +
                "      description" +
                "    }" +
                "  }" +
                "}";

        String expected = "{localDates={select=[{id=1, localDateTime=2019-08-01T10:58:08, description=Add test for LocalDate.}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryLocalDateTimeWithNotBetweenTest() {
        //given
        String query = "query{" +
                "  localDates" +
                "  (where:{" +
                "    localDateTime:{" +
                "      NOT_BETWEEN:[\"2019-08-02T10:58:08\",\"2019-08-08T13:58:08\"]" +
                "    }" +
                "  })" +
                "{" +
                "    select{" +
                "      id" +
                "      localDateTime" +
                "      description" +
                "    }" +
                "  }" +
                "}";

        String expected = "{localDates={select=[{id=1, localDateTime=2019-08-01T10:58:08, description=Add test for LocalDate.}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryLocalDateTimeWithNotEqualTest() {
        //given
        String query = "query{" +
                "  localDates" +
                "  (where:{" +
                "    localDateTime:{" +
                "      NE:\"2019-08-05T10:58:08\"" +
                "    }" +
                "  })" +
                "{" +
                "    select{" +
                "      id" +
                "      localDateTime" +
                "      description" +
                "    }" +
                "  }" +
                "}";

        String expected = "{localDates={select=[" +
                "{id=1, localDateTime=2019-08-01T10:58:08, description=Add test for LocalDate.}, " +
                "{id=2, localDateTime=2019-08-02T10:58:08, description=Add test for LocalDate.}, " +
                "{id=3, localDateTime=2019-08-03T10:58:08, description=Add test for LocalDate.}, " +
                "{id=4, localDateTime=2019-08-04T10:58:08, description=Add test for LocalDate.}, " +
                "{id=6, localDateTime=2019-08-06T10:58:08, description=Add test for LocalDate.}, " +
                "{id=7, localDateTime=2019-08-07T10:58:08, description=Add test for LocalDate.}, " +
                "{id=8, localDateTime=2019-08-08T10:58:08, description=Add test for LocalDate.}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryOffsetDateTimeWithGreaterOrEqualTest() {
        //given
        String query = "query{" +
                "  localDates" +
                "  (where:{" +
                "    offsetDateTime:{" +
                "      GE:\"2019-08-08T10:58:07+07:00\"" +
                "    }" +
                "  })" +
                "{" +
                "    select{" +
                "      id" +
                "      offsetDateTime" +
                "      description" +
                "    }" +
                "  }" +
                "}";

        String expected = "{localDates={select=[{id=8, offsetDateTime=2019-08-08T03:58:07Z, description=Add test for LocalDate.}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryOffsetDateTimeWithGreaterThanTest() {
        //given
        String query = "query{" +
                "  localDates" +
                "  (where:{" +
                "    offsetDateTime:{" +
                "      GT:\"2019-08-07T10:58:07+07:00\"" +
                "    }" +
                "  })" +
                "{" +
                "    select{" +
                "      id" +
                "      offsetDateTime" +
                "      description" +
                "    }" +
                "  }" +
                "}";

        String expected = "{localDates={select=[{id=8, offsetDateTime=2019-08-08T03:58:07Z, description=Add test for LocalDate.}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryOffsetDateTimeWithBetweenTest() {
        //given
        String query = "query{" +
                "  localDates" +
                "  (where:{" +
                "    offsetDateTime:{" +
                "      BETWEEN:[\"2019-08-06T09:58:07+07:00\",\"2019-08-06T15:58:07+07:00\"]" +
                "    }" +
                "  })" +
                "{" +
                "    select{" +
                "      id" +
                "      offsetDateTime" +
                "      description" +
                "    }" +
                "  }" +
                "}";

        String expected = "{localDates={select=[{id=6, offsetDateTime=2019-08-06T03:58:07Z, description=Add test for LocalDate.}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryOffsetDateTimeWithNotBetweenTest() {
        //given
        String query = "query{" +
                "  localDates" +
                "  (where:{" +
                "    offsetDateTime:{" +
                "      NOT_BETWEEN:[\"2019-08-02T10:58:07+07:00\",\"2019-08-08T15:58:07+07:00\"]" +
                "    }" +
                "  })" +
                "{" +
                "    select{" +
                "      id" +
                "      offsetDateTime" +
                "      description" +
                "    }" +
                "  }" +
                "}";

        String expected = "{localDates={select=[{id=1, offsetDateTime=2019-08-01T03:58:07Z, description=Add test for LocalDate.}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryOffsetDateTimeWithLessThanTest() {
        //given
        String query = "query{" +
                "  localDates" +
                "  (where:{" +
                "    offsetDateTime:{" +
                "      LT:\"2019-08-02T10:58:07+07:00\"" +
                "    }" +
                "  })" +
                "{" +
                "    select{" +
                "      id" +
                "      offsetDateTime" +
                "      description" +
                "    }" +
                "  }" +
                "}";

        String expected = "{localDates={select=[{id=1, offsetDateTime=2019-08-01T03:58:07Z, description=Add test for LocalDate.}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryOffsetDateTimeWithLessThanOrEqualTest() {
        //given
        String query = "query{" +
                "  localDates" +
                "  (where:{" +
                "    offsetDateTime:{" +
                "      LE:\"2019-08-01T15:58:07+07:00\"" +
                "    }" +
                "  })" +
                "{" +
                "    select{" +
                "      id" +
                "      offsetDateTime" +
                "      description" +
                "    }" +
                "  }" +
                "}";

        String expected = "{localDates={select=[{id=1, offsetDateTime=2019-08-01T03:58:07Z, description=Add test for LocalDate.}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryOffsetDateTimeWithNotEqualTest() {
        //given
        String query = "query{" +
                "  localDates" +
                "  (where:{" +
                "    offsetDateTime:{" +
                "      NE:\"2019-08-01T10:58:07+07:00\"" +
                "    }" +
                "  })" +
                "{" +
                "    select{" +
                "      id" +
                "      offsetDateTime" +
                "      description" +
                "    }" +
                "  }" +
                "}";

        String expected = "{localDates={select=[" +
                "{id=2, offsetDateTime=2019-08-02T03:58:07Z, description=Add test for LocalDate.}, " +
                "{id=3, offsetDateTime=2019-08-03T03:58:07Z, description=Add test for LocalDate.}, " +
                "{id=4, offsetDateTime=2019-08-04T03:58:07Z, description=Add test for LocalDate.}, " +
                "{id=5, offsetDateTime=2019-08-05T03:58:07Z, description=Add test for LocalDate.}, " +
                "{id=6, offsetDateTime=2019-08-06T03:58:07Z, description=Add test for LocalDate.}, " +
                "{id=7, offsetDateTime=2019-08-07T03:58:07Z, description=Add test for LocalDate.}, " +
                "{id=8, offsetDateTime=2019-08-08T03:58:07Z, description=Add test for LocalDate.}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryZonedDateTimeWithGreaterThanTest() {
        //given
        String query = "query{" +
                "  localDates" +
                "  (where:{" +
                "    zonedDateTime:{" +
                "      GT:\"2019-08-07T19:58:07+07:00\"" +
                "    }" +
                "  })" +
                "{" +
                "    select{" +
                "      id" +
                "      zonedDateTime" +
                "      description" +
                "    }" +
                "  }" +
                "}";

        String expected = "{localDates={select=[{id=8, zonedDateTime=2019-08-08T03:58:08Z[UTC], description=Add test for LocalDate.}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryZonedDateTimeWithGreaterThanOrEqualTest() {
        //given
        String query = "query{" +
                "  localDates" +
                "  (where:{" +
                "    zonedDateTime:{" +
                "      GE:\"2019-08-08T10:58:07+07:00\"" +
                "    }" +
                "  })" +
                "{" +
                "    select{" +
                "      id" +
                "      zonedDateTime" +
                "      description" +
                "    }" +
                "  }" +
                "}";

        String expected = "{localDates={select=[{id=8, zonedDateTime=2019-08-08T03:58:08Z[UTC], description=Add test for LocalDate.}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryZonedDateTimeWithLessThanOrEqualTest() {
        //given
        String query = "query{" +
                "  localDates" +
                "  (where:{" +
                "    zonedDateTime:{" +
                "      LE:\"2019-08-01T03:58:08Z[UTC]\"" +
                "    }" +
                "  })" +
                "{" +
                "    select{" +
                "      id" +
                "      zonedDateTime" +
                "      description" +
                "    }" +
                "  }" +
                "}";

        String expected = "{localDates={select=[{id=1, zonedDateTime=2019-08-01T03:58:08Z[UTC], description=Add test for LocalDate.}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryZonedDateTimeWithLessThanTest() {
        //given
        String query = "query{" +
                "  localDates" +
                "  (where:{" +
                "    zonedDateTime:{" +
                "      LT:\"2019-08-02T10:58:07+07:00\"" +
                "    }" +
                "  })" +
                "{" +
                "    select{" +
                "      id" +
                "      zonedDateTime" +
                "      description" +
                "    }" +
                "  }" +
                "}";

        String expected = "{localDates={select=[{id=1, zonedDateTime=2019-08-01T03:58:08Z[UTC], description=Add test for LocalDate.}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryZonedDateTimeWithBetweenTest() {
        //given
        String query = "query{" +
                "  localDates" +
                "  (where:{" +
                "    zonedDateTime:{" +
                "      BETWEEN:[\"2019-08-05T10:58:07+07:00\",\"2019-08-06T15:58:07+07:00\"]" +
                "    }" +
                "  })" +
                "{" +
                "    select{" +
                "      id" +
                "      zonedDateTime" +
                "      description" +
                "    }" +
                "  }" +
                "}";

        String expected = "{localDates={select=[" +
                "{id=5, zonedDateTime=2019-08-05T03:58:08Z[UTC], description=Add test for LocalDate.}, " +
                "{id=6, zonedDateTime=2019-08-06T03:58:08Z[UTC], description=Add test for LocalDate.}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryZonedDateTimeWithNotBetweenTest() {
        //given
        String query = "query{" +
                "  localDates" +
                "  (where:{" +
                "    zonedDateTime:{" +
                "      NOT_BETWEEN:[\"2019-08-02T10:58:07+07:00\",\"2019-08-08T15:58:07+07:00\"]" +
                "    }" +
                "  })" +
                "{" +
                "    select{" +
                "      id" +
                "      zonedDateTime" +
                "      description" +
                "    }" +
                "  }" +
                "}";

        String expected = "{localDates={select=[{id=1, zonedDateTime=2019-08-01T03:58:08Z[UTC], description=Add test for LocalDate.}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryZonedDateTimeWithNotEqualTest() {
        //given
        String query = "query{" +
                "  localDates" +
                "  (where:{" +
                "    zonedDateTime:{" +
                "      NE:\"2019-08-05T03:58:08Z[UTC]\"" +
                "    }" +
                "  })" +
                "{" +
                "    select{" +
                "      id" +
                "      zonedDateTime" +
                "      description" +
                "    }" +
                "  }" +
                "}";

        String expected = "{localDates={select=[" +
                "{id=1, zonedDateTime=2019-08-01T03:58:08Z[UTC], description=Add test for LocalDate.}, " +
                "{id=2, zonedDateTime=2019-08-02T03:58:08Z[UTC], description=Add test for LocalDate.}, " +
                "{id=3, zonedDateTime=2019-08-03T03:58:08Z[UTC], description=Add test for LocalDate.}, " +
                "{id=4, zonedDateTime=2019-08-04T03:58:08Z[UTC], description=Add test for LocalDate.}, " +
                "{id=6, zonedDateTime=2019-08-06T03:58:08Z[UTC], description=Add test for LocalDate.}, " +
                "{id=7, zonedDateTime=2019-08-07T03:58:08Z[UTC], description=Add test for LocalDate.}, " +
                "{id=8, zonedDateTime=2019-08-08T03:58:08Z[UTC], description=Add test for LocalDate.}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryZonedDateTimeWithEqualTest() {
        //given
        String query = "query{" +
                "  localDates" +
                "  (where:{" +
                "    zonedDateTime:{" +
                "      EQ:\"2019-08-06T10:58:08+07:00\"" +
                "    }" +
                "  })" +
                "{" +
                "    select{" +
                "      id" +
                "      zonedDateTime" +
                "      description" +
                "    }" +
                "  }" +
                "}";

        String expected = "{localDates={select=[{id=6, zonedDateTime=2019-08-06T03:58:08Z[UTC], description=Add test for LocalDate.}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryInstantWithGreaterThanTest() {
        //given
        String query = "query{" +
                "  localDates" +
                "  (where:{" +
                "    instant:{" +
                "      GT:\"2019-08-07T03:58:08Z\"" +
                "    }" +
                "  })" +
                "{" +
                "    select{" +
                "      id" +
                "      instant" +
                "      description" +
                "    }" +
                "  }" +
                "}";

        String expected = "{localDates={select=[{id=8, instant=2019-08-08T03:58:08Z, description=Add test for LocalDate.}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryInstantWithGreaterThanOrEqualTest() {
        //given
        String query = "query{" +
                "  localDates" +
                "  (where:{" +
                "    instant:{" +
                "      GE:\"2019-08-08T03:58:08Z\"" +
                "    }" +
                "  })" +
                "{" +
                "    select{" +
                "      id" +
                "      instant" +
                "      description" +
                "    }" +
                "  }" +
                "}";

        String expected = "{localDates={select=[{id=8, instant=2019-08-08T03:58:08Z, description=Add test for LocalDate.}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryInstantWithLessThanOrEqualTest() {
        //given
        String query = "query{" +
                "  localDates" +
                "  (where:{" +
                "    instant:{" +
                "      LE:\"2019-08-01T03:58:08Z\"" +
                "    }" +
                "  })" +
                "{" +
                "    select{" +
                "      id" +
                "      instant" +
                "      description" +
                "    }" +
                "  }" +
                "}";

        String expected = "{localDates={select=[{id=1, instant=2019-08-01T03:58:08Z, description=Add test for LocalDate.}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryInstantWithLessThanTest() {
        //given
        String query = "query{" +
                "  localDates" +
                "  (where:{" +
                "    instant:{" +
                "      LT:\"2019-08-02T03:58:08Z\"" +
                "    }" +
                "  })" +
                "{" +
                "    select{" +
                "      id" +
                "      instant" +
                "      description" +
                "    }" +
                "  }" +
                "}";

        String expected = "{localDates={select=[{id=1, instant=2019-08-01T03:58:08Z, description=Add test for LocalDate.}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryInstantWithBetweenTest() {
        //given
        String query = "query{" +
                "  localDates" +
                "  (where:{" +
                "    instant:{" +
                "      BETWEEN:[\"2019-08-06T03:58:08Z\",\"2019-08-06T03:58:08Z\"]" +
                "    }" +
                "  })" +
                "{" +
                "    select{" +
                "      id" +
                "      instant" +
                "      description" +
                "    }" +
                "  }" +
                "}";

        String expected = "{localDates={select=[{id=6, instant=2019-08-06T03:58:08Z, description=Add test for LocalDate.}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryInstantWithNotBetweenTest() {
        //given
        String query = "query{" +
                "  localDates" +
                "  (where:{" +
                "    instant:{" +
                "      NOT_BETWEEN:[\"2019-08-02T03:58:08Z\",\"2019-08-08T03:58:08Z\"]" +
                "    }" +
                "  })" +
                "{" +
                "    select{" +
                "      id" +
                "      instant" +
                "      description" +
                "    }" +
                "  }" +
                "}";

        String expected = "{localDates={select=[{id=1, instant=2019-08-01T03:58:08Z, description=Add test for LocalDate.}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryInstantWithNotEqualTest() {
        //given
        String query = "query{" +
                "  localDates" +
                "  (where:{" +
                "    instant:{" +
                "      NE:\"2019-08-08T03:58:08Z\"" +
                "    }" +
                "  })" +
                "{" +
                "    select{" +
                "      id" +
                "      instant" +
                "      description" +
                "    }" +
                "  }" +
                "}";

        String expected = "{localDates={select=[" +
                "{id=1, instant=2019-08-01T03:58:08Z, description=Add test for LocalDate.}, " +
                "{id=2, instant=2019-08-02T03:58:08Z, description=Add test for LocalDate.}, " +
                "{id=3, instant=2019-08-03T03:58:08Z, description=Add test for LocalDate.}, " +
                "{id=4, instant=2019-08-04T03:58:08Z, description=Add test for LocalDate.}, " +
                "{id=5, instant=2019-08-05T03:58:08Z, description=Add test for LocalDate.}, " +
                "{id=6, instant=2019-08-06T03:58:08Z, description=Add test for LocalDate.}, " +
                "{id=7, instant=2019-08-07T03:58:08Z, description=Add test for LocalDate.}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryInstantWithEqualTest() {
        //given
        String query = "query{" +
                "  localDates" +
                "  (where:{" +
                "    instant:{" +
                "      EQ:\"2019-08-06T03:58:08Z\"" +
                "    }" +
                "  })" +
                "{" +
                "    select{" +
                "      id" +
                "      instant" +
                "      description" +
                "    }" +
                "  }" +
                "}";

        String expected = "{localDates={select=[{id=6, instant=2019-08-06T03:58:08Z, description=Add test for LocalDate.}]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }
}