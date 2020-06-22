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
public class RestrictedKeysProviderRelayTests extends AbstractSpringBootTestSupport {

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
                .enableRelay(true)
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
        String query = "query RestrictedThingQuery { things { pageInfo { hasNextPage, startCursor, endCursor } edges { node { id type } } } }";
        String expected = "{things={pageInfo={hasNextPage=false, startCursor=b2Zmc2V0PTE=, endCursor=b2Zmc2V0PTE=}, "
                + "edges=[{node={id=2d1ebc5b-7d27-4197-9cf0-e84451c5bbb1, type=Thing1}}]}}";

        //when
        Object result = executor.execute(query).getData();

        //then
        assertThat(result.toString()).isEqualTo(expected);
    }
    
    @Test
    @WithMockUser(value = "spring", authorities = "Thing:read:*")
    public void testNonRestrictedThingQuery() {
        //given
        String query = "query RestrictedThingQuery { things { pageInfo { hasNextPage, startCursor, endCursor } edges { node {id type } } } }";
        String expected = "{things={pageInfo={hasNextPage=false, startCursor=b2Zmc2V0PTE=, endCursor=b2Zmc2V0PTM=}, "
                + "edges=["
                + "{node={id=2d1ebc5b-7d27-4197-9cf0-e84451c5bbb1, type=Thing1}}, "
                + "{node={id=2d1ebc5b-7d27-4197-9cf0-e84451c5bbc1, type=Thing2}}, "
                + "{node={id=2d1ebc5b-7d27-4197-9cf0-e84451c5bbd1, type=Thing3}}"
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
        String query = "query RestrictedThingQuery { things { edges { node {id type } } } }";
        String expected = "{things={edges=[]}}";

        //when
        Object result = executor.execute(query).getData();

        //then
        assertThat(result.toString()).isEqualTo(expected);
    }        

    @Test
    @WithAnonymousUser
    public void testRestrictAllThingQueryForAnonymous() {
        //given
        String query = "query RestrictedThingQuery { things { edges { node {id type } } } }";
        String expected = "{things={edges=[]}}";
        
        //when
        Object result = executor.execute(query).getData();

        //then
        assertThat(result.toString()).isEqualTo(expected);
    }        

    @Test
    public void testRestrictAllThingQueryWithNullAuthentication() {
        //given
        String query = "query RestrictedThingQuery { things { edges { node {id type } } } }";
        String expected = "{things={edges=[]}}";
        
        //when
        Object result = executor.execute(query).getData();

        //then
        assertThat(result.toString()).isEqualTo(expected);
    }        
    
}
