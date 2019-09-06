package com.introproventures.graphql.jpa.query.introspection;


import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Methods {

    protected final ClassDescriptor classDescriptor;
    protected final Map<String, MethodDescriptor[]> methodsMap;

    // cached
    private MethodDescriptor[] allMethods;

    public Methods(ClassDescriptor classDescriptor) {
        this.classDescriptor = classDescriptor;
        this.methodsMap = inspectMethods();
    }

    protected Map<String, MethodDescriptor[]> inspectMethods() {
        boolean scanAccessible = classDescriptor.isScanAccessible();
        Class<?> type = classDescriptor.getType();

        Method[] methods =
                scanAccessible ? ReflectionUtil.getAccessibleMethods(type) : ReflectionUtil.getAllMethodsOfClass(type);

        Map<String, MethodDescriptor[]> map = new LinkedHashMap<>(methods.length);

        for (Method method : methods) {
            
            if(Modifier.isStatic(method.getModifiers())) {
                continue;
            }
                
            String methodName = method.getName();

            MethodDescriptor[] mds = map.get(methodName);

            if (mds == null) {
                mds = new MethodDescriptor[1];
            } else {
                mds = Arrays.copyOf(mds, mds.length + 1);
            }

            map.put(methodName, mds);

            mds[mds.length - 1] = createMethodDescriptor(method);
        }

        return map;
    }

    protected MethodDescriptor createMethodDescriptor(Method method) {
        return new MethodDescriptor(classDescriptor, method);
    }

    public MethodDescriptor getMethodDescriptor(String name, Class<?>[] paramTypes) {
        MethodDescriptor[] methodDescriptors = methodsMap.get(name);
        if (methodDescriptors == null) {
            return null;
        }
        for (int i = 0; i < methodDescriptors.length; i++) {
            Method method = methodDescriptors[i].getMethod();
            if (ObjectUtil.isEquals(method.getParameterTypes(), paramTypes)) {
                return methodDescriptors[i];
            }
        }
        return null;
    }

    public MethodDescriptor getMethodDescriptor(String name) {
        MethodDescriptor[] methodDescriptors = methodsMap.get(name);
        if (methodDescriptors == null) {
            return null;
        }
        if (methodDescriptors.length != 1) {
            throw new IllegalArgumentException("Method name not unique: " + name);
        }
        return methodDescriptors[0];
    }

    public MethodDescriptor[] getAllMethodDescriptors(String name) {
        return methodsMap.get(name);
    }

    public MethodDescriptor[] getAllMethodDescriptors() {
        if (allMethods == null) {
            List<MethodDescriptor> allMethodsList = new ArrayList<>();

            for (MethodDescriptor[] methodDescriptors : methodsMap.values()) {
                Collections.addAll(allMethodsList, methodDescriptors);
            }

            MethodDescriptor[] allMethods = allMethodsList.toArray(new MethodDescriptor[allMethodsList.size()]);

            Arrays.sort(allMethods, new Comparator<MethodDescriptor>() {
                @Override
                public int compare(MethodDescriptor md1, MethodDescriptor md2) {
                    return md1.getMethod().getName().compareTo(md2.getMethod().getName());
                }
            });

            this.allMethods = allMethods;
        }
        return allMethods;
    }

}