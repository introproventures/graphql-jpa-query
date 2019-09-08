package com.introproventures.graphql.jpa.query.schema.impl;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.introproventures.graphql.jpa.query.introspection.ReflectionUtil;

public class Order<T> implements Comparator<T> {

    List<String> fields;
    
    public static <T> Order<T> by(String... fields) {
        return new Order<>(fields);
    }

    public static <T> Order<T> by(List<String> fields) {
        return new Order<>(fields);
    }
    
    Order(List<String> fields) {
        this.fields = fields;
    }

    Order(String... fields) {
        this.fields = Arrays.asList(fields);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public int compare(T target1, T target2) {
        int result = 0;
        for (String fieldName : fields) {
            if (result == 0) {
                Object value1 = ReflectionUtil.readField(target1, fieldName);
                Object value2 = ReflectionUtil.readField(target2, fieldName);
                
                if (value1 instanceof Comparable && value2 instanceof Comparable) {
                    Comparable comparable1 = (Comparable) value1;
                    Comparable comparable2 = (Comparable) value2;
                    
                    result = comparable1.compareTo(comparable2);
                } else {
                    throw new RuntimeException("Cannot compare non Comparable fields. " + 
                                               value1.getClass().getName() + 
                                               " must implement Comparable<" + 
                                               value1.getClass().getName() + ">");
                }
            } else {
                break;
            }
        }
        return result;
    }
}
