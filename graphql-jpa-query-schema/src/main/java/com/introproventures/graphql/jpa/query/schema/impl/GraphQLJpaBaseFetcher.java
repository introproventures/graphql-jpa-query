package com.introproventures.graphql.jpa.query.schema.impl;

import com.introproventures.graphql.jpa.query.annotation.GraphQLReadEntityForRole;
import graphql.execution.AbortExecutionException;
import graphql.language.Field;
import graphql.language.Selection;
import graphql.language.SelectionSet;
import graphql.schema.*;

import javax.persistence.EntityManager;
import java.lang.reflect.AnnotatedElement;

public abstract class GraphQLJpaBaseFetcher implements DataFetcher<Object> {
    protected final EntityManager entityManager;
    protected final FetcherParams fetcherParams;

    public GraphQLJpaBaseFetcher(EntityManager entityManager, FetcherParams fetcherParams) {
        this.entityManager = entityManager;
        this.fetcherParams = fetcherParams;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public FetcherParams getFetcherParams() {
        return fetcherParams;
    }

    public void checkAccessDataFetching(DataFetchingEnvironment environment) {
        if (fetcherParams.getPredicateRole() == null) {
            return ;
        }
        if (environment.getFieldType() instanceof GraphQLObjectType) {
            System.out.println(environment.getFieldType());
            for (Field field : environment.getFields()) {
                checkAccessSelectionFields(field.getSelectionSet(), (GraphQLObjectType) environment.getFieldType());
            }
        }
    }

    public boolean isReadEntity(AnnotatedElement annotatedElement) {
        if (annotatedElement != null) {
            GraphQLReadEntityForRole readRoles = annotatedElement.getAnnotation(GraphQLReadEntityForRole.class);
            if (readRoles != null) {
                return fetcherParams.getPredicateRole().test(readRoles.value());
            }
        }

        return false;
    }

    public void checkAccessGraphQLObjectType(GraphQLType graphQLType) {
        if (fetcherParams.getMapEntityType().existEntityType(graphQLType.getName())) {
            Class cls = fetcherParams.getMapEntityType().getEntityType(graphQLType.getName()).getJavaType();

            if (!isReadEntity(cls)) {
                throw new RuntimeException("Read access error for entity "+graphQLType.getName());
            }
        }
    }

    public void checkAccessSelectionFields(SelectionSet selectionSet, GraphQLObjectType parentType) {
        checkAccessGraphQLObjectType(parentType);

        if (selectionSet == null)
            return;

        for (Selection sel :selectionSet.getSelections()) {
            if (sel instanceof Field) {
                Field field = (Field) sel;

                GraphQLType graphQLType;


                GraphQLFieldDefinition fieldDef = parentType.getFieldDefinition(field.getName());
                if (fieldDef == null) {
                    throw new AbortExecutionException("Field "+field.getName()+" not found");
                }
                graphQLType = fieldDef.getType();


                if (graphQLType instanceof GraphQLList) {
                    graphQLType = ((GraphQLList) graphQLType).getWrappedType();
                }

                if (graphQLType instanceof GraphQLObjectType) {
                    checkAccessSelectionFields(field.getSelectionSet(), (GraphQLObjectType)graphQLType);
                }
            }
        }
    }
}
