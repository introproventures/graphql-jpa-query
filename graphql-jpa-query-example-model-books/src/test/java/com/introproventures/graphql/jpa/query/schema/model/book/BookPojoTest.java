package com.introproventures.graphql.jpa.query.schema.model.book;

import static pl.pojo.tester.api.assertion.Assertions.assertPojoMethodsFor;

import org.junit.Test;

public class BookPojoTest {

    @Test
    public void testAuthor() {
        assertPojoMethodsFor(Author.class).quickly()
                                          .areWellImplemented();
    }

    @Test
    public void testBook() {
        assertPojoMethodsFor(Book.class).quickly()
                                        .areWellImplemented();
    }
    
}
