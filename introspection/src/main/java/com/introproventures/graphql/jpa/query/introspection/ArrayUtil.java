package com.introproventures.graphql.jpa.query.introspection;

import java.lang.reflect.Array;

public class ArrayUtil {

    private static final int INDEX_NOT_FOUND = -1;

    public static boolean isEmpty(Object array) {
        if(array == null) {
            return true;
        }
        
        // not an array
        if(!array.getClass().isArray()) {
            return false;
        }
        
        // check array length
        return Array.getLength(array) == 0;
    }

    public static boolean isNotEmpty(Object array) {
        return !isEmpty(array);
    }
    
    public static int indexOf(Object[] array, Object objectToFind) {
        return indexOf(array, objectToFind, 0);
    }
    
    public static int indexOf(Object[] array, Object objectToFind, int startIndex) {
        if (array == null) {
            return INDEX_NOT_FOUND;
        }

        if (startIndex < 0) {
            startIndex = 0;
        }

        if (objectToFind == null) {
            for (int i = startIndex; i < array.length; i++) {
                if (array[i] == null) {
                    return i;
                }
            }
        } else {
            for (int i = startIndex; i < array.length; i++) {
                if (objectToFind.equals(array[i])) {
                    return i;
                }
            }
        }

        return INDEX_NOT_FOUND;
    }

    public static <T> T[] addAll(T[] array1, T[] array2) {
        if (array1 == null) {
            return (T[]) clone(array2);
        } else if (array2 == null) {
            return (T[]) clone(array1);
        }
        @SuppressWarnings("unchecked")
        T[] joinedArray = (T[]) Array.newInstance(array1.getClass().getComponentType(), array1.length + array2.length);
        System.arraycopy(array1, 0, joinedArray, 0, array1.length);
        try {
            System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        } catch (ArrayStoreException ase) {
            // Check if problem was due to incompatible types
            /*
             * We do this here, rather than before the copy because: - it would be a wasted check most of the time -
             * safer, in case check turns out to be too strict
             */
            final Class<?> type1 = array1.getClass().getComponentType();
            final Class<?> type2 = array2.getClass().getComponentType();
            if (!type1.isAssignableFrom(type2)) {
                throw new IllegalArgumentException("Cannot store " + type2.getName() + " in an array of "
                        + type1.getName());
            }
            throw ase; // No, so rethrow original
        }
        return joinedArray;
    }

    public static <T> T[] clone(T[] array) {
        if (array == null) {
            return null;
        }

        return (T[]) array.clone();
    }
    
}
