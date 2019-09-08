package com.introproventures.graphql.jpa.query.introspection;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class ReflectionUtil {

    private static final Logger logger = LoggerFactory.getLogger(ReflectionUtil.class);

    public static Field[] getAllFieldsOfClass(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }

        return getAllFieldsOfClass0(clazz);
    }
    
    public static Method[] getAccessibleMethods(Class<?> clazz) {
        return getAccessibleMethods(clazz, Object.class);
    }

    public static Method[] getAllMethodsOfClass(final Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        Method[] methods = null;
        Class<?> itr = clazz;
        while (itr != null && !itr.equals(Object.class)) {
            methods = ArrayUtil.addAll(itr.getDeclaredMethods(), methods);
            itr = itr.getSuperclass();
        }
        return methods;
    }    
    
    public static Method[] getAccessibleMethods(Class<?> clazz, Class<?> limit) {
        Package topPackage = clazz.getPackage();
        List<Method> methodList = new ArrayList<>();
        int topPackageHash = (topPackage == null) ? 0 : topPackage.hashCode();
        boolean top = true;
        do {
            if (clazz == null) {
                break;
            }
            Method[] declaredMethods = clazz.getDeclaredMethods();
            for (Method method : declaredMethods) {
                if (Modifier.isVolatile(method.getModifiers())) {
                    continue;
                }
                if (top) {
                    methodList.add(method);
                    continue;
                }
                int modifier = method.getModifiers();
                if (Modifier.isPrivate(modifier) || Modifier.isAbstract(modifier)) {
                    continue;
                }

                if (Modifier.isPublic(modifier) || Modifier.isProtected(modifier)) {
                    addMethodIfNotExist(methodList, method);
                    continue;
                }
                // add super default methods from the same package
                Package pckg = method.getDeclaringClass().getPackage();
                int pckgHash = (pckg == null) ? 0 : pckg.hashCode();
                if (pckgHash == topPackageHash) {
                    addMethodIfNotExist(methodList, method);
                }
            }
            top = false;
        } while ((clazz = clazz.getSuperclass()) != limit);

        Method[] methods = new Method[methodList.size()];
        for (int i = 0; i < methods.length; i++) {
            methods[i] = methodList.get(i);
        }
        return methods;
    }

    private static void addMethodIfNotExist(List<Method> allMethods, Method newMethod) {
        for (Method method : allMethods) {
            if (ObjectUtil.isEquals(method, newMethod)) {
                return;
            }
        }

        allMethods.add(newMethod);
    }
    
    public static <T> T readField(Object target, String propertyName) { 
        Field field = Optional.ofNullable(getField(target.getClass(), 
                                                   propertyName))
                              .orElseThrow(() -> noSuchElementException(target.getClass(),  
                                                                        propertyName)); 
        try { 
            field.setAccessible(true); 
             
            return (T) field.get(target); 
             
        } catch (Exception ignored) { } 
               
        return null; 
    }     
    
    public static Field getField(Class<?> clazz, String fieldName) {
        if (ObjectUtil.isAnyNull(clazz, fieldName)) {
            return null;
        }

        return getField0(clazz, fieldName);
    }
    
    static Field getField0(Class<?> clazz, String fieldName) {
        for (Class<?> itr = clazz; hasSuperClass(itr);) {
            Field[] fields = itr.getDeclaredFields();
            for (Field field : fields) {
                if (field.getName().equals(fieldName)) {
                    return field;
                }
            }

            itr = itr.getSuperclass();
        }

        return null;
    }    
    public static Class<?> getComponentType(Type type, Class<?> implClass) {
        Class<?>[] componentTypes = getComponentTypes(type, implClass);
        if (componentTypes == null) {
            return null;
        }
        return componentTypes[componentTypes.length - 1];
    }
    
    public static Class<?> getComponentType(Type type) {
        return getComponentType(type, null);
    }    
    
    static Field[] getAllFieldsOfClass0(Class<?> clazz) {
        Field[] fields = null;

        for (Class<?> itr = clazz; hasSuperClass(itr);) {
            fields = ArrayUtil.addAll(itr.getDeclaredFields(), fields);
            itr = itr.getSuperclass();
        }

        return fields;
    }
    
    public static boolean hasSuperClass(Class<?> clazz) {
        return (clazz != null) && !clazz.equals(Object.class);
    }    
    
    public static Annotation[] getAnnotation(AnnotatedElement annotatedElement) {
        if (Objects.isNull(annotatedElement)) {
            return null;
        }

        return annotatedElement.getAnnotations();
    }
 
    public static boolean isPublic(Member m) {
        return m != null && Modifier.isPublic(m.getModifiers());
    }  
    
    public static boolean isAccessible(Member m) {
        return m != null && Modifier.isPublic(m.getModifiers());
    }
    
    public static void forceAccess(AccessibleObject object) {
        if (object == null || object.isAccessible()) {
            return;
        }
        try {
            object.setAccessible(true);
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static Class<?> getRawType(Type type) {
        return getRawType(type, null);
    }

    public static Class<?> getRawType(Type type, Class<?> implClass) {
        if (type == null) {
            return null;
        }

        GenericType gt = GenericType.find(type);
        if (gt != null) {
            return gt.toRawType(type, implClass);
        }

        return null;

    }
    
    public static Class<?>[] getComponentTypes(Type type, Class<?> implClass) {
        if (type == null) {
            return null;
        }

        GenericType gt = GenericType.find(type);
        if (gt != null) {
            return gt.getComponentTypes(type, implClass);
        }

        return null;

    }    
    
    public static Field[] getAccessibleFields(Class<?> clazz) {
        return getAccessibleFields(clazz, Object.class);
    }

    public static Field[] getAccessibleFields(Class<?> clazz, Class<?> limit) {
        if (clazz == null) {
            return null;
        }

        Package topPackage = clazz.getPackage();
        List<Field> fieldList = new ArrayList<>();
        int topPackageHash = (topPackage == null) ? 0 : topPackage.hashCode();
        boolean top = true;
        do {
            if (clazz == null) {
                break;
            }
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field field : declaredFields) {
                if (top == true) { // add all top declared fields
                    fieldList.add(field);
                    continue;
                }
                int modifier = field.getModifiers();
                if (Modifier.isPrivate(modifier)) {
                    continue;
                }
                if (Modifier.isPublic(modifier) || Modifier.isProtected(modifier)) {
                    addFieldIfNotExist(fieldList, field);
                    continue;
                }

                // add super default methods from the same package
                Package pckg = field.getDeclaringClass().getPackage();
                int pckgHash = (pckg == null) ? 0 : pckg.hashCode();
                if (pckgHash == topPackageHash) {
                    addFieldIfNotExist(fieldList, field);
                }
            }
            top = false;
        } while ((clazz = clazz.getSuperclass()) != limit);

        Field[] fields = new Field[fieldList.size()];
        for (int i = 0; i < fields.length; i++) {
            fields[i] = fieldList.get(i);
        }

        return fields;
    }    
    
    private static void addFieldIfNotExist(List<Field> allFields, Field newField) {
        for (Field field : allFields) {
            if (ObjectUtil.isEquals(field, newField)) {
                return;
            }
        }

        allFields.add(newField);
    }
    
    
    enum GenericType {

        CLASS_TYPE {

            @Override
            Class<?> type() {
                return Class.class;
            }

            @Override
            Class<?> toRawType(Type type, Class<?> implClass) {
                return (Class<?>) type;
            }

            @Override
            Class<?>[] getComponentTypes(Type type, Class<?> implClass) {
                Class<?> clazz = (Class<?>) type;
                if (clazz.isArray()) {
                    return new Class[] { clazz.getComponentType() };
                }
                return null;
            }
        },
        PARAMETERIZED_TYPE {

            @Override
            Class<?> type() {
                return ParameterizedType.class;
            }

            @Override
            Class<?> toRawType(Type type, Class<?> implClass) {
                ParameterizedType pType = (ParameterizedType) type;
                return getRawType(pType.getRawType(), implClass);
            }

            @Override
            Class<?>[] getComponentTypes(Type type, Class<?> implClass) {
                ParameterizedType pt = (ParameterizedType) type;

                Type[] generics = pt.getActualTypeArguments();

                if (generics.length == 0) {
                    return null;
                }

                Class<?>[] types = new Class[generics.length];

                for (int i = 0; i < generics.length; i++) {
                    types[i] = getRawType(generics[i], implClass);
                }
                return types;
            }
        },
        WILDCARD_TYPE {

            @Override
            Class<?> type() {
                return WildcardType.class;
            }

            @Override
            Class<?> toRawType(Type type, Class<?> implClass) {
                WildcardType wType = (WildcardType) type;

                Type[] lowerTypes = wType.getLowerBounds();
                if (lowerTypes.length > 0) {
                    return getRawType(lowerTypes[0], implClass);
                }

                Type[] upperTypes = wType.getUpperBounds();
                if (upperTypes.length != 0) {
                    return getRawType(upperTypes[0], implClass);
                }

                return Object.class;
            }

            @Override
            Class<?>[] getComponentTypes(Type type, Class<?> implClass) {
                return null;
            }
        },
        GENERIC_ARRAY_TYPE {

            @Override
            Class<?> type() {
                return GenericArrayType.class;
            }

            @Override
            Class<?> toRawType(Type type, Class<?> implClass) {
                Type genericComponentType = ((GenericArrayType) type).getGenericComponentType();
                Class<?> rawType = getRawType(genericComponentType, implClass);
                // FIXME
                return Array.newInstance(rawType, 0).getClass();
            }

            @Override
            Class<?>[] getComponentTypes(Type type, Class<?> implClass) {
                GenericArrayType gat = (GenericArrayType) type;

                Class<?> rawType = getRawType(gat.getGenericComponentType(), implClass);
                if (rawType == null) {
                    return null;
                }

                return new Class[] { rawType };
            }
        },
        TYPE_VARIABLE {

            @Override
            Class<?> type() {
                return TypeVariable.class;
            }

            @Override
            Class<?> toRawType(Type type, Class<?> implClass) {
                TypeVariable<?> varType = (TypeVariable<?>) type;
                if (implClass != null) {
                    Type resolvedType = resolveVariable(varType, implClass);
                    if (resolvedType != null) {
                        return getRawType(resolvedType, null);
                    }
                }
                Type[] boundsTypes = varType.getBounds();
                if (boundsTypes.length == 0) {
                    return Object.class;
                }
                return getRawType(boundsTypes[0], implClass);
            }

            @Override
            Class<?>[] getComponentTypes(Type type, Class<?> implClass) {
                return null;
            }
        };

        abstract Class<?> toRawType(Type type, Class<?> implClass);

        abstract Class<?> type();

        abstract Class<?>[] getComponentTypes(Type type, Class<?> implClass);

        static GenericType find(Type type) {
            for (GenericType gt : GenericType.values()) {
                if (gt.type().isInstance(type)) {
                    return gt;
                }
            }

            return null;
        }
    }
    
    public static Type resolveVariable(TypeVariable<?> variable, final Class<?> implClass) {
        final Class<?> rawType = getRawType(implClass, null);

        int index = ArrayUtil.indexOf(rawType.getTypeParameters(), variable);
        if (index >= 0) {
            return variable;
        }

        final Class<?>[] interfaces = rawType.getInterfaces();
        final Type[] genericInterfaces = rawType.getGenericInterfaces();

        for (int i = 0; i <= interfaces.length; i++) {
            Class<?> rawInterface;

            if (i < interfaces.length) {
                rawInterface = interfaces[i];
            } else {
                rawInterface = rawType.getSuperclass();
                if (rawInterface == null) {
                    continue;
                }
            }

            final Type resolved = resolveVariable(variable, rawInterface);
            if (resolved instanceof Class || resolved instanceof ParameterizedType) {
                return resolved;
            }

            if (resolved instanceof TypeVariable) {
                final TypeVariable<?> typeVariable = (TypeVariable<?>) resolved;
                index = ArrayUtil.indexOf(rawInterface.getTypeParameters(), typeVariable);

                if (index < 0) {
                    throw new IllegalArgumentException("Invalid type variable:" + typeVariable);
                }

                final Type type = i < genericInterfaces.length ? genericInterfaces[i] : rawType.getGenericSuperclass();

                if (type instanceof Class) {
                    return Object.class;
                }

                if (type instanceof ParameterizedType) {
                    return ((ParameterizedType) type).getActualTypeArguments()[index];
                }

                throw new IllegalArgumentException("Unsupported type: " + type);
            }
        }
        return null;
    }
    
    public static Method getMethod(Class<?> clazz, String methodName, Class<?>...parameterTypes) {
        if (clazz == null || methodName == null) {
            return null;
        }

        for (Class<?> itr = clazz; hasSuperClass(itr);) {
            Method[] methods = itr.getDeclaredMethods();

            for (Method method : methods) {
                if (method.getName().equals(methodName) && Arrays.equals(method.getParameterTypes(), parameterTypes)) {
                    return method;
                }
            }

            itr = itr.getSuperclass();
        }

        return null;

    }

    public static Field[] getAllInstanceFields(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }

        return getAllInstanceFields0(clazz);
    }
    
    static Field[] getAllInstanceFields0(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> itr = clazz; hasSuperClass(itr);) {
            for (Field field : itr.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    fields.add(field);
                }
            }
            itr = itr.getSuperclass();
        }

        return fields.toArray(new Field[fields.size()]);
    }

    public static <T, A extends Annotation> List<Method> getAnnotationMethods(Class<T> clazz, Class<A> annotationType) {
        if (clazz == null || annotationType == null) {
            return null;
        }
        List<Method> list = new ArrayList<>();

        for (Method method : getAllMethodsOfClass(clazz)) {
            A type = method.getAnnotation(annotationType);
            if (type != null) {
                list.add(method);
            }
        }

        return list;
    }

    public static Field[] getAnnotationFields(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        if (clazz == null || annotationClass == null) {
            return null;
        }

        Field[] fields = getAllFieldsOfClass0(clazz);
        if (ArrayUtil.isEmpty(fields)) {
            return null;
        }

        List<Field> list = new ArrayList<>();
        for (Field field : fields) {
            if (null != field.getAnnotation(annotationClass)) {
                list.add(field);
                field.setAccessible(true);
            }
        }

        return list.toArray(new Field[0]);
    }

    public static Class<?>[] getGenericSuperTypes(Class<?> type) {
        if (type == null) {
            return null;
        }

        return getComponentTypes(type.getGenericSuperclass());
    }
    
    public static Class<?>[] getComponentTypes(Type type) {
        return getComponentTypes(type, null);
    }
    
    public static <T> T invokeMethod(Method method, Object target, Object...args) {
        if (method == null) {
            return null;
        }

        method.setAccessible(true);
        try {
            @SuppressWarnings("unchecked")
            T result = (T) method.invoke(target, args);

            return result;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }
    
    public static Object invokeMethod(Object object, String methodName, Class<?>[] parameterTypes, Object...args) {
        if (object == null || methodName == null) {
            return null;
        }

        if (parameterTypes == null) {
            parameterTypes = new Class[0];
        }
        if (args == null) {
            args = new Object[0];
        }
        Method method;
        try {
            method = object.getClass().getDeclaredMethod(methodName, parameterTypes);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        if (method == null) {
            return null;
        }

        return invokeMethod(method, object, args);

    }
    
    public static Object invokeMethod(Object object, String methodName, Object...args) {
        if (object == null || methodName == null) {
            return null;
        }
        if (args == null) {
            args =  new Object[0];
        }

        int arguments = args.length;
        Class<?>[] parameterTypes = new Class[arguments];
        for (int i = 0; i < arguments; i++) {
            parameterTypes[i] = args[i].getClass();
        }

        return invokeMethod(object, methodName, parameterTypes, args);
    }
    
    private static NoSuchElementException noSuchElementException(Class<?> containerClass, 
                                                                 String propertyName) { 
        return new NoSuchElementException(String.format(Locale.ROOT, 
                                                        "Could not locate field name [%s] on class [%s]", 
                                                        propertyName, 
                                                        containerClass.getName())); 
         
    }     
}