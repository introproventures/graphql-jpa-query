/*
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
package com.introproventures.graphql.jpa.query.schema.security;

import com.introproventures.graphql.jpa.query.schema.IQueryAuthorizationStrategy;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;
import com.introproventures.graphql.jpa.query.schema.security.Authorization;
import graphql.introspection.Introspection;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static graphql.Scalars.GraphQLString;

public class GraphQLJpaSchemaWithExtensionsBuilder extends GraphQLJpaSchemaBuilder {
    public GraphQLJpaSchemaWithExtensionsBuilder(EntityManager entityManager) {
        super(entityManager);
        authorizationStrategy(new AuthorizationStrategy());
    }

    @Override
    public List<GraphQLDirective> getAuthorizationDirectives(ManagedType<?> entityType) {
        //Get the security annotations for the entity
        Authorization authorization = getAuthorization(entityType);

        //Generate the security directive and return it
        List<GraphQLDirective> directives = getSecurityDirectives(authorization);

        return directives;
    }

    private Authorization getAuthorization(ManagedType<?> entityType) {
        Class<?> clazz = entityType.getJavaType();
        while (clazz != null) {
            Authorization authorization = clazz.getAnnotation(Authorization.class);
            if (authorization != null)
                return authorization;
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    private Authorization getAuthorization(Attribute<?, ?> attribute) {
        Class<?> clazz = attribute.getDeclaringType().getJavaType();
        Field field = getField(clazz, attribute.getName());

        Authorization authorization = field.getAnnotation(Authorization.class);
        return authorization;
    }

    private Field getField(Class clazz, String fieldName) {
        while (clazz != null && fieldName != null) {
            Field field = null;
            try {
                field = clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
                continue;
            }
            if (field != null)
                return field;

        }

        return null;
    }

    private List<GraphQLDirective> getSecurityDirectives(Authorization authorization) {
        if (authorization == null)
            return null;

        List<GraphQLDirective> directiveList = new ArrayList<>(1);
        GraphQLDirective directive = GraphQLDirective.newDirective().name(IQueryAuthorizationStrategy.AUTHORIZATION)
                .argument(GraphQLArgument.newArgument().name("role").type(GraphQLString).value(authorization.role())
                        .build())
                .validLocation(Introspection.DirectiveLocation.FIELD_DEFINITION).build();
        directiveList.add(directive);

        return directiveList;
    }

    @Override
    public List<GraphQLDirective> getAuthorizationDirectives(Attribute<?, ?> attribute) {
        //Get the security annotations for the entity
        Authorization authorization = getAuthorization(attribute);

        //Generate the security directive and return it
        List<GraphQLDirective> directives = getSecurityDirectives(authorization);
        return directives;
    }

}
