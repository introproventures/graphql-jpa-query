/*
 * Copyright 2017 IntroPro Ventures Inc. and/or its affiliates.
 * Copyright IBM Corporation 2018
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.introproventures.graphql.jpa.query.schema;

import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;

import org.atteo.evo.inflector.English;

public interface NamingStrategy {
    default String singularize(String word) {
        return English.plural(word, 1);
    };

    default String pluralize(String word) {
        return English.plural(word);
    }; 

    default String getName(ManagedType<?> entityType) {
        if (entityType instanceof EntityType)
            return getName((EntityType<?>)entityType);

        if (entityType instanceof EmbeddableType)
            return getName((EmbeddableType<?>) entityType);

        return entityType.getJavaType().getSimpleName();
    }

    default String getName(EntityType<?> entityType) {
        return entityType.getName();
    }

    default String getName(EmbeddableType<?> embeddableType) {
        return embeddableType.getJavaType().getSimpleName() + "EmbeddableType";
    }
}
