package com.introproventures.graphql.jpa.query.introspection;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

public class ObjectUtil {

    public static boolean isAnyNull(Object... objects) {
        if(objects == null) {
            return true;
        }
        
        return Stream.of(objects).anyMatch(Objects::isNull);
    }
    
    public static boolean isEquals(Object object1, Object object2) {
        if (object1 == object2) {
            return true;
        }

        if (object1 == null || object2 == null) {
            return false;
        }

        if (!object1.getClass().equals(object2.getClass())) {
            return false;
        }

        if (object1 instanceof Object[]) {
            return Arrays.deepEquals((Object[]) object1, (Object[]) object2);
        }
        if (object1 instanceof int[]) {
            return Arrays.equals((int[]) object1, (int[]) object2);
        }
        if (object1 instanceof long[]) {
            return Arrays.equals((long[]) object1, (long[]) object2);
        }
        if (object1 instanceof short[]) {
            return Arrays.equals((short[]) object1, (short[]) object2);
        }
        if (object1 instanceof byte[]) {
            return Arrays.equals((byte[]) object1, (byte[]) object2);
        }
        if (object1 instanceof double[]) {
            return Arrays.equals((double[]) object1, (double[]) object2);
        }
        if (object1 instanceof float[]) {
            return Arrays.equals((float[]) object1, (float[]) object2);
        }
        if (object1 instanceof char[]) {
            return Arrays.equals((char[]) object1, (char[]) object2);
        }
        if (object1 instanceof boolean[]) {
            return Arrays.equals((boolean[]) object1, (boolean[]) object2);
        }
        return object1.equals(object2);
    }    

}
