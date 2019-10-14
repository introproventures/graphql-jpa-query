package com.introproventures.graphql.jpa.query.mutations;

import com.introproventures.graphql.jpa.query.mutations.model.book.*;
import com.introproventures.graphql.jpa.query.schema.GraphQLExecutor;
import com.introproventures.graphql.jpa.query.schema.GraphQLSchemaBuilder;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaExecutor;
import graphql.ExecutionResult;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.NONE)
@TestPropertySource({"classpath:hibernate.properties"})
public class MutationTests {

    static class TransactionQLExecutor {
        @Autowired
        private GraphQLExecutor executor;

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public ExecutionResult execute(String query) {
            ExecutionResult res = executor.execute(query);
            return res;
        }

        public GraphQLExecutor getExecutor() {
            return executor;
        }
    }

    @SpringBootApplication
    static class Application {
        private static String currentRole = "user";

        public static String getCurrentRole() {
            return currentRole;
        }

        public static void setCurrentRole(String currentRole) {
            Application.currentRole = currentRole;
        }

        @Bean
        public GraphQLExecutor graphQLExecutor(final GraphQLSchemaBuilder graphQLSchemaBuilder) {
            return new GraphQLJpaExecutor(graphQLSchemaBuilder.build());
        }

        @Bean
        public GraphQLSchemaBuilder graphQLSchemaBuilder(final EntityManager entityManager) {
            Predicate<String[]> predicateAccess = roles -> {
                for(int i = 0; i < roles.length; i++) {
                    if (roles[i].equals(this.currentRole)) {
                        return true;
                    }
                }
                return false;
            };

            return new GraphQLJpaSchemaBuilderWithMutation(entityManager)
                    .predicateRole(predicateAccess)
                    .name("GraphQLBooks")
                    .description("Books JPA test schema");
        }

        @Bean
        public TransactionQLExecutor transactionQLExecutor() {
            return new TransactionQLExecutor();
        }

    }

    @Autowired
    TransactionQLExecutor transactionQLExecutor;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    DataSource dataSource;

    Logger log = LoggerFactory.getLogger(MutationTests.class);

    public void checkCorrectExecutionResult(ExecutionResult result) {
        if (result.getErrors().size() > 0) {
            String errorMsg = result.getErrors().stream().map(Object::toString)
                    .collect(Collectors.joining(", "));
            throw new RuntimeException("Error graphQL result: "+errorMsg);
        }
    }

    @Test
    public void contextLoads() {
        Application.setCurrentRole("user");

        org.springframework.util.Assert.isAssignable(GraphQLExecutor.class, transactionQLExecutor.getExecutor().getClass());
    }

    @Test
    public void queryInsertStudentOkWriteAccess() {
        Application.setCurrentRole("user");

        String query  = "mutation  {    insertStudent(entity: {id: 46, name: \"nm\"}) {id, name} }";

        String expected = "{insertStudent={id=46, name=nm}}";

        ExecutionResult result = transactionQLExecutor.execute(query);
        checkCorrectExecutionResult(result);

        String strRes = result.getData().toString();
        log.info("InsertStudent Result: "+strRes);
        assertThat(strRes).isEqualTo(expected);
    }

    @Test
    public void queryInsertStudentErrorWriteAccess() {
        Application.setCurrentRole("test");

        String query  = "mutation { insertStudent(entity: {id: 46, name: \"nm\"}) {id, name} }";

        ExecutionResult result = transactionQLExecutor.execute(query);

        assertThat(result.getErrors())
                .isNotEmpty()
                .extracting("message")
                .containsOnly(
                        "Exception while fetching data (/insertStudent) : For entity Student cannot INSERT"
                );
    }

    @Test
    @Transactional
    public void queryInsertBook() {
        Application.setCurrentRole("user");

        Author author = entityManager.find(Author.class, 1L);

        String query  = "mutation  {    insertBook(" +
                "entity: {id: 5, title: \"Shot\", author: {id: 1}, houses: [{id: 1}, {id: 2}] }) " +
                "{id, title, genre, author{id, name, genre}, houses {id, name}} }";

        String expected = "{insertBook={id=5, title=Shot, genre=null, author={id=1, name=Pushkin, genre=NOVEL}, houses=[{id=2, name=house 2}, {id=1, name=house 1}]}}";

        ExecutionResult result = transactionQLExecutor.execute(query);
        checkCorrectExecutionResult(result);

        String strRes = result.getData().toString();
        log.info("InsertBook Result: "+strRes);
        assertThat(strRes).isEqualTo(expected);

        Book book = entityManager.find(Book.class, 5L);
        assertThat(book.getTitle()).isEqualTo("Shot");
        assertThat(book.getHouses().size()).isEqualTo(2);
    }

    @Test
    public void queryUpdateBook() {
        Application.setCurrentRole("user");

        String query  = "mutation  {    updateBook(entity: {id: 2, title: \"Shot 2\", author: {id: 2}}) {id, title, genre, author{id, name, genre}} }";

        String expected = "{updateBook={id=2, title=Shot 2, genre=PLAY, author={id=2, name=Lermontov, genre=PLAY}}}";

        ExecutionResult result = transactionQLExecutor.execute(query);
        checkCorrectExecutionResult(result);

        String strRes = result.getData().toString();
        log.info("UpdateBook Result: "+strRes);
        assertThat(strRes).isEqualTo(expected);
    }

