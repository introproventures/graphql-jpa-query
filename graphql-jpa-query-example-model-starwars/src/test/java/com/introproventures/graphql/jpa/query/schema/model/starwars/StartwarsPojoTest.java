package com.introproventures.graphql.jpa.query.schema.model.starwars;

import static pl.pojo.tester.api.assertion.Assertions.assertPojoMethodsFor;

import org.junit.Test;

public class StartwarsPojoTest {

    @Test
    public void testCodeList() {
        assertPojoMethodsFor(CodeList.class).quickly()
                                            .areWellImplemented();
    }

    @Test
    public void testDroid() {
        assertPojoMethodsFor(Droid.class).quickly()
                                         .areWellImplemented();
    }

    @Test
    public void testHuman() {
        assertPojoMethodsFor(Human.class).quickly()
                                         .areWellImplemented();
    }

    @Test
    public void testDroidFunction() {
        assertPojoMethodsFor(DroidFunction.class).quickly()
                                                 .areWellImplemented();
    }

}
