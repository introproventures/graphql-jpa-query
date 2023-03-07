package com.introproventures.graphql.jpa.query.schema.model.metamodel;

import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(ClassWithCustomMetamodel.class)
public class ClassWithCustomMetamodel_ {

    public static volatile SingularAttribute<ClassWithCustomMetamodel, Long> id;
    public static volatile SingularAttribute<ClassWithCustomMetamodel, String> publicValue;
    public static volatile SingularAttribute<ClassWithCustomMetamodel, String> protectedValue;
    public static volatile SingularAttribute<ClassWithCustomMetamodel, String> ignoredProtectedValue;

    public static final String ID = "id";
    public static final String PUBLIC_VALUE = "publicValue";
    public static final String PROTECTED_VALUE = "protectedValue";
    public static final String IGNORED_PROTECTED_VALUE = "ignoredProtectedValue";
}
