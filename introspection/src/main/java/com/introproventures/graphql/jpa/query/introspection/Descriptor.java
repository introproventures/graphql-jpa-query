package com.introproventures.graphql.jpa.query.introspection;

import java.lang.annotation.Annotation;
import java.util.Objects;

public abstract class Descriptor {

    protected final ClassDescriptor classDescriptor;
    protected final boolean isPublic;

    protected Annotations annotations;

    protected Descriptor(ClassDescriptor classDescriptor, boolean isPublic) {
        this.classDescriptor = classDescriptor;
        this.isPublic = isPublic;
    }

    public ClassDescriptor getClassDescriptor() {
        return classDescriptor;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public boolean matchDeclared(boolean declared) {
        if (!declared) {
            return isPublic;
        }

        return true;
    }

    protected Annotations getAnnotations() {
        return annotations;
    }

    public AnnotationDescriptor getAnnotationDescriptor(Class<? extends Annotation> clazz) {
        return annotations.getAnnotationDescriptor(clazz);
    }

    public <A extends Annotation> A getAnnotation(Class<A> clazz) {
        AnnotationDescriptor annotationDescriptor = annotations.getAnnotationDescriptor(clazz);
        if (annotationDescriptor == null) {
            return null;
        }

        return annotationDescriptor.getAnnotation();
    }

    public AnnotationDescriptor[] getAllAnnotationDescriptors() {
        return annotations.getAllAnnotationDescriptors();
    }

    public abstract String getName();

    @Override
    public int hashCode() {
        return Objects.hash(annotations, classDescriptor, isPublic);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Descriptor other = (Descriptor) obj;
        return (
            Objects.equals(annotations, other.annotations) &&
            Objects.equals(classDescriptor, other.classDescriptor) &&
            isPublic == other.isPublic
        );
    }
}
