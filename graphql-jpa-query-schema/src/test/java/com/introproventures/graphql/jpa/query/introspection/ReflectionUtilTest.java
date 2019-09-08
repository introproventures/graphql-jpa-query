package com.introproventures.graphql.jpa.query.introspection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import javax.management.loading.MLet;

import org.junit.Test;

import lombok.Data;


public class ReflectionUtilTest {

    @Test
    public void getAllMethodsOfClass() {
        assertNull(ReflectionUtil.getAllMethodsOfClass(null));

        Method[] methods = ReflectionUtil.getAllMethodsOfClass(MLet.class);
        assertTrue(methods.length > 0);

        Method equalsMethod = ReflectionUtil.getMethod(Object.class, "equals", Object.class);

        assertTrue(methods.length > 0);
        List<Method> methodList = Arrays.asList(methods);

        assertFalse(methodList.contains(equalsMethod));

        List<Class<?>> list = ClassUtil.getAllInterfaces(MLet.class);

        int interMethodLength = 0;
        for (Class<?> clazz : list) {
            Method[] interMethods = ReflectionUtil.getAllMethodsOfClass(clazz);
            interMethodLength += interMethods.length;
        }

        assertTrue(methods.length > interMethodLength);    
    }

    @Test
    public void getAllFieldsOfClass() {
        assertNull(ReflectionUtil.getAllFieldsOfClass(null));
        assertNull(ReflectionUtil.getAllFieldsOfClass(Object.class));

        assertEquals(0, ReflectionUtil.getAllFieldsOfClass(List.class).length);
        Field[] fields = ReflectionUtil.getAllFieldsOfClass(String.class);
        assertTrue(fields.length > 0);

        Field[] instancefields = ReflectionUtil.getAllInstanceFields(String.class);
        assertTrue(instancefields.length > 0);

        assertTrue(fields.length - instancefields.length > 0); 
    }
    
    @Test
    public void getComponentType() throws Exception {
        Field f1 = BaseClass.class.getField("f1");
        Field f5 = ConcreteClass.class.getField("f5");

        assertNull(ReflectionUtil.getComponentType(f1.getGenericType()));
        assertEquals(Long.class, ReflectionUtil.getComponentType(f5.getGenericType()));
    }
    
    @Test
    public void getAnnotationMethods() {
        assertNull(ReflectionUtil.getAnnotationMethods((Class<?>) null, (Class<? extends Annotation>) null));

        assertNull(ReflectionUtil.getAnnotationMethods((Class<?>) null, AnnotationClass.TestAnnotation.class));
        assertNull(ReflectionUtil.getAnnotationMethods(AnnotationClass.class, (Class<? extends Annotation>) null));

        List<Method> list =
                ReflectionUtil.getAnnotationMethods(AnnotationClass.class, AnnotationClass.TestAnnotation.class);

        assertTrue(list.size() == 8);

        list = ReflectionUtil.getAnnotationMethods(AnnotationClass.class, Test.class);

        assertTrue(list.size() == 0);
    }
    
    @Test
    public void getAnnotationFields() {
        assertNull(ReflectionUtil.getAnnotationFields((Class<?>) null, (Class<? extends Annotation>) null));

        assertNull(ReflectionUtil.getAnnotationFields((Class<?>) null, AnnotationClass.TestAnnotation.class));
        assertNull(ReflectionUtil.getAnnotationFields(AnnotationClass.class, (Class<? extends Annotation>) null));

        Field[] fields =
                ReflectionUtil.getAnnotationFields(AnnotationClass.class, AnnotationClass.TestAnnotation.class);

        assertTrue(fields.length == 2);

        fields = ReflectionUtil.getAnnotationFields(AnnotationClass.class, Test.class);

        assertTrue(ArrayUtil.isEmpty(fields));

    }    
    
    @Test
    public void getGenericSuperType() throws Exception {
        Class<?>[] genericSupertypes = ReflectionUtil.getGenericSuperTypes(ConcreteClass.class);
        assertEquals(String.class, genericSupertypes[0]);
        assertEquals(Integer.class, genericSupertypes[1]);
    }    
    
    @Test
    public void getRawType() throws Exception {
        Field f1 = BaseClass.class.getField("f1");
        Field f2 = BaseClass.class.getField("f2");
        Field f3 = BaseClass.class.getField("f3");
        Field f4 = ConcreteClass.class.getField("f4");
        Field f5 = ConcreteClass.class.getField("f5");
        Field array1 = BaseClass.class.getField("array1");

        assertEquals(String.class, ReflectionUtil.getRawType(f1.getGenericType(), ConcreteClass.class));
        assertEquals(Integer.class, ReflectionUtil.getRawType(f2.getGenericType(), ConcreteClass.class));
        assertEquals(String.class, ReflectionUtil.getRawType(f3.getGenericType(), ConcreteClass.class));
        assertEquals(Long.class, ReflectionUtil.getRawType(f4.getGenericType(), ConcreteClass.class));
        assertEquals(List.class, ReflectionUtil.getRawType(f5.getGenericType(), ConcreteClass.class));
        assertEquals(String[].class, ReflectionUtil.getRawType(array1.getGenericType(), ConcreteClass.class));

        assertEquals(Object.class, ReflectionUtil.getRawType(f1.getGenericType()));
    }
    
