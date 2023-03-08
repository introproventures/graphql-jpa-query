package com.introproventures.graphql.jpa.query.introspection;

import java.lang.reflect.Array;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

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

    private void assertTrue(boolean anyNull) {
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
        Integer[] ia = new Integer[] { 1, 2, 3 };
        Long[] la = new Long[] { 1L, 2L, 3L };
        Short[] sa = new Short[] { 1, 2, 3 };
        Byte[] ba = new Byte[] { 1, 2, 3 };
        Double[] da = new Double[] { 1.0, 2.0, 3.0 };
        Float[] fa = new Float[] { 1.0F, 2.0F, 3.0F };
        Boolean[] bla = new Boolean[] { true, false, true };
        Character[] ca = new Character[] { 'a', 'b', 'c' };
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
    
    private <T> void assertObjectEquals(T[] array) throws Exception {
        Object clone = array.clone();

        assertTrue(ObjectUtil.isEquals(array, array)); // same
        assertTrue(ObjectUtil.isEquals(array, clone)); // equals to copy

        Object copy = Array.newInstance(array.getClass().getComponentType(), Array.getLength(array) - 1);
        System.arraycopy(array, 0, copy, 0, Array.getLength(copy));

        assertFalse(ObjectUtil.isEquals(array, copy)); // not equals
    }
}
