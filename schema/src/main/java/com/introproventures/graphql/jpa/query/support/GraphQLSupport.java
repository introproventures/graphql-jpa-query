package com.introproventures.graphql.jpa.query.support;

import static com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder.PAGE_PARAM_NAME;
import static com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder.QUERY_WHERE_PARAM_NAME;

import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;
import com.introproventures.graphql.jpa.query.schema.impl.PageArgument;
import graphql.language.Argument;
import graphql.language.Field;
import graphql.language.ObjectField;
import graphql.language.ObjectValue;
import graphql.language.SelectionSet;
import graphql.schema.DataFetchingEnvironment;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GraphQLSupport {

    public static Stream<Field> fields(SelectionSet selectionSet) {
        return selectionSet.getSelections().stream().filter(Field.class::isInstance).map(Field.class::cast);
    }

    public static Optional<Field> searchByFieldName(Field root, String fieldName) {
        Predicate<Field> matcher = field -> fieldName.equals(field.getName());

        return search(root, matcher);
    }

    public static Optional<Field> search(Field root, Predicate<Field> predicate) {
        Queue<Field> queue = new ArrayDeque<>();
        queue.add(root);

        Field currentNode;
        while (!queue.isEmpty()) {
            currentNode = queue.remove();

            if (predicate.test(currentNode)) {
                return Optional.of(currentNode);
            } else {
                queue.addAll(selections(currentNode));
            }
        }

        return Optional.empty();
    }

    public static final Collection<Field> selections(Field field) {
        SelectionSet selectionSet = Optional
            .ofNullable(field.getSelectionSet())
            .map(Function.identity())
            .orElseGet(() -> new SelectionSet(Collections.emptyList()));

        return fields(selectionSet).collect(Collectors.toList());
    }

    public static Optional<Argument> getPageArgument(Field field) {
        return field.getArguments().stream().filter(it -> PAGE_PARAM_NAME.equals(it.getName())).findFirst();
    }

    public static Optional<Argument> getWhereArgument(Field field) {
        return field.getArguments().stream().filter(it -> QUERY_WHERE_PARAM_NAME.equals(it.getName())).findFirst();
    }

    public static PageArgument extractPageArgument(
        DataFetchingEnvironment environment,
        Optional<Argument> paginationRequest,
        int defaultPageLimitSize
    ) {
        if (paginationRequest.isPresent()) {
            Map<String, Integer> pagex = environment.getArgument(GraphQLJpaSchemaBuilder.PAGE_PARAM_NAME);

            Integer start = pagex.getOrDefault(GraphQLJpaSchemaBuilder.PAGE_START_PARAM_NAME, 1);
            Integer limit = pagex.getOrDefault(GraphQLJpaSchemaBuilder.PAGE_LIMIT_PARAM_NAME, defaultPageLimitSize);

            return new PageArgument(start, limit);
        }

        return new PageArgument(1, defaultPageLimitSize);
    }

    public static Field removeArgument(Field field, Optional<Argument> argument) {
        if (!argument.isPresent()) {
            return field;
        }

        List<Argument> newArguments = field
            .getArguments()
            .stream()
            .filter(a -> !a.equals(argument.get()))
            .collect(Collectors.toList());

        return field.transform(builder -> builder.arguments(newArguments));
    }

    public static Boolean isWhereArgument(Argument argument) {
        return GraphQLJpaSchemaBuilder.QUERY_WHERE_PARAM_NAME.equals(argument.getName());
    }

    public static Boolean isPageArgument(Argument argument) {
        return GraphQLJpaSchemaBuilder.PAGE_PARAM_NAME.equals(argument.getName());
    }

    public static Boolean isFirstArgument(Argument argument) {
        return "first".equals(argument.getName());
    }

    public static Boolean isAfterArgument(Argument argument) {
        return "after".equals(argument.getName());
    }

    public static Boolean isLogicalArgument(Argument argument) {
        return GraphQLJpaSchemaBuilder.QUERY_LOGICAL_PARAM_NAME.equals(argument.getName());
    }

    public static Boolean isDistinctArgument(Argument argument) {
        return GraphQLJpaSchemaBuilder.SELECT_DISTINCT_PARAM_NAME.equals(argument.getName());
    }

    public static final Optional<ObjectField> getObjectField(ObjectValue objectValue, String fieldName) {
        return objectValue.getObjectFields().stream().filter(it -> fieldName.equals(it.getName())).findFirst();
    }

    public static final Optional<Field> getSelectionField(Field field, String fieldName) {
        return GraphQLSupport.fields(field.getSelectionSet()).filter(it -> fieldName.equals(it.getName())).findFirst();
    }

    public static Optional<Argument> findArgument(Field selectedField, String name) {
        return Optional
            .ofNullable(selectedField.getArguments())
            .flatMap(arguments -> arguments
                .stream()
                .filter(argument -> name.equals(argument.getName()))
                .findFirst());
    }

    public static List<Field> getFields(SelectionSet selections, String fieldName) {
        return selections
            .getSelections()
            .stream()
            .map(Field.class::cast)
            .filter(it -> fieldName.equals(it.getName()))
            .collect(Collectors.toList());
    }

    public static String getAliasOrName(Field field) {
        return Optional.ofNullable(field.getAlias()).orElse(field.getName());
    }

    public static Collector<Object, List<Object>, List<Object>> toResultList() {
        return Collector.of(
            ArrayList::new,
            (list, item) -> {
                if (item != null) {
                    list.add(item);
                }
            },
            (left, right) -> {
                left.addAll(right);
                return left;
            },
            list -> {
                return list
                    .stream()
                    .filter(GraphQLSupport.distinctByKey(GraphQLSupport::identityToString))
                    .collect(Collectors.toList());
            },
            Collector.Characteristics.CONCURRENT
        );
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();

        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    public static String identityToString(final Object object) {
        if (object == null) {
            return null;
        }
        final String name = object.getClass().getName();
        final String hexString = Integer.toHexString(System.identityHashCode(object));
        final StringBuilder builder = new StringBuilder(name.length() + 1 + hexString.length());
        // @formatter:off
        builder.append(name)
              .append("@")
              .append(hexString);
        // @formatter:off
        return builder.toString();
    }
}
