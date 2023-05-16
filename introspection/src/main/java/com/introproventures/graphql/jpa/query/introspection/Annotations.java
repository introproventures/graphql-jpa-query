package com.introproventures.graphql.jpa.query.introspection;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class Annotations {

    protected final AnnotatedElement annotatedElement;

    protected final Map<Class<? extends Annotation>, AnnotationDescriptor> annotationsMap;

    // cache
    private AnnotationDescriptor[] allAnnotations;

    public Annotations(AnnotatedElement annotatedElement) {
        this.annotatedElement = annotatedElement;
        this.annotationsMap = inspectAnnotations();
    }

    private Map<Class<? extends Annotation>, AnnotationDescriptor> inspectAnnotations() {
        Annotation[] annotations = ReflectionUtil.getAnnotation(annotatedElement);
        if (ArrayUtil.isEmpty(annotations)) {
            return null;
        }

        Map<Class<? extends Annotation>, AnnotationDescriptor> map = new LinkedHashMap<>(annotations.length);

        for (Annotation annotation : annotations) {
            map.put(annotation.annotationType(), new AnnotationDescriptor(annotation));
        }

        return map;
    }

    public AnnotationDescriptor getAnnotationDescriptor(Class<? extends Annotation> clazz) {
        if (annotationsMap == null) {
            return null;
        }

        return annotationsMap.get(clazz);
    }

    public AnnotationDescriptor[] getAllAnnotationDescriptors() {
        if (annotationsMap == null) {
            return null;
        }

        if (allAnnotations == null) {
            AnnotationDescriptor[] allAnnotations = new AnnotationDescriptor[annotationsMap.size()];

            int index = 0;
            for (AnnotationDescriptor annotationDescriptor : annotationsMap.values()) {
                allAnnotations[index] = annotationDescriptor;
                index++;
            }

            Arrays.sort(
                allAnnotations,
                new Comparator<AnnotationDescriptor>() {
                    @Override
                    public int compare(AnnotationDescriptor ad1, AnnotationDescriptor ad2) {
                        return ad1.getClass().getName().compareTo(ad2.getClass().getName());
                    }
                }
            );

            this.allAnnotations = allAnnotations;
        }

        return allAnnotations;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Annotations [annotatedElement=").append(annotatedElement).append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(annotatedElement);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Annotations other = (Annotations) obj;
        return Objects.equals(annotatedElement, other.annotatedElement);
    }
}
