package com.introproventures.graphql.jpa.query.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import com.introproventures.graphql.jpa.query.AbstractSpringBootTestSupport;
import com.introproventures.graphql.jpa.query.schema.GraphQLExecutor;
import graphql.ExecutionResult;
import org.junit.Test;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
public abstract class GraphQLExecutorExperimentalTestsSupport extends AbstractSpringBootTestSupport {

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
