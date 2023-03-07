package com.introproventures.graphql.jpa.query.idclass;

import com.introproventures.graphql.jpa.query.AbstractSpringBootTestSupport;
import com.introproventures.graphql.jpa.query.schema.GraphQLExecutor;
import com.introproventures.graphql.jpa.query.schema.GraphQLSchemaBuilder;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaExecutor;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;
import jakarta.persistence.EntityManager;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.sql.init.data-locations=EntityWithIdClassTest.sql")
public class EntityWithIdClassTest extends AbstractSpringBootTestSupport {

    @SpringBootApplication
    static class Application {
        @Bean
        public GraphQLExecutor graphQLExecutor(final GraphQLSchemaBuilder graphQLSchemaBuilder) {
            return new GraphQLJpaExecutor(graphQLSchemaBuilder.build());
        }

        @Bean
        public GraphQLSchemaBuilder graphQLSchemaBuilder(final EntityManager entityManager) {
            return new GraphQLJpaSchemaBuilder(entityManager)
                    .name("IdClassCompsiteKeysTest");
        }
    }

    @Autowired
    private GraphQLExecutor executor;

    @Test
    public void querySingularEntityWithIdClass() {
        //given
        String query = "query {" +
                "  Account(" +
                "    accountNumber: \"1\"" +
                "    accountType: \"Savings\"" +
                "  )" +
                "  {" +
                "    accountNumber" +
                "    accountType" +
                "    description" +
                "  }" +
                "}";

        String expected = "{Account={accountNumber=1, accountType=Savings, description=Saving account record}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

    @Test
    public void queryEntityWithIdClass() {
        //given
        String query = "query {" +
                "  Accounts {" +
                "    total" +
                "    pages" +
                "    select {" +
                "       accountNumber" +
                "       accountType" +
                "       description" +
                "    }" +
                "  }" +
                "}";

        String expected = "{Accounts={total=2, pages=1, select=["
                + "{accountNumber=1, accountType=Savings, description=Saving account record}, "
                + "{accountNumber=2, accountType=Checking, description=Checking account record}"
                + "]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }
    
    @Test
    public void queryEntityWithIdClassWhereCriteriaExpression() {
        //given
        String query = "query {" +
                "  Accounts(" +
                "    where: {" +
                "      accountNumber: {" +
                "        EQ: \"1\"" +
                "      }" +
                "      accountType: {" +
                "        EQ: \"Savings\"" +
                "      }" +
                "    })" +
                "  {" +
                "    total" +
                "    pages" +
                "    select {" +
                "       accountNumber" +
                "       accountType" +
                "       description" +
                "    }" +
                "  }" +
                "}";

        String expected = "{Accounts={total=1, pages=1, select=["
                + "{accountNumber=1, accountType=Savings, description=Saving account record}"
                + "]}}";

        //when
        Object result = executor.execute(query).getData();

        // then
        assertThat(result.toString()).isEqualTo(expected);
    }

}
