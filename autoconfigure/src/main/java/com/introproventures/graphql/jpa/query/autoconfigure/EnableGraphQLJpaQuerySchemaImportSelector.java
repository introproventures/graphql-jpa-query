package com.introproventures.graphql.jpa.query.autoconfigure;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

class EnableGraphQLJpaQuerySchemaImportSelector implements ImportSelector {

    private static List<String> packageNames = new ArrayList<>();

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(
                importingClassMetadata.getAnnotationAttributes(
                        EnableGraphQLJpaQuerySchema.class.getName(), false));

        if (attributes != null) {
            Stream.of(attributes.getClassArray("basePackageClasses"))
                  .map(Class::getPackage)
                  .map(Package::getName)
                  .forEach(packageNames::add);
        }

        return new String[0];
    }

    static List<String> getPackageNames() {
        return packageNames;
    }

}
