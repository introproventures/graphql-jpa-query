package com.introproventures.graphql.jpa.query.restricted;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.Metamodel;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.util.Assert;

import com.introproventures.graphql.jpa.query.AbstractSpringBootTestSupport;
import com.introproventures.graphql.jpa.query.schema.GraphQLExecutor;
import com.introproventures.graphql.jpa.query.schema.GraphQLSchemaBuilder;
import com.introproventures.graphql.jpa.query.schema.RestrictedKeysProvider;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaExecutor;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;
import com.introproventures.graphql.jpa.query.schema.model.uuid.Thing;

@SpringBootTest(properties = "spring.datasource.data=RestrictedKeysProviderTests.sql")
public class RestrictedKeysProviderTests extends AbstractSpringBootTestSupport {

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
                .restrictedKeysProvider(restrictedKeysProvider(entityManager.getMetamodel()));
        }
        
        public RestrictedKeysProvider restrictedKeysProvider(Metamodel metamodel) {
            return (entityDescriptor) -> {
                Authentication token = SecurityContextHolder.getContext()
                                                            .getAuthentication();
                if (token == null) {
                    return Optional.empty();
                }
                
                String entityName = metamodel.entity(entityDescriptor.getEntity())
                                             .getName();
                
                List<Object> keys = resolveKeys(entityName, token.getAuthorities());
                
                return keys.isEmpty() ? Optional.empty()  // restrict query if no permissions exists 
                                      : Optional.of(keys); // execute query with restricted keys
            };
        }

        private List<Object> resolveKeys(String entityName, Collection<? extends GrantedAuthority> grantedAuthorities) {
            return grantedAuthorities.stream()
                                     .filter(GrantedAuthority.class::isInstance)
                                     .map(GrantedAuthority.class::cast)
                                     .map(GrantedAuthority::getAuthority)
                                     .map(authority -> authority.split(":"))
                                     .filter(permission -> entityName.equals(permission[0]))
                                     .filter(permission -> "read".equals(permission[1]))
                                     .map(permission -> permission[2])
                                     .collect(Collectors.toList());
            
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
        String query = "query RestrictedThingQuery { Things { select {id type } } }";
        String expected = "{Things={select=[{id=2d1ebc5b-7d27-4197-9cf0-e84451c5bbb1, type=Thing1}]}}";

        //when
        Object result = executor.execute(query).getData();

        //then
        assertThat(result.toString()).isEqualTo(expected);
    }
    
    @Test
    @WithMockUser(value = "spring", authorities = "Thing:read:*")
    public void testNonRestrictedThingQuery() {
        //given
        String query = "query RestrictedThingQuery { Things { select {id type } } }";
        String expected = "{Things={select=["
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
        String query = "query RestrictedThingQuery { Things { select {id type } } }";
        String expected = "{Things={select=[]}}";

        //when
        Object result = executor.execute(query).getData();

        //then
        assertThat(result.toString()).isEqualTo(expected);
    }        

    @Test
    @WithAnonymousUser
    public void testRestrictAllThingQueryForAnonymous() {
        //given
        String query = "query RestrictedThingQuery { Things { select {id type } } }";
        String expected = "{Things={select=[]}}";

        //when
        Object result = executor.execute(query).getData();

        //then
        assertThat(result.toString()).isEqualTo(expected);
    }        

    @Test
    public void testRestrictAllThingQueryWithNullAuthentication() {
        //given
        String query = "query RestrictedThingQuery { Things { select {id type } } }";
        String expected = "{Things={select=[]}}";

        //when
        Object result = executor.execute(query).getData();

        //then
        assertThat(result.toString()).isEqualTo(expected);
    }        
    
}
