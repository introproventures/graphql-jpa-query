package com.introproventures.graphql.jpa.query.introspection;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.junit.Test;


public class ClassIntrospectorTest {

    private static ClassIntrospector introspector = ClassIntrospector.builder()
                                                                     .withEnhancedProperties(true)
                                                                     .withScanAccessible(true)
                                                                     .withIncludeFieldsAsProperties(true)
                                                                     .build();
    
    @Test
    public void testBasic() {
        ClassDescriptor cd = introspector.introspect(BeanSampleA.class);
        assertNotNull(cd);
        PropertyDescriptor[] properties = cd.getAllPropertyDescriptors();
        int c = 0;
        for (PropertyDescriptor property : properties) {
            if (property.isFieldOnlyDescriptor())
                continue;
            if (property.isPublic())
                c++;
        }
        assertEquals(2, c);

        Arrays.sort(properties, new Comparator<PropertyDescriptor>() {
            @Override
            public int compare(PropertyDescriptor o1, PropertyDescriptor o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        PropertyDescriptor pd = properties[0];
        assertEquals("fooProp", pd.getName());
        assertNotNull(pd.getReadMethodDescriptor());
        assertNotNull(pd.getWriteMethodDescriptor());
        assertNotNull(pd.getFieldDescriptor());

        pd = properties[1];
        assertEquals("shared", pd.getName());
        assertNull(pd.getReadMethodDescriptor());
        assertNull(pd.getWriteMethodDescriptor());
        assertNotNull(pd.getFieldDescriptor());

        pd = properties[2];
        assertEquals("something", pd.getName());
        assertNotNull(pd.getReadMethodDescriptor());
        assertNull(pd.getWriteMethodDescriptor());
        assertNull(pd.getFieldDescriptor());

        assertNotNull(cd.getPropertyDescriptor("fooProp", false));
        assertNotNull(cd.getPropertyDescriptor("something", false));
        assertNull(cd.getPropertyDescriptor("FooProp", false));
        assertNull(cd.getPropertyDescriptor("Something", false));
        assertNull(cd.getPropertyDescriptor("notExisting", false));
    }
    
    @Test
    public void testExtends() {
        ClassDescriptor cd = introspector.introspect(BeanSampleB.class);
        assertNotNull(cd);

        PropertyDescriptor[] properties = cd.getAllPropertyDescriptors();
        int c = 0;
        for (PropertyDescriptor property : properties) {
            if (property.isFieldOnlyDescriptor())
                continue;
            if (property.isPublic())
                c++;
        }
        assertEquals(2, c);

        c = 0;
        for (PropertyDescriptor property : properties) {
            if (property.isFieldOnlyDescriptor())
                continue;
            c++;
        }
        assertEquals(3, c);
        assertEquals(4, properties.length);

        Arrays.sort(properties, new Comparator<PropertyDescriptor>() {
            @Override
            public int compare(PropertyDescriptor o1, PropertyDescriptor o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        PropertyDescriptor pd = properties[0];
        assertEquals("boo", pd.getName());
        assertNotNull(pd.getReadMethodDescriptor());
        assertNotNull(pd.getWriteMethodDescriptor());
        assertNotNull(pd.getFieldDescriptor());
        assertFalse(pd.isFieldOnlyDescriptor());

        pd = properties[1];
        assertEquals("fooProp", pd.getName());
        assertNotNull(pd.getReadMethodDescriptor());
        assertNotNull(pd.getWriteMethodDescriptor());
        assertNull(pd.getFieldDescriptor()); // null since field is not visible
        assertFalse(pd.isFieldOnlyDescriptor());

        pd = properties[2];
        assertEquals("shared", pd.getName());
        assertNull(pd.getReadMethodDescriptor());
        assertNull(pd.getWriteMethodDescriptor());
        assertNotNull(pd.getFieldDescriptor());
        assertTrue(pd.isFieldOnlyDescriptor());

        pd = properties[3];
        assertEquals("something", pd.getName());
        assertNotNull(pd.getReadMethodDescriptor());
        assertNull(pd.getWriteMethodDescriptor());
        assertNull(pd.getFieldDescriptor());
        assertFalse(pd.isFieldOnlyDescriptor());

        assertNotNull(cd.getPropertyDescriptor("fooProp", false));
        assertNotNull(cd.getPropertyDescriptor("something", false));
        assertNull(cd.getPropertyDescriptor("FooProp", false));
        assertNull(cd.getPropertyDescriptor("Something", false));
        assertNull(cd.getPropertyDescriptor("notExisting", false));

        assertNotNull(cd.getPropertyDescriptor("boo", true));
        assertNull(cd.getPropertyDescriptor("boo", false));
    }
    
    @Test
    public void testCtors() {
        ClassDescriptor cd = introspector.introspect(Parent.class);
        ConstructorDescriptor[] ctors = cd.getAllConstructorDescriptors();
        int c = 0;
        for (ConstructorDescriptor ctor : ctors) {
            if (ctor.isPublic())
                c++;
        }
        assertEquals(1, c);
        ctors = cd.getAllConstructorDescriptors();
        assertEquals(2, ctors.length);
        assertNotNull(cd.getDefaultCtorDescriptor(true));
        assertNull(cd.getDefaultCtorDescriptor(false));

        Constructor<?> ctor = cd.getConstructorDescriptor(new Class[] { Integer.class }, true).getConstructor();
        assertNotNull(ctor);

        cd =  introspector.introspect(Child.class);
        ctors = cd.getAllConstructorDescriptors();
        c = 0;
        for (ConstructorDescriptor ccc : ctors) {
            if (ccc.isPublic())
                c++;
        }
        assertEquals(1, c);

        ctors = cd.getAllConstructorDescriptors();
        assertEquals(1, ctors.length);
        assertNull(cd.getDefaultCtorDescriptor(false));
        assertNull(cd.getDefaultCtorDescriptor(true));

        ConstructorDescriptor ctorDescriptor = cd.getConstructorDescriptor(new Class[] { Integer.class }, true);
        assertNull(ctorDescriptor);
        ctor = cd.getConstructorDescriptor(new Class[] { String.class }, true).getConstructor();
        assertNotNull(ctor);
    }
    
    @Test
    public void testSameFieldDifferentClass() {
        ClassDescriptor cd = introspector.introspect(BeanSampleA.class);

        FieldDescriptor fd = cd.getFieldDescriptor("shared", false);
        assertNull(fd);

        fd = cd.getFieldDescriptor("shared", true);
        assertNotNull(fd);

        ClassDescriptor cd2 = introspector.introspect(BeanSampleB.class);
        FieldDescriptor fd2 = cd2.getFieldDescriptor("shared", true);

        assertNotEquals(fd, fd2);
        assertEquals(fd.getField(), fd2.getField());
    }
    
    @Test
    public void testPropertyMatches() {
        ClassDescriptor cd = introspector.introspect(BeanSampleC.class);

        PropertyDescriptor pd;

        pd = cd.getPropertyDescriptor("s1", false);
        assertNull(pd);

        pd = cd.getPropertyDescriptor("s1", true);
        assertFalse(pd.isPublic());
        assertTrue(pd.getReadMethodDescriptor().isPublic());
        assertFalse(pd.getWriteMethodDescriptor().isPublic());

        assertNotNull(getPropertyGetterDescriptor(cd, "s1", false));
        assertNull(getPropertySetterDescriptor(cd, "s1", false));

        pd = cd.getPropertyDescriptor("s2", false);
        assertNull(pd);

        pd = cd.getPropertyDescriptor("s2", true);
        assertFalse(pd.isPublic());
        assertFalse(pd.getReadMethodDescriptor().isPublic());
        assertTrue(pd.getWriteMethodDescriptor().isPublic());

        assertNull(getPropertyGetterDescriptor(cd, "s2", false));
        assertNotNull(getPropertySetterDescriptor(cd, "s2", false));

        pd = cd.getPropertyDescriptor("s3", false);
        assertNotNull(pd);

        pd = cd.getPropertyDescriptor("s3", true);
        assertTrue(pd.isPublic());
        assertTrue(pd.getReadMethodDescriptor().isPublic());
        assertTrue(pd.getWriteMethodDescriptor().isPublic());

        assertNotNull(getPropertyGetterDescriptor(cd, "s3", false));
        assertNotNull(getPropertySetterDescriptor(cd, "s3", false));
    }
    
    @Test
    public void testOverload() {
        ClassDescriptor cd = introspector.introspect(Overload.class);

        PropertyDescriptor[] pds = cd.getAllPropertyDescriptors();

        assertEquals(1, pds.length);

        PropertyDescriptor pd = pds[0];

        assertNotNull(pd.getFieldDescriptor());
        assertNotNull(pd.getReadMethodDescriptor());
        assertNull(pd.getWriteMethodDescriptor());
    }

    @Test
    public void testSerialUid() {
        ClassDescriptor cd = introspector.introspect(BeanSampleB.class);

        assertNull(cd.getFieldDescriptor("serialVersionUID", true));
    }
    
    @Test
    public void testFields() throws NoSuchFieldException {
        ClassDescriptor cd = introspector.introspect(MethodParameterType.class);

        assertEquals(MethodParameterType.class, cd.getType());
        assertEquals(4, cd.getAllFieldDescriptors().length);

        FieldDescriptor[] fs = cd.getAllFieldDescriptors();
        int p = 0;
        for (FieldDescriptor f : fs) {
            if (f.isPublic()) {
                p++;
            }
        }
        assertEquals(0, p);

        FieldDescriptor fd = cd.getFieldDescriptor("f", true);
        FieldDescriptor fd2 = cd.getFieldDescriptor("f2", true);
        FieldDescriptor fd3 = cd.getFieldDescriptor("f3", true);
        FieldDescriptor fd4 = cd.getFieldDescriptor("f4", true);

        assertEquals(List.class, fd.getRawType());
        assertEquals(Object.class, fd.getRawComponentType());

        assertEquals(List.class, fd2.getRawType());
        assertEquals(Object.class, fd2.getRawComponentType());

        assertEquals(Map.class, fd3.getRawType());
        assertEquals(Object.class, fd3.getRawComponentType());

        assertEquals(List.class, fd4.getRawType());
        assertEquals(Long.class, fd4.getRawComponentType());

        // impl
        cd = introspector.introspect(Foo.class);

        fd = cd.getFieldDescriptor("f", true);
        fd2 = cd.getFieldDescriptor("f2", true);
        fd3 = cd.getFieldDescriptor("f3", true);

        assertEquals(List.class, fd.getRawType());
        assertEquals(Integer.class, fd.getRawComponentType());

        assertEquals(List.class, fd2.getRawType());
        assertEquals(Object.class, fd2.getRawComponentType());

        assertEquals(Map.class, fd3.getRawType());
        assertEquals(Integer.class, fd3.getRawComponentType());
        assertEquals(String.class, ReflectionUtil.getComponentTypes(fd3.getField().getGenericType(), cd.getType())[0]);
    }
    
    @Test
    public void testMethods() throws NoSuchMethodException {
        ClassDescriptor cd = introspector.introspect(MethodParameterType.class);

        assertEquals(MethodParameterType.class, cd.getType());
        assertEquals(5, cd.getAllMethodDescriptors().length);

        MethodDescriptor[] mds = cd.getAllMethodDescriptors();
        int mc = 0;
        for (MethodDescriptor md : mds) {
            if (md.isPublic())
                mc++;
        }
        assertEquals(0, mc);

        Class<?>[] params = new Class[] { Object.class, String.class, List.class, List.class, List.class };

        Method m = MethodParameterType.class.getDeclaredMethod("m", params);
        assertNotNull(m);

        Method m2 = cd.getMethodDescriptor("m", params, true).getMethod();
        assertNotNull(m2);
        assertEquals(m, m2);

        MethodDescriptor md1 = cd.getMethodDescriptor("m", params, true);
        assertNotNull(md1);
        assertEquals(m, md1.getMethod());
        assertArrayEquals(params, md1.getRawParameterTypes());
        assertEquals(void.class, md1.getRawReturnType());
        assertNull(md1.getRawReturnComponentType());

        MethodDescriptor md2 = cd.getMethodDescriptor("m2", params, true);
        assertNotNull(md2);
        assertArrayEquals(params, md2.getRawParameterTypes());
        assertEquals(List.class, md2.getRawReturnType());
        assertEquals(List.class, md2.getRawReturnComponentType());

        MethodDescriptor md3 = cd.getMethodDescriptor("m3", params, true);
        assertNotNull(md3);
        assertArrayEquals(params, md3.getRawParameterTypes());
        assertEquals(List.class, md3.getRawReturnType());
        assertEquals(Object.class, md3.getRawReturnComponentType());

        MethodDescriptor md4 = cd.getMethodDescriptor("m4", new Class[] { List.class }, true);
        assertNotNull(md4);
        assertArrayEquals(new Class[] { List.class }, md4.getRawParameterTypes());
        assertEquals(List.class, md4.getRawReturnType());
        assertEquals(Byte.class, md4.getRawReturnComponentType());
        assertEquals(List.class, md4.getSetterRawType());
        assertEquals(Long.class, md4.getSetterRawComponentType());

        MethodDescriptor md5 = cd.getMethodDescriptor("m5", new Class[] { List.class }, true);
        assertNotNull(md5);
        assertArrayEquals(new Class[] { List.class }, md5.getRawParameterTypes());
        assertEquals(List.class, md5.getRawReturnType());
        assertEquals(Object.class, md5.getRawReturnComponentType());
        assertEquals(List.class, md5.getSetterRawType());
        assertEquals(Object.class, md5.getSetterRawComponentType());

        Class<?>[] params2 = new Class[] { Integer.class, String.class, List.class, List.class, List.class };

        ClassDescriptor cd1 = introspector.introspect(Foo.class);

        //assertEquals(0, Foo.class.getDeclaredMethods().length);

        MethodDescriptor[] allm = cd1.getAllMethodDescriptors();

        assertEquals(5, allm.length);

        md3 = cd1.getMethodDescriptor("m", params, true);
        assertNotNull(md3);

        assertArrayEquals(params2, md3.getRawParameterTypes());

        md3 = cd1.getMethodDescriptor("m3", params, true);
        assertNotNull(md3);
        assertArrayEquals(params2, md3.getRawParameterTypes());
        assertEquals(List.class, md3.getRawReturnType());
        assertEquals(Integer.class, md3.getRawReturnComponentType());

        md5 = cd1.getMethodDescriptor("m5", new Class[] { List.class }, true);
        assertNotNull(md5);
        assertArrayEquals(new Class[] { List.class }, md5.getRawParameterTypes());
        assertEquals(List.class, md5.getRawReturnType());
        assertEquals(Integer.class, md5.getRawReturnComponentType());
        assertEquals(List.class, md5.getSetterRawType());
        assertEquals(Integer.class, md5.getSetterRawComponentType());
    }    

    static class BeanSampleA {

        protected Integer shared;

        private String fooProp = "abean_value";

        public void setFooProp(String v) {
            fooProp = v;
        }

        public String getFooProp() {
            return fooProp;
        }

        public boolean isSomething() {
            return true;
        }
    }
    
    static class BeanSampleB extends BeanSampleA {

        public static final long serialVersionUID = 42L;

        private Long boo;

        Long getBoo() {
            return boo;
        }

        void setBoo(Long boo) {
            this.boo = boo;
        }
    }
    
    public class BeanSampleC {

        private String s1;
        private String s2;
        private String s3;

        public String getS1() {
            return s1;
        }

        protected void setS1(String s1) {
            this.s1 = s1;
        }

        protected String getS2() {
            return s2;
        }

        public void setS2(String s2) {
            this.s2 = s2;
        }

        public String getS3() {
            return s3;
        }

        public void setS3(String s3) {
            this.s3 = s3;
        }
    }    
    
    static class Parent {

        protected Parent() {

        }

        public Parent(Integer i) {

        }

    }
    
    static class Child extends Parent {

        public Child(String a) {
            super();
        }
    }
    
    static class Overload {

        String company;

        // not a property setter
        public void setCompany(StringBuilder sb) {
            this.company = sb.toString();
        }

        public String getCompany() {
            return company;
        }
    }
    
    static class MethodParameterType<A> {
        List<A> f;
        List<?> f2;
        Map<String, A> f3;
        List<Long> f4;

        <T extends List<T>> void m(A a, String p1, T p2, List<?> p3, List<T> p4) {
        }

        <T extends List<T>> List<T> m2(A a, String p1, T p2, List<?> p3, List<T> p4) {
            return null;
        }

        <T extends List<T>> List<A> m3(A a, String p1, T p2, List<?> p3, List<T> p4) {
            return null;
        }

        List<Byte> m4(List<Long> list) {
            return null;
        }

        List<A> m5(List<A> list) {
            return null;
        }
    }

    static class Foo extends MethodParameterType<Integer> {
    }    

    MethodDescriptor getPropertySetterDescriptor(ClassDescriptor cd, String name, boolean declared) {
        PropertyDescriptor propertyDescriptor = cd.getPropertyDescriptor(name, true);

        if (propertyDescriptor != null) {
            MethodDescriptor setter = propertyDescriptor.getWriteMethodDescriptor();

            if ((setter != null) && setter.matchDeclared(declared)) {
                return setter;
            }
        }
        return null;
    }

    MethodDescriptor getPropertyGetterDescriptor(ClassDescriptor cd, String name, boolean declared) {
        PropertyDescriptor propertyDescriptor = cd.getPropertyDescriptor(name, true);

        if (propertyDescriptor != null) {
            MethodDescriptor getter = propertyDescriptor.getReadMethodDescriptor();

            if ((getter != null) && getter.matchDeclared(declared)) {
                return getter;
            }
        }
        return null;
    }    
}
