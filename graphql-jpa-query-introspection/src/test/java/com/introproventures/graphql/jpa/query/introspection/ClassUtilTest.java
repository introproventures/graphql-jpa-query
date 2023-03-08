package com.introproventures.graphql.jpa.query.introspection;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.RandomAccess;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClassUtilTest {

    @Test
    public void getAllInterfaces() {
        assertEquals(null, ClassUtil.getAllInterfaces(null));
        assertTrue(ClassUtil.getAllInterfaces(Object.class).isEmpty());

        assertFalse(ClassUtil.getAllInterfaces(String.class).isEmpty());
        assertFalse(ClassUtil.getAllInterfaces(Class.class).isEmpty());

        List<Class<?>> supers = ClassUtil.getAllInterfaces(ArrayList.class);
        assertFalse(supers.contains(AbstractList.class));
        assertFalse(supers.contains(AbstractCollection.class));

        assertTrue(supers.contains(List.class));
        assertTrue(supers.contains(RandomAccess.class));
        assertTrue(supers.contains(Iterable.class));
        assertFalse(supers.contains(Object.class));

        assertTrue(ClassUtil.getAllInterfaces(int.class).isEmpty());
        // assertTrue(ClassUtil.getAllInterfaces(int[].class).isEmpty());
        assertFalse(ClassUtil.getAllInterfaces(Integer.class).contains(Number.class));
        assertTrue(ClassUtil.getAllInterfaces(Integer.class).contains(Comparable.class));

        List<Class<?>> list = ClassUtil.getAllInterfaces(Integer[].class);
        assertTrue(list.contains(Cloneable.class));
        assertTrue(list.contains(Serializable.class));

    }
    
    @Test
    public void getAllSuperclasses() {
        assertEquals(null, ClassUtil.getAllSuperclasses(null));
        assertTrue(ClassUtil.getAllSuperclasses(Object.class).isEmpty());

        assertTrue(ClassUtil.getAllSuperclasses(String.class).isEmpty());

        List<Class<?>> supers = ClassUtil.getAllSuperclasses(ArrayList.class);
        assertTrue(supers.contains(AbstractList.class));
        assertTrue(supers.contains(AbstractCollection.class));

        assertFalse(supers.contains(List.class));
        assertFalse(supers.contains(Object.class));

        assertTrue(ClassUtil.getAllSuperclasses(int.class).isEmpty());
        assertTrue(ClassUtil.getAllSuperclasses(int[].class).isEmpty());
        assertTrue(ClassUtil.getAllSuperclasses(Integer.class).contains(Number.class));

        assertTrue(ClassUtil.getAllSuperclasses(Integer[].class).isEmpty());

    }    

}