    @Test
    public void queryDeleteBook() {
        Application.setCurrentRole("user");

        String query  = "mutation  { deleteBook(entity: {id: 3}) {id, title} }";

        String expected = "{deleteBook=null}";

        ExecutionResult result = transactionQLExecutor.execute(query);
        checkCorrectExecutionResult(result);

        String strRes = result.getData().toString();
        log.info("DeleteBook Result: "+strRes);
        assertThat(strRes).isEqualTo(expected);
    }

    @Test
    @Transactional
    public void queryMergeBook() {
        Application.setCurrentRole("user");

        String query  = "mutation  { mergeBook(entity: {id: 40, title: \"new book\", houses: [{id: 100, name:\"new house\"}, {id: 2}]}) " +
                "{id, title, genre, author{id, name, genre}, houses {id, name}} }";

        String expected = "{mergeBook={id=40, title=new book, genre=null, author=null, houses=[{id=2, name=house 2}, {id=100, name=new house}]}}";

        ExecutionResult result = transactionQLExecutor.execute(query);
        checkCorrectExecutionResult(result);

        String strRes = result.getData().toString();
        log.info("MergeBook Result: "+strRes);
        assertThat(strRes).isEqualTo(expected);


        Book book = entityManager.find(Book.class, 40L);
        assertThat(book.getHouses().size()).isEqualTo(2);
        Assert.assertTrue(book.getHouses().stream().anyMatch(e -> e.getId().equals(100L)));
        Assert.assertTrue(book.getHouses().stream().anyMatch(e -> e.getId().equals(2L)));

        //delete array element
        query  = "mutation  { mergeBook(entity: {id: 40, title: \"new book\", houses: [{id: 100, name: \"new house\"}]}) " +
                "{id, title, genre, author{id, name, genre}, houses {id, name}} }";

        expected = "{mergeBook={id=40, title=new book, genre=null, author=null, houses=[{id=100, name=new house}]}}";

        result = transactionQLExecutor.execute(query);
        checkCorrectExecutionResult(result);

        strRes = result.getData().toString();
        log.info("MergeBook Result: "+strRes);
        assertThat(strRes).isEqualTo(expected);

        book = entityManager.find(Book.class, 40L);
        assertThat(book.getHouses().size()).isEqualTo(2);
        Assert.assertTrue(book.getHouses().stream().anyMatch(e -> e.getId().equals(100L)));
    }

    @Test
    @Transactional
    public void queryMergeHouse() {
        Application.setCurrentRole("user");

        String query  = "mutation  { mergePublishingHouse(entity: {id: 400, name: \"new house\", books: [{id: 201, title:\"new book 201\"}, {id: 2}]}) " +
                "{id, name, books{id, title, genre}} }";

        String expected = "{mergePublishingHouse={id=400, name=new house, books=[{id=201, title=new book 201, genre=null}, {id=2, title=book2, genre=PLAY}]}}";

        ExecutionResult result = transactionQLExecutor.execute(query);
        checkCorrectExecutionResult(result);

        String strRes = result.getData().toString();
        log.info("MergeHouse Result: "+strRes);
        assertThat(strRes).isEqualTo(expected);

        PublishingHouse ph = entityManager.find(PublishingHouse.class, 400L);
        assertThat(ph.getBooks().size()).isEqualTo(2);
        ph.getBooks().stream().forEach(element -> System.out.println(element.getId()));
        Assert.assertTrue(ph.getBooks().stream().anyMatch(e -> e.getId().equals(201L)));
        Assert.assertTrue(ph.getBooks().stream().anyMatch(e -> e.getId().equals(2L)));
    }

    @Test
    @Transactional
    public void queryInsertAuthor() {
        Application.setCurrentRole("user");

        String query  = "mutation  { insertAuthor(entity: {id: 40, name: \"qwe\",  phoneNumbers: [\"123456\", \"987654\"]}) " +
                "{id, name, genre, phoneNumbers} }";

        String expected = "{insertAuthor={id=40, name=qwe, genre=null, phoneNumbers=[123456, 987654]}}";

        ExecutionResult result = transactionQLExecutor.execute(query);
        checkCorrectExecutionResult(result);

        String strRes = result.getData().toString();
        log.info("InsertAuthor Result: "+strRes);
        assertThat(strRes).isEqualTo(expected);

        Author author = entityManager.find(Author.class, 40L);
        assertThat(author.getPhoneNumbers().size()).isEqualTo(2);
    }

    @Test
    public void queryMergeAuthor() {
        Application.setCurrentRole("user");

        String query  = "mutation  { mergeAuthor(entity: {id: 40, name: \"qwe\",  books: [{id: 100, title:\"new book\"}, {id: 2}]}) " +
                "{id, name, genre, books {id, title}} }";

        String expected = "{mergeAuthor={id=40, name=qwe, genre=null, books=[{id=100, title=new book}, {id=2, title=null}]}}";

        ExecutionResult result = transactionQLExecutor.execute(query);
        checkCorrectExecutionResult(result);

        String strRes = result.getData().toString();
        log.info("MergeAuthor Result: "+strRes);
        assertThat(strRes).isEqualTo(expected);
    }
}
