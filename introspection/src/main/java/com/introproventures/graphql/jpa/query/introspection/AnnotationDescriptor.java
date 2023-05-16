package com.introproventures.graphql.jpa.query.introspection;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Objects;

public class AnnotationDescriptor {

    private final Annotation annotation;

    private final Class<? extends Annotation> annotationType;

    private final ElementType[] elementTypes;

    private final RetentionPolicy policy;

    private final boolean isDocumented;

    private final boolean isInherited;

    public <A extends Annotation> AnnotationDescriptor(A annotation) {
        this.annotation = annotation;
        annotationType = annotation.annotationType();

        Target target = annotationType.getAnnotation(Target.class);
        elementTypes = (target == null) ? ElementType.values() : target.value();

        Retention retention = annotationType.getAnnotation(Retention.class);
        policy = (retention == null) ? RetentionPolicy.CLASS : retention.value();

        Documented documented = annotationType.getAnnotation(Documented.class);
        isDocumented = (documented != null);

        Inherited inherited = annotationType.getAnnotation(Inherited.class);
        isInherited = (inherited != null);
    }

    @SuppressWarnings("unchecked")
    public <A extends Annotation> A getAnnotation() {
        return (A) annotation;
    }

    public Class<? extends Annotation> getAnnotationType() {
        return annotationType;
    }

    public ElementType[] getElementTypes() {
        return elementTypes;
    }

    public RetentionPolicy getPolicy() {
        return policy;
    }

    public boolean isDocumented() {
        return isDocumented;
    }

    public boolean isInherited() {
        return isInherited;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder
            .append("AnnotationDescriptor [annotation=")
            .append(annotation)
            .append(", annotationType=")
            .append(annotationType)
            .append(", elementTypes=")
            .append(Arrays.toString(elementTypes))
            .append(", policy=")
            .append(policy)
            .append(", isDocumented=")
            .append(isDocumented)
            .append(", isInherited=")
            .append(isInherited)
            .append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(elementTypes);
        result = prime * result + Objects.hash(annotation, annotationType, isDocumented, isInherited, policy);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        AnnotationDescriptor other = (AnnotationDescriptor) obj;
        return (
            Objects.equals(annotation, other.annotation) &&
            Objects.equals(annotationType, other.annotationType) &&
            Arrays.equals(elementTypes, other.elementTypes) &&
            isDocumented == other.isDocumented &&
            isInherited == other.isInherited &&
            policy == other.policy
        );
    }
}
