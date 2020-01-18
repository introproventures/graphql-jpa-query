package com.introproventures.graphql.jpa.query.support;

import java.util.stream.Stream;

import graphql.language.Field;
import graphql.language.SelectionSet;

public class GraphQLSupport {

    public static Stream<Field> fields(SelectionSet selectionSet) {
        return selectionSet.getSelections()
                           .stream()
                           .filter(Field.class::isInstance)
                           .map(Field.class::cast);
        
    }
    
}
