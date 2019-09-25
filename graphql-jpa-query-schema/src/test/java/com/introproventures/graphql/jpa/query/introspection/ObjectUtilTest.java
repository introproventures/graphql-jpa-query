package com.introproventures.graphql.jpa.query.introspection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.junit.Test;


public class ObjectUtilTest {

    @Test
    public void testNull() {
        // all null
        Object[] NULL = null;

        // any null
        assertTrue(ObjectUtil.isAnyNull(NULL));
        assertTrue(ObjectUtil.isAnyNull(new Object[] { null, null, null, null, null }));
        assertTrue(ObjectUtil.isAnyNull(new Object[] { null, null, 0, null, null }));
        assertTrue(ObjectUtil.isAnyNull(new Object[] { null, "null", 0, null, null }));
        assertFalse(ObjectUtil
                .isAnyNull(new Object[] { "", "null", 0, new int[] {}, new ArrayList<>() }));
    }
    
    @Test
    public void isEquals() throws Exception {
        assertTrue(ObjectUtil.isEquals(null, null));
        assertFalse(ObjectUtil.isEquals(null, ""));
        assertFalse(ObjectUtil.isEquals("", null));
        assertTrue(ObjectUtil.isEquals("", ""));
        assertFalse(ObjectUtil.isEquals(Boolean.TRUE, null));
        assertFalse(ObjectUtil.isEquals(Boolean.TRUE, "true"));
        assertTrue(ObjectUtil.isEquals(Boolean.TRUE, Boolean.TRUE));
        assertFalse(ObjectUtil.isEquals(Boolean.TRUE, Boolean.FALSE));

        Object[] oa = new Object[] { new MyObject(), new MyObject() };
        int[] ia = new int[] { 1, 2, 3 };
        long[] la = new long[] { 1, 2, 3 };
        short[] sa = new short[] { 1, 2, 3 };
        byte[] ba = new byte[] { 1, 2, 3 };
        double[] da = new double[] { 1, 2, 3 };
        float[] fa = new float[] { 1, 2, 3 };
        boolean[] bla = new boolean[] { true, false, true };
        char[] ca = new char[] { 'a', 'b', 'c' };
        Object[] combo = { oa, ia, la, sa, ba, da, fa, bla, ca, null };

        assertObjectEquals(oa);
        assertObjectEquals(ia);
        assertObjectEquals(la);
        assertObjectEquals(sa);
        assertObjectEquals(ba);
        assertObjectEquals(da);
        assertObjectEquals(fa);
        assertObjectEquals(bla);
        assertObjectEquals(ca);
        assertObjectEquals(combo);
    }    
    
    private class MyObject {
        @Override
        public int hashCode() {
            return 123;
        }
    }
    
    private void assertObjectEquals(Object array) throws Exception {
        Method clone = Object.class.getDeclaredMethod("clone");
        clone.setAccessible(true);

        assertTrue(ObjectUtil.isEquals(array, array)); // same
        assertTrue(ObjectUtil.isEquals(array, clone.invoke(array))); // equals to copy

        Object copy = Array.newInstance(array.getClass().getComponentType(), Array.getLength(array) - 1);
        System.arraycopy(array, 0, copy, 0, Array.getLength(copy));

        assertFalse(ObjectUtil.isEquals(array, copy)); // not equals
    }
}
