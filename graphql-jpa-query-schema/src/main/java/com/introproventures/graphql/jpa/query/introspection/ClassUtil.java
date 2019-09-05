package com.introproventures.graphql.jpa.query.introspection;

import java.util.ArrayList;
import java.util.List;

public class ClassUtil {

    public static Class<?>[] getAllInterfacesAsArray(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }

        List<Class<?>> interfacesFound = new ArrayList<>();
        getAllInterfaces(clazz, interfacesFound);

        return interfacesFound.toArray(new Class<?>[0]);
    }
    
    private static void getAllInterfaces(Class<?> clazz, List<Class<?>> interfacesFound) {
        while (clazz != null) {
            Class<?>[] interfaces = clazz.getInterfaces();

            for (int i = 0; i < interfaces.length; i++) {
                if (!interfacesFound.contains(interfaces[i])) {
                    interfacesFound.add(interfaces[i]);
                    getAllInterfaces(interfaces[i], interfacesFound);
                }
            }

            clazz = clazz.getSuperclass();
        }
    }    
    
    public static List<Class<?>> getAllSuperclasses(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        List<Class<?>> classes = new ArrayList<>();
        Class<?> superclass = clazz.getSuperclass();
        while (superclass != null && superclass != Object.class) {
            classes.add(superclass);
            superclass = superclass.getSuperclass();
        }
        return classes;
    }

    public static Class<?>[] getAllSuperclassesAsArray(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        List<Class<?>> classes = new ArrayList<>();
        Class<?> superclass = clazz.getSuperclass();
        while (superclass != null && superclass != Object.class) {
            classes.add(superclass);
            superclass = superclass.getSuperclass();
        }
        return classes.toArray(new Class<?>[0]);
    }
    
}
