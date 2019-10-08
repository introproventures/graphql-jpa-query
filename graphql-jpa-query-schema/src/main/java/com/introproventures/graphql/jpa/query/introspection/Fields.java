package com.introproventures.graphql.jpa.query.introspection;


import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

public class Fields {
    
    public static final String SERIAL_VERSION_UID = "serialVersionUID";

    protected final ClassDescriptor classDescriptor;
    protected final Map<String, FieldDescriptor> fieldsMap;

    // cache
    private FieldDescriptor[] allFields;

    public Fields(ClassDescriptor classDescriptor) {
        this.classDescriptor = classDescriptor;
        this.fieldsMap = inspectFields();
    }

    protected Map<String, FieldDescriptor> inspectFields() {
        boolean scanAccessible = classDescriptor.isScanAccessible();
        boolean scanStatics = classDescriptor.isScanStatics();
        Class<?> type = classDescriptor.getType();

        Field[] fields =
                scanAccessible ? ReflectionUtil.getAccessibleFields(type) : ReflectionUtil.getAllFieldsOfClass(type);

        Map<String, FieldDescriptor> map = new LinkedHashMap<>(fields.length);

        for (Field field : fields) {
            String fieldName = field.getName();

            if (fieldName.equals(SERIAL_VERSION_UID)) {
                continue;
            }

            if (!scanStatics && Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            
            map.put(fieldName, createFieldDescriptor(field));
        }

        return map;
    }

    protected FieldDescriptor createFieldDescriptor(Field field) {
        return new FieldDescriptor(classDescriptor, field);
    }

    public FieldDescriptor getFieldDescriptor(String name) {
        return fieldsMap.get(name);
    }

    public FieldDescriptor[] getAllFieldDescriptors() {
        if (allFields == null) {
            FieldDescriptor[] allFields = new FieldDescriptor[fieldsMap.size()];

            int index = 0;
            for (FieldDescriptor fieldDescriptor : fieldsMap.values()) {
                allFields[index] = fieldDescriptor;
                index++;
            }

            Arrays.sort(allFields, new Comparator<FieldDescriptor>() {
                @Override
                public int compare(FieldDescriptor fd1, FieldDescriptor fd2) {
                    return fd1.getField().getName().compareTo(fd2.getField().getName());
                }
            });

            this.allFields = allFields;
        }

        return allFields;
    }

}