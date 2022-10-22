package com.introproventures.graphql.jpa.query.schema;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;

import org.junit.Test;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;

import com.introproventures.graphql.jpa.query.AbstractSpringBootTestSupport;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaExecutor;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;

import graphql.ExecutionResult;

@SpringBootTest
@Transactional
public class GraphQLExecutorExperimentalTests extends AbstractSpringBootTestSupport {
    
    @SpringBootApplication
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
                .enableSubscription(true);
        }
        
    }
    
    @Autowired
    private GraphQLExecutor executor;

    @Test
    public void querySubscriptionStream() throws InterruptedException {
        //given
        String query = "subscription { Books { id title genre author {name} } }";
        
        //when
        ExecutionResult initialResult = executor.execute(query);
        
        //then
        assertThat(initialResult.getErrors()).isEmpty();
        
        //when
        List<Object> resultList = new ArrayList<>();

        Publisher<ExecutionResult> deferredResultStream = initialResult.getData();

        CountDownLatch doneORCancelled = new CountDownLatch(1);
        
        Subscriber<ExecutionResult> subscriber = new Subscriber<ExecutionResult>() {

            Subscription subscription;

            @Override
            public void onSubscribe(Subscription s) {
                subscription = s;
                subscription.request(1);
            }

            @Override
            public void onNext(ExecutionResult executionResult) {
                subscription.request(1);

                Map<String,Object> data = executionResult.getData();
                resultList.addAll(data.values());
            }

            @Override
            public void onError(Throwable t) {
                doneORCancelled.countDown();
            }

            @Override
            public void onComplete() {
                doneORCancelled.countDown();
            }
        };

        deferredResultStream.subscribe(subscriber);

        doneORCancelled.await(1, TimeUnit.SECONDS);
        
        // then
        assertThat(resultList).hasSize(5);
        
        assertThat(resultList.toString()).isEqualTo("["
                + "{id=2, title=War and Peace, genre=NOVEL, author={name=Leo Tolstoy}}, "
                + "{id=3, title=Anna Karenina, genre=NOVEL, author={name=Leo Tolstoy}}, "
                + "{id=5, title=The Cherry Orchard, genre=PLAY, author={name=Anton Chekhov}}, "
                + "{id=6, title=The Seagull, genre=PLAY, author={name=Anton Chekhov}}, "
                + "{id=7, title=Three Sisters, genre=PLAY, author={name=Anton Chekhov}}]");
    }

}