    @Test
    public void invokeMethod() {
        assertNull(ReflectionUtil.invokeMethod(null, (Object) null));
        assertNull(ReflectionUtil.invokeMethod(null, new Object(), new Object()));
        assertNull(ReflectionUtil.invokeMethod(null, new Object()));

        assertNull(ReflectionUtil.invokeMethod((Object) null, (String) null, (Object[]) null));
        assertNull(ReflectionUtil.invokeMethod((Object) null, (String) null, (Class<?>[]) null, (Object) null));
        assertNull(ReflectionUtil.invokeMethod("", (String) null, (Class<?>[]) null, (Object) null));
        assertNull(ReflectionUtil.invokeMethod((Object) null, (String) null, (Class<?>[]) null, new Object[] {}));
        assertNull(ReflectionUtil.invokeMethod((Object) null, (String) null, new Class[0], (Object[]) null));

        assertNull(ReflectionUtil.invokeMethod(null, new Object(), new Object()));
        assertNull(ReflectionUtil.invokeMethod(null, new Object()));

        Method method = null;
        try {
            method = String.class.getMethod("valueOf", int.class);
            assertEquals("1", ReflectionUtil.invokeMethod(method, (Object) null, 1));
            assertEquals("1", ReflectionUtil.invokeMethod(method, (Object) "", 1));
            assertEquals("1", ReflectionUtil.invokeMethod(method, new Object(), 1));

            method = String.class.getMethod("trim");
            assertEquals("xxx", ReflectionUtil.invokeMethod(method, (Object) " xxx "));
            assertEquals("xxx", ReflectionUtil.invokeMethod(method, new Object()));

        } catch (Exception e) {
            assertTrue(e instanceof RuntimeException);
        }

        List<String> list = new ArrayList<>();

        try {
            method = ArrayList.class.getDeclaredMethod("RangeCheck", int.class);
            ReflectionUtil.invokeMethod(method, list, Integer.MAX_VALUE);
        } catch (Exception e) {
            InvocationTargetException ex = (InvocationTargetException) e.getCause();

            if (ex != null) {
                assertTrue(ex.getTargetException() instanceof IndexOutOfBoundsException);
            }
        }

        try {

            assertEquals("xxx", ReflectionUtil.invokeMethod(" xxx ", "trim", null, (Object[]) null));
            assertEquals("xxx", ReflectionUtil.invokeMethod(new Object(), "trim", null, (Object[]) null));

        } catch (Exception e) {
            assertTrue(e instanceof RuntimeException);
        }

        list = new ArrayList<>();

        try {
            ReflectionUtil.invokeMethod(list, "RangeCheck", new Class<?>[] { int.class }, Integer.MAX_VALUE);
        } catch (Exception e) {

            if (e.getCause() instanceof NoSuchMethodException) {

            } else {

                InvocationTargetException ex = (InvocationTargetException) e.getCause();

                assertTrue(ex.getTargetException() instanceof IndexOutOfBoundsException);
            }
        }

    }    
    
    @Test
    public void testReadField() {
        // given
        FooClass foo = new FooClass("foo");
        
        // when
        String result = ReflectionUtil.readField(foo, "bar");
        
        // then
        assertThat(result).isEqualTo("foo");
        
    }
    
    @Test(expected = NoSuchElementException.class)
    public void testReadFieldNoSuchElement() {
        // given
        FooClass foo = new FooClass("foo");
        
        // when
        String result = ReflectionUtil.readField(foo, "foo");
        
    }
    
    
    
    @Data
    static class FooClass {
        private final String bar;
    }
    
    static class BaseClass<A, B> {
        public A f1;
        public B f2;
        public String f3;
        public A[] array1;
    }

    static class ConcreteClass extends BaseClass<String, Integer> {
        public Long f4;
        public List<Long> f5;
    }

    static class BaseClass2<X> extends BaseClass<X, Integer> {
    }

    static class ConcreteClass2 extends BaseClass2<String> {
    }

    static class Soo {
        public List<String> stringList;
        public String[] strings;
        public String string;

        public List<Integer> getIntegerList() {
            return null;
        }

        public Integer[] getIntegers() {
            return null;
        }

        public Integer getInteger() {
            return null;
        }

        public <T> T getTemplate(T foo) {
            return null;
        }

        public Collection<? extends Number> getCollection() {
            return null;
        }

        public Collection<?> getCollection2() {
            return null;
        }
    }    
    
    interface SomeGuy {
    }

    interface Cool extends SomeGuy {
    }

    interface Vigilante {
    }

    interface Flying extends Vigilante {
    }

    interface SuperMario extends Flying, Cool {
    };

    class User implements SomeGuy {
    }

    class SuperUser extends User implements Cool {
    }

    class SuperMan extends SuperUser implements Flying {
    }
    
    static class AnnotationClass {

        private int x;
        @TestAnnotation(value = "y")
        private int y;

        private String z;
        @TestAnnotation(value = "d")
        private Date d;

        public int getX() {
            return x;
        }

        @TestAnnotation(value = "setX")
        public void setX(int x) {
            this.x = x;
        }

        @TestAnnotation(value = "getY")
        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        @TestAnnotation(value = "getZ")
        public String getZ() {
            return z;
        }

        @TestAnnotation(value = "setZ")
        public void setZ(String z) {
            this.z = z;
        }

        @TestAnnotation(value = "getD")
        public Date getD() {
            return d;
        }

        @TestAnnotation(value = "setD")
        public void setD(Date d) {
            this.d = d;
        }

        @Override
        @TestAnnotation(value = "toString")
        public String toString() {
            return super.toString();
        }

        @Override
        @TestAnnotation(value = "clone")
        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

        @Retention(RetentionPolicy.RUNTIME)
        public @interface TestAnnotation {
            String value();
        }

    }    
}
