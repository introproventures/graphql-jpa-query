package com.introproventures.graphql.jpa.query.restricted;

import static org.assertj.core.api.Assertions.assertThat;

import javax.persistence.EntityManager;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.util.Assert;

import com.introproventures.graphql.jpa.query.AbstractSpringBootTestSupport;
import com.introproventures.graphql.jpa.query.schema.GraphQLExecutor;
import com.introproventures.graphql.jpa.query.schema.GraphQLSchemaBuilder;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaExecutor;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;
import com.introproventures.graphql.jpa.query.schema.model.uuid.Thing;

@SpringBootTest(properties = "spring.datasource.data=RestrictedKeysProviderTests.sql")
public class RestrictedKeysProviderSelectTests extends AbstractSpringBootTestSupport {

    @SpringBootApplication
    @EntityScan(basePackageClasses = Thing.class)
    static class Application {
        
        @Bean
        public GraphQLExecutor graphQLExecutor(final GraphQLSchemaBuilder graphQLSchemaBuilder) {
            return new GraphQLJpaExecutor(graphQLSchemaBuilder.build());
        }

        @Bean
        public GraphQLSchemaBuilder graphQLSchemaBuilder(final EntityManager entityManager) {

            return new GraphQLJpaSchemaBuilder(entityManager)
                .name("GraphQLBooks")
                .description("Books JPA test schema")
                .enableDefaultMaxResults(false)
                .restrictedKeysProvider(new SpringSecurityRestrictedKeysProvider(entityManager.getMetamodel()));
        }
    }
    
    @Autowired
    private GraphQLExecutor executor;

    @Test
    public void contextLoads() {
        Assert.isAssignable(GraphQLExecutor.class, executor.getClass());
    }

    @Test
    @WithMockUser(value = "spring", authorities = "Thing:read:2d1ebc5b-7d27-4197-9cf0-e84451c5bbb1")
    public void testRestrictedThingQuery() {
        //given
        String query = "query RestrictedThingQuery { Things { total select {id type } } }";
        String expected = "{Things={total=1, select=[{id=2d1ebc5b-7d27-4197-9cf0-e84451c5bbb1, type=Thing1}]}}";

        //when
        Object result = executor.execute(query).getData();

        //then
        assertThat(result.toString()).isEqualTo(expected);
    }
    
    @Test
    @WithMockUser(value = "spring", authorities = {"Thing:read:2d1ebc5b-7d27-4197-9cf0-e84451c5bbb1", 
                                                   "Thing:read:2d1ebc5b-7d27-4197-9cf0-e84451c5bbc1"})
    public void testRestrictedThingQueryMultiple() {
        //given
        String query = "query RestrictedThingQuery { Things { total select {id type } } }";
        String expected = "{Things={total=2, select=["
                + "{id=2d1ebc5b-7d27-4197-9cf0-e84451c5bbb1, type=Thing1}, "
                + "{id=2d1ebc5b-7d27-4197-9cf0-e84451c5bbc1, type=Thing2}"
                + "]}}";

        //when
        Object result = executor.execute(query).getData();

        //then
        assertThat(result.toString()).isEqualTo(expected);
    }
    
    
    @Test
    @WithMockUser(value = "spring", authorities = "Thing:read:*")
    public void testNonRestrictedThingQuery() {
        //given
        String query = "query RestrictedThingQuery { Things { total select {id type } } }";
        String expected = "{Things={total=3, select=["
                + "{id=2d1ebc5b-7d27-4197-9cf0-e84451c5bbb1, type=Thing1}, "
                + "{id=2d1ebc5b-7d27-4197-9cf0-e84451c5bbc1, type=Thing2}, "
                + "{id=2d1ebc5b-7d27-4197-9cf0-e84451c5bbd1, type=Thing3}"
                + "]}}";

        //when
        Object result = executor.execute(query).getData();

        //then
        assertThat(result.toString()).isEqualTo(expected);
    }    

    @Test
    @WithMockUser(value = "spring", authorities = "OtherThing:*")
    public void testRestrictAllOtherThingQuery() {
        //given
        String query = "query RestrictedThingQuery { Things { total select {id type } } }";
        String expected = "{Things={total=0, select=[]}}";

        //when
        Object result = executor.execute(query).getData();

        //then
        assertThat(result.toString()).isEqualTo(expected);
    }        

    @Test
    @WithAnonymousUser
    public void testRestrictAllThingQueryForAnonymous() {
        //given
        String query = "query RestrictedThingQuery { Things { total select {id type } } }";
        String expected = "{Things={total=0, select=[]}}";

        //when
        Object result = executor.execute(query).getData();

        //then
        assertThat(result.toString()).isEqualTo(expected);
    }        

    @Test
    public void testRestrictAllThingQueryWithNullAuthentication() {
        //given
        String query = "query RestrictedThingQuery { Things { total select {id type } } }";
        String expected = "{Things={total=0, select=[]}}";

        //when
        Object result = executor.execute(query).getData();

        //then
        assertThat(result.toString()).isEqualTo(expected);
    }        
    
}
