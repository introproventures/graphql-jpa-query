package com.introproventures.graphql.jpa.query.introspection;


import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

public class Properties {

    protected final ClassDescriptor classDescriptor;
    protected final Map<String, PropertyDescriptor> propertyDescriptors;

    // cache
    private PropertyDescriptor[] allProperties;

    public Properties(ClassDescriptor classDescriptor) {
        this.classDescriptor = classDescriptor;
        this.propertyDescriptors = inspectProperties();
    }

    protected Map<String, PropertyDescriptor> inspectProperties() {
        boolean scanAccessible = classDescriptor.isScanAccessible();
        Class<?> type = classDescriptor.getType();

        Map<String, PropertyDescriptor> map = new LinkedHashMap<>();

        Method[] methods =
                scanAccessible ? ReflectionUtil.getAccessibleMethods(type) : ReflectionUtil.getAllMethodsOfClass(type);

        for (int iteration = 0; iteration < 2; iteration++) {
            // first find the getters, and then the setters!
            for (Method method : methods) {
                if (Modifier.isStatic(method.getModifiers())) {
                    continue; // ignore static methods
                }

                boolean add = false;
                boolean issetter = false;

                String propertyName;

                if (iteration == 0) {
                    propertyName = BeanUtil.getBeanGetterName(method);
                    if (propertyName != null) {
                        add = true;
                        issetter = false;
                    }
                } else {
                    propertyName = BeanUtil.getBeanSetterName(method);
                    if (propertyName != null) {
                        add = true;
                        issetter = true;
                    }
                }

                if (add == true) {
                    MethodDescriptor methodDescriptor =
                            classDescriptor.getMethodDescriptor(method.getName(), method.getParameterTypes(), true);
                    addProperty(map, propertyName, methodDescriptor, issetter);
                }
            }
        }

        if (classDescriptor.isIncludeFieldsAsProperties()) {
            FieldDescriptor[] fieldDescriptors = classDescriptor.getAllFieldDescriptors();
            String prefix = classDescriptor.getPropertyFieldPrefix();

            for (FieldDescriptor fieldDescriptor : fieldDescriptors) {
                String name = fieldDescriptor.getField().getName();

                if (prefix != null) {
                    if (!name.startsWith(prefix)) {
                        continue;
                    }
                    name = name.substring(prefix.length());
                }

                if (!map.containsKey(name)) {
                    // add missing field as a potential property
                    map.put(name, createPropertyDescriptor(name, fieldDescriptor));
                }
            }

        }

        return map;
    }

    protected void addProperty(Map<String, PropertyDescriptor> map, String name, MethodDescriptor methodDescriptor,
            boolean isSetter) {
        MethodDescriptor setterMethod = isSetter ? methodDescriptor : null;
        MethodDescriptor getterMethod = isSetter ? null : methodDescriptor;

        PropertyDescriptor existing = map.get(name);

        if (existing == null) {
            // new property, just add it
            PropertyDescriptor propertyDescriptor = createPropertyDescriptor(name, getterMethod, setterMethod);

            map.put(name, propertyDescriptor);
            return;
        }

        if (!isSetter) {
            // use existing setter
            setterMethod = existing.getWriteMethodDescriptor();
            // check existing
            MethodDescriptor existingMethodDescriptor = existing.getReadMethodDescriptor();
            if (existingMethodDescriptor != null) {
                // check for special case of double get/is

                // getter with the same name already exist
                String methodName = methodDescriptor.getMethod().getName();
                String existingMethodName = existingMethodDescriptor.getMethod().getName();

                if (existingMethodName.startsWith(BeanUtil.METHOD_IS_PREFIX)
                        && methodName.startsWith(BeanUtil.METHOD_GET_PREFIX)) {
                    return;
                }
            }
        } else {
            // setter
            // use existing getter
            getterMethod = existing.getReadMethodDescriptor();

            if (getterMethod.getMethod().getReturnType() != setterMethod.getMethod().getParameterTypes()[0]) {
                return;
            }
        }

        PropertyDescriptor propertyDescriptor = createPropertyDescriptor(name, getterMethod, setterMethod);

        map.put(name, propertyDescriptor);
    }

    protected PropertyDescriptor createPropertyDescriptor(String name, MethodDescriptor getterMethod,
            MethodDescriptor setterMethod) {
        return new PropertyDescriptor(classDescriptor, name, getterMethod, setterMethod);
    }

    protected PropertyDescriptor createPropertyDescriptor(String name, FieldDescriptor fieldDescriptor) {
        return new PropertyDescriptor(classDescriptor, name, fieldDescriptor);
    }

    public PropertyDescriptor getPropertyDescriptor(String name) {
        return propertyDescriptors.get(name);
    }

    public PropertyDescriptor[] getAllPropertyDescriptors() {
        if (allProperties == null) {
            PropertyDescriptor[] allProperties = new PropertyDescriptor[propertyDescriptors.size()];

            int index = 0;
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors.values()) {
                allProperties[index] = propertyDescriptor;
                index++;
            }

            Arrays.sort(allProperties, new Comparator<PropertyDescriptor>() {
                @Override
                public int compare(PropertyDescriptor pd1, PropertyDescriptor pd2) {
                    return pd1.getName().compareTo(pd2.getName());
                }
            });

            this.allProperties = allProperties;
        }

        return allProperties;
    }

}