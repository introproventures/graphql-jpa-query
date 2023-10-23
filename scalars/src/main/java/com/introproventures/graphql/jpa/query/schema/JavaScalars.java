/*
 * Copyright 2017 IntroPro Ventures Inc. and/or its affiliates.
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

import static graphql.schema.GraphQLScalarType.newScalar;

import graphql.Assert;
import graphql.Scalars;
import graphql.language.ArrayValue;
import graphql.language.BooleanValue;
import graphql.language.EnumValue;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.NullValue;
import graphql.language.ObjectField;
import graphql.language.ObjectValue;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.language.VariableReference;
import graphql.scalars.ExtendedScalars;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides Registry to resolve GraphQL Query Java Scalar Types
 *
 * @author Igor Dianov
 *
 */
public class JavaScalars {

    private static final Logger log = LoggerFactory.getLogger(JavaScalars.class);

    private static final Map<Class<?>, GraphQLScalarType> scalarsRegistry = new HashMap<Class<?>, GraphQLScalarType>();

    private static final JavaScalars instance = new JavaScalars();

    public static final GraphQLScalarType GraphQLDateScalar = newScalarType(
        "Date",
        "Date type",
        new GraphQLDateCoercing()
    );
    public static final GraphQLScalarType GraphQLTimeScalar = newScalarType(
        "Time",
        "Time type",
        new GraphQLTimeCoercing()
    );
    public static final GraphQLScalarType GraphQLTimestampScalar = newScalarType(
        "Timestamp",
        "Timestamp type",
        new GraphQLSqlTimestampCoercing()
    );
    public static final GraphQLScalarType GraphQLLocalDateTimeScalar = newScalarType(
        "LocalDateTime",
        "LocalDateTime type",
        new GraphQLLocalDateTimeCoercing()
    );
    public static final GraphQLScalarType GraphQLLocalDateScalar = newScalarType(
        "LocalDate",
        "LocalDate type",
        new GraphQLLocalDateCoercing()
    );
    public static final GraphQLScalarType GraphQLLocalTimeScalar = newScalarType(
        "LocalTime",
        "LocalTime type",
        new GraphQLLocalTimeCoercing()
    );
    public static final GraphQLScalarType GraphQLUUIDScalar = newScalarType(
        "UUID",
        "UUID type",
        new GraphQLUUIDCoercing()
    );
    public static final GraphQLScalarType GraphQLObjectScalar = newScalarType(
        "Object",
        "Object type",
        new GraphQLObjectCoercing()
    );
    public static final GraphQLScalarType GraphQLByteArrayScalar = newScalarType(
        "ByteArray",
        "ByteArray type",
        new GraphQLLOBCoercing()
    );
    public static final GraphQLScalarType GraphQLZonedDateTimeScalar = newScalarType(
        "ZonedDateTime",
        "ZonedDateTime type",
        new GraphQLZonedDateTimeCoercing()
    );
    public static final GraphQLScalarType GraphQLOffsetDateTimeScalar = newScalarType(
        "OffsetDateTime",
        "OffsetDateTime type",
        new GraphQLOffsetDateTimeCoercing()
    );
    public static final GraphQLScalarType GraphQLInstantScalar = newScalarType(
        "Instant",
        "Instant type",
        new GraphQLInstantCoercing()
    );

    static {
        scalarsRegistry.put(String.class, Scalars.GraphQLString);

        scalarsRegistry.put(Integer.class, Scalars.GraphQLInt);
        scalarsRegistry.put(int.class, Scalars.GraphQLInt);

        scalarsRegistry.put(Short.class, ExtendedScalars.GraphQLShort);
        scalarsRegistry.put(short.class, ExtendedScalars.GraphQLShort);

        scalarsRegistry.put(Float.class, Scalars.GraphQLFloat);
        scalarsRegistry.put(float.class, Scalars.GraphQLFloat);

        scalarsRegistry.put(Double.class, Scalars.GraphQLFloat);
        scalarsRegistry.put(double.class, Scalars.GraphQLFloat);

        scalarsRegistry.put(Long.class, ExtendedScalars.GraphQLLong);
        scalarsRegistry.put(long.class, ExtendedScalars.GraphQLLong);

        scalarsRegistry.put(Boolean.class, Scalars.GraphQLBoolean);
        scalarsRegistry.put(boolean.class, Scalars.GraphQLBoolean);

        scalarsRegistry.put(BigInteger.class, ExtendedScalars.GraphQLBigInteger);

        scalarsRegistry.put(char.class, ExtendedScalars.GraphQLChar);
        scalarsRegistry.put(Character.class, ExtendedScalars.GraphQLChar);

        scalarsRegistry.put(Byte.class, ExtendedScalars.GraphQLByte);
        scalarsRegistry.put(byte.class, ExtendedScalars.GraphQLByte);

        scalarsRegistry.put(BigDecimal.class, ExtendedScalars.GraphQLBigDecimal);

        scalarsRegistry.put(LocalDateTime.class, GraphQLLocalDateTimeScalar);
        scalarsRegistry.put(LocalDate.class, GraphQLLocalDateScalar);
        scalarsRegistry.put(LocalTime.class, GraphQLLocalTimeScalar);
        scalarsRegistry.put(Date.class, GraphQLTimestampScalar);
        scalarsRegistry.put(UUID.class, GraphQLUUIDScalar);
        scalarsRegistry.put(Object.class, GraphQLObjectScalar);
        scalarsRegistry.put(java.sql.Date.class, GraphQLDateScalar);
        scalarsRegistry.put(java.sql.Time.class, GraphQLTimeScalar);
        scalarsRegistry.put(java.sql.Timestamp.class, GraphQLTimestampScalar);
        scalarsRegistry.put(Byte[].class, GraphQLByteArrayScalar);
        scalarsRegistry.put(byte[].class, GraphQLByteArrayScalar);
        scalarsRegistry.put(Instant.class, GraphQLInstantScalar);
        scalarsRegistry.put(ZonedDateTime.class, GraphQLZonedDateTimeScalar);
        scalarsRegistry.put(OffsetDateTime.class, GraphQLOffsetDateTimeScalar);
    }

    public static Optional<GraphQLScalarType> of(String name) {
        return scalarsRegistry.values().stream().filter(scalar -> name.equals(scalar.getName())).findFirst();
    }

    public static Collection<GraphQLScalarType> scalars() {
        return Collections.unmodifiableCollection(scalarsRegistry.values());
    }

    public static boolean contains(Class<?> key) {
        return scalarsRegistry.containsKey(key);
    }

    public static GraphQLScalarType of(Class<?> key) {
        return scalarsRegistry.computeIfAbsent(key, JavaScalars::computeGraphQLScalarType);
    }

    protected static GraphQLScalarType computeGraphQLScalarType(Class<?> key) {
        String typeName = key.getSimpleName();
        String description = typeName + " Scalar Object Type";

        return newScalarType(typeName, description, new GraphQLObjectCoercing());
    }

    public static <T extends Coercing> GraphQLScalarType newScalarType(String name, String description, T coercing) {
        return newScalar().name(name).description(description).coercing(coercing).build();
    }

    public static JavaScalars register(Class<?> key, GraphQLScalarType value) {
        Assert.assertNotNull(key, () -> "key parameter cannot be null.");
        Assert.assertNotNull(value, () -> "value parameter cannot be null.");

        scalarsRegistry.put(key, value);

        return instance;
    }

    public static class GraphQLLocalDateTimeCoercing implements Coercing<Object, Object> {

        private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        @Override
        public Object serialize(Object input) {
            if (input instanceof String) {
                return parseStringToLocalDateTime((String) input);
            } else if (input instanceof LocalDateTime) {
                return ((LocalDateTime) input).format(formatter);
            } else if (input instanceof LocalDate) {
                return ((LocalDate) input).format(formatter);
            } else if (input instanceof Long) {
                return parseLongToLocalDateTime((Long) input);
            } else if (input instanceof Integer) {
                return parseLongToLocalDateTime((Integer) input);
            }
            return null;
        }

        @Override
        public Object parseValue(Object input) {
            return serialize(input);
        }

        @Override
        public Object parseLiteral(Object input) {
            if (input instanceof StringValue) {
                return parseStringToLocalDateTime(((StringValue) input).getValue());
            } else if (input instanceof IntValue) {
                BigInteger value = ((IntValue) input).getValue();
                return parseLongToLocalDateTime(value.longValue());
            }
            return null;
        }

        private LocalDateTime parseLongToLocalDateTime(long input) {
            return LocalDateTime.ofInstant(Instant.ofEpochSecond(input), TimeZone.getDefault().toZoneId());
        }

        private LocalDateTime parseStringToLocalDateTime(String input) {
            try {
                return LocalDateTime.parse(input);
            } catch (DateTimeParseException e) {
                log.warn("Failed to parse Date from input: " + input, e);
                return null;
            }
        }
    }

    public static class GraphQLLocalDateCoercing implements Coercing<Object, Object> {

        private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        @Override
        public Object serialize(Object input) {
            if (input instanceof String) {
                return parseStringToLocalDate((String) input);
            } else if (input instanceof LocalDate) {
                return ((LocalDate) input).format(formatter);
            } else if (input instanceof Long) {
                return parseLongToLocalDate((Long) input);
            } else if (input instanceof Integer) {
                return parseLongToLocalDate((Integer) input);
            }
            return null;
        }

        @Override
        public Object parseValue(Object input) {
            return serialize(input);
        }

        @Override
        public Object parseLiteral(Object input) {
            if (input instanceof StringValue) {
                return parseStringToLocalDate(((StringValue) input).getValue());
            } else if (input instanceof IntValue) {
                BigInteger value = ((IntValue) input).getValue();
                return parseLongToLocalDate(value.longValue());
            }
            return null;
        }

        private LocalDate parseLongToLocalDate(long input) {
            return LocalDateTime
                .ofInstant(Instant.ofEpochSecond(input), TimeZone.getDefault().toZoneId())
                .toLocalDate();
        }

        private LocalDate parseStringToLocalDate(String input) {
            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException e) {
                log.warn("Failed to parse Date from input: " + input, e);
                return null;
            }
        }
    }

    public static class GraphQLLocalTimeCoercing implements Coercing<LocalTime, String> {

        public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME.withZone(ZoneOffset.UTC);

        private LocalTime convertImpl(Object input) {
            if (input instanceof String) {
                try {
                    return LocalTime.parse((String) input, FORMATTER);
                } catch (DateTimeParseException ignored) {}
            }
            return null;
        }

        @Override
        public String serialize(Object input) {
            if (input instanceof LocalTime) {
                return DateTimeFormatter.ISO_LOCAL_TIME.format((LocalTime) input);
            } else {
                LocalTime result = convertImpl(input);
                if (result == null) {
                    throw new CoercingSerializeException("Invalid value '" + input + "' for LocalTime");
                }
                return DateTimeFormatter.ISO_LOCAL_TIME.format(result);
            }
        }

        @Override
        public LocalTime parseValue(Object input) {
            LocalTime result = convertImpl(input);
            if (result == null) {
                throw new CoercingParseValueException("Invalid value '" + input + "' for LocalTime");
            }
            return result;
        }

        @Override
        public LocalTime parseLiteral(Object input) {
            if (!(input instanceof StringValue)) return null;
            String value = ((StringValue) input).getValue();
            LocalTime result = convertImpl(value);
            return result;
        }
    }

    public static class GraphQLInstantCoercing implements Coercing<Object, Object> {

        @Override
        public Object serialize(Object input) {
            if (input instanceof String) {
                return parseStringToInstant((String) input);
            } else if (input instanceof Instant) {
                return input.toString();
            } else if (input instanceof Long) {
                return Instant.ofEpochMilli((Long) input);
            }
            return null;
        }

        @Override
        public Object parseValue(Object input) {
            return serialize(input);
        }

        @Override
        public Object parseLiteral(Object input) {
            if (input instanceof StringValue) {
                return parseStringToInstant(((StringValue) input).getValue());
            }
            return null;
        }

        private Instant parseStringToInstant(String input) {
            try {
                return Instant.parse(input);
            } catch (DateTimeParseException e) {
                log.warn("Failed to parse Date from input: " + input, e);
                return null;
            }
        }
    }

    public static class GraphQLZonedDateTimeCoercing implements Coercing<Object, Object> {

        @Override
        public Object serialize(Object input) {
            if (input instanceof String) {
                return parseStringToZonedDateTime((String) input);
            } else if (input instanceof ZonedDateTime) {
                return ((ZonedDateTime) input).withZoneSameInstant(ZoneId.of("UTC"));
            } else if (input instanceof LocalDate) {
                return input;
            } else if (input instanceof Long) {
                return parseLongToZonedDateTime((Long) input);
            } else if (input instanceof Integer) {
                return parseLongToZonedDateTime((Integer) input);
            }
            return null;
        }

        @Override
        public Object parseValue(Object input) {
            return serialize(input);
        }

        @Override
        public Object parseLiteral(Object input) {
            if (input instanceof StringValue) {
                return parseStringToZonedDateTime(((StringValue) input).getValue());
            }
            return null;
        }

        private ZonedDateTime parseLongToZonedDateTime(long input) {
            return ZonedDateTime.ofInstant(Instant.ofEpochSecond(input), TimeZone.getDefault().toZoneId());
        }

        private ZonedDateTime parseStringToZonedDateTime(String input) {
            try {
                return ZonedDateTime.parse(input);
            } catch (DateTimeParseException e) {
                log.warn("Failed to parse Date from input: " + input, e);
                return null;
            }
        }
    }

    public static class GraphQLOffsetDateTimeCoercing implements Coercing<Object, Object> {

        @Override
        public Object serialize(Object input) {
            if (input instanceof String) {
                return parseStringToOffsetDateTime((String) input);
            } else if (input instanceof OffsetDateTime) {
                return ((OffsetDateTime) input).withOffsetSameInstant(ZoneOffset.of("Z"));
            } else if (input instanceof LocalDate) {
                return input;
            } else if (input instanceof Long) {
                return parseLongToOffsetDateTime((Long) input);
            } else if (input instanceof Integer) {
                return parseLongToOffsetDateTime((Integer) input);
            }
            return null;
        }

        @Override
        public Object parseValue(Object input) {
            return serialize(input);
        }

        @Override
        public Object parseLiteral(Object input) {
            if (input instanceof StringValue) {
                return parseStringToOffsetDateTime(((StringValue) input).getValue());
            }
            return null;
        }

        private OffsetDateTime parseLongToOffsetDateTime(long input) {
            return OffsetDateTime.ofInstant(Instant.ofEpochSecond(input), TimeZone.getDefault().toZoneId());
        }

        private OffsetDateTime parseStringToOffsetDateTime(String input) {
            try {
                return OffsetDateTime.parse(input);
            } catch (DateTimeParseException e) {
                log.warn("Failed to parse Date from input: " + input, e);
                return null;
            }
        }
    }

    public static class GraphQLUUIDCoercing implements Coercing<Object, Object> {

        @Override
        public Object serialize(Object input) {
            if (input instanceof UUID) {
                return input;
            }
            return null;
        }

        @Override
        public Object parseValue(Object input) {
            if (input instanceof String) {
                return parseStringToUUID((String) input);
            }
            return null;
        }

        @Override
        public Object parseLiteral(Object input) {
            if (input instanceof StringValue) {
                return parseStringToUUID(((StringValue) input).getValue());
            }
            return null;
        }

        private UUID parseStringToUUID(String input) {
            try {
                return UUID.fromString(input);
            } catch (IllegalArgumentException e) {
                log.warn("Failed to parse UUID from input: " + input, e);
                return null;
            }
        }
    }

    public static class GraphQLRawObjectCoercing implements Coercing<Object, Object> {

        @Override
        public Object serialize(Object input) {
            return input;
        }

        @Override
        public Object parseValue(Object input) {
            return input;
        }

        @Override
        public Object parseLiteral(Object input) {
            return input;
        }
    }

    public static class GraphQLDateCoercing implements Coercing<Object, Object> {

        private final ThreadLocal<DateFormat> df;

        /**
         * Default to pattern 'yyyy-MM-dd'
         */
        public GraphQLDateCoercing() {
            this("yyyy-MM-dd");
        }

        /**
         * Parse date strings according to the provided SimpleDateFormat pattern
         *
         * @param dateFormatString e.g. "yyyy-MM-dd'T'HH:mm:ss.SSSXXX" for "2001-07-04T12:08:56.235-07:00"
         */
        public GraphQLDateCoercing(String dateFormatString) {
            this.df = ThreadLocal.withInitial(() -> new SimpleDateFormat(dateFormatString));
        }

        @Override
        public Object serialize(Object input) {
            if (input instanceof String stringInput) {
                return parseStringToDate(stringInput);
            } else if (input instanceof Date) {
                return df.get().format(input);
            } else if (input instanceof Long longInput) {
                return new java.sql.Date(longInput);
            } else if (input instanceof Integer intInput) {
                return new java.sql.Date(intInput.longValue());
            }
            return null;
        }

        @Override
        public Object parseValue(Object input) {
            return serialize(input);
        }

        @Override
        public Object parseLiteral(Object input) {
            if (input instanceof StringValue stringValue) {
                return parseStringToDate(stringValue.getValue());
            } else if (input instanceof IntValue intValue) {
                BigInteger value = intValue.getValue();
                return new java.sql.Date(value.longValue());
            }
            return null;
        }

        private java.sql.Date parseStringToDate(String input) {
            try {
                return new java.sql.Date(df.get().parse(input).getTime());
            } catch (ParseException e) {
                log.warn("Failed to parse SQL Date from input: " + input, e);
                return null;
            }
        }
    }

    public static class GraphQLTimeCoercing implements Coercing<Object, Object> {

        private final ThreadLocal<DateFormat> df;

        /**
         * Default to pattern 'yyyy-MM-dd'
         */
        public GraphQLTimeCoercing() {
            this("HH:mm:ss");
        }

        /**
         * Parse time strings according to the provided SimpleDateFormat pattern
         *
         * @param timeFormatString e.g. "HH:mm:ss.SSSXXX"
         */
        public GraphQLTimeCoercing(String timeFormatString) {
            this.df = ThreadLocal.withInitial(() -> new SimpleDateFormat(timeFormatString));
        }

        @Override
        public Object serialize(Object input) {
            if (input instanceof String inputString) {
                return parseStringToTime(inputString);
            } else if (input instanceof java.sql.Time) {
                return df.get().format(input);
            } else if (input instanceof Long longInput) {
                return new java.sql.Time(longInput);
            } else if (input instanceof Integer integerInput) {
                return new java.sql.Time(integerInput.longValue());
            }
            return null;
        }

        @Override
        public Object parseValue(Object input) {
            return serialize(input);
        }

        @Override
        public Object parseLiteral(Object input) {
            if (input instanceof StringValue stringValue) {
                return parseStringToTime(stringValue.getValue());
            } else if (input instanceof IntValue intValue) {
                BigInteger value = intValue.getValue();
                return new java.sql.Time(value.longValue());
            }
            return null;
        }

        private java.sql.Time parseStringToTime(String input) {
            try {
                return new java.sql.Time(df.get().parse(input).getTime());
            } catch (ParseException e) {
                log.warn("Failed to parse SQL Date from input: " + input, e);
                return null;
            }
        }
    }

    public static class GraphQLSqlTimestampCoercing implements Coercing<Timestamp, Object> {

        private final ThreadLocal<DateTimeFormatter> df = ThreadLocal.withInitial(() -> DateTimeFormatter.ISO_INSTANT);

        private Timestamp doConvert(Object input) {
            if (input instanceof Long longInput) {
                return new Timestamp(longInput);
            } else if (input instanceof String stringInput) {
                Instant instant = DateTimeHelper.parseDate(stringInput);
                return Timestamp.from(instant);
            } else if (input instanceof Timestamp timestampInput) {
                return timestampInput;
            } else if (input instanceof Date dateInput) {
                return new Timestamp(dateInput.getTime());
            }

            return null;
        }

        @Override
        public Object serialize(Object input) {
            Timestamp result = doConvert(input);

            if (result == null) {
                throw new CoercingSerializeException("Invalid value '" + input + "' for Timestamp");
            }

            return df.get().format(result.toInstant());
        }

        @Override
        public Timestamp parseValue(Object input) {
            Timestamp result = doConvert(input);

            if (result == null) {
                throw new CoercingParseValueException("Invalid value '" + input + "' for Timestamp");
            }
            return result;
        }

        @Override
        public Timestamp parseLiteral(Object input) {
            Object value = null;

            if (input instanceof IntValue intValue) {
                value = intValue.getValue().longValue();
            } else if (input instanceof StringValue stringValue) {
                value = stringValue.getValue();
            } else {
                throw new CoercingParseLiteralException("Invalid value '" + input + "' for Timestamp");
            }

            return doConvert(value);
        }
    }

    public static class GraphQLLOBCoercing implements Coercing<Object, Object> {

        @Override
        public Object serialize(Object input) {
            if (input.getClass() == byte[].class) {
                return new String((byte[]) input, StandardCharsets.UTF_8);
            }
            return null;
        }

        @Override
        public Object parseValue(Object input) {
            if (input instanceof String) {
                return parseStringToByteArray((String) input);
            }
            return null;
        }

        @Override
        public Object parseLiteral(Object input) {
            if (input instanceof StringValue) {
                return parseStringToByteArray(((StringValue) input).getValue());
            }
            return null;
        }

        private byte[] parseStringToByteArray(String input) {
            try {
                return input.getBytes(StandardCharsets.UTF_8);
            } catch (IllegalArgumentException e) {
                log.warn("Failed to parse byte[] from input: " + input, e);
                return null;
            }
        }
    }

    public static class GraphQLObjectCoercing implements Coercing<Object, Object> {

        @Override
        public Object serialize(Object dataFetcherResult) {
            return dataFetcherResult;
        }

        @Override
        public Object parseValue(Object input) {
            return input;
        }

        @Override
        public Object parseLiteral(Object input) {
            return parseLiteral(input, Collections.emptyMap());
        }

        //recursively parse the input into a Map
        @Override
        public Object parseLiteral(Object value, Map<String, Object> variables) {
            if (!(value instanceof Value)) {
                throw new IllegalArgumentException("Expected AST type 'StringValue' but was '" + value + "'.");
            }

            if (value instanceof StringValue) {
                return ((StringValue) value).getValue();
            }
            if (value instanceof IntValue) {
                return ((IntValue) value).getValue();
            }
            if (value instanceof FloatValue) {
                return ((FloatValue) value).getValue();
            }
            if (value instanceof BooleanValue) {
                return ((BooleanValue) value).isValue();
            }
            if (value instanceof EnumValue) {
                return ((EnumValue) value).getName();
            }
            if (value instanceof NullValue) {
                return null;
            }
            if (value instanceof VariableReference) {
                String varName = ((VariableReference) value).getName();
                return variables.get(varName);
            }
            if (value instanceof ArrayValue) {
                List<Value> values = ((ArrayValue) value).getValues();
                return values.stream().map(v -> parseLiteral(v, variables)).collect(Collectors.toList());
            }
            if (value instanceof ObjectValue) {
                List<ObjectField> values = ((ObjectValue) value).getObjectFields();
                Map<String, Object> parsedValues = new LinkedHashMap<>();

                values.forEach(field -> {
                    Object parsedValue = parseLiteral(field.getValue(), variables);
                    parsedValues.put(field.getName(), parsedValue);
                });
                return parsedValues;
            }

            //Should never happen, as it would mean the variable was not replaced by the parser
            throw new IllegalArgumentException("Unsupported scalar value type: " + value.getClass().getName());
        }
    }

    public static final class DateTimeHelper {

        static final List<Function<String, Instant>> CONVERTERS = new CopyOnWriteArrayList<>();

        static {
            CONVERTERS.add(new InstantConverter());
            CONVERTERS.add(new OffsetDateTimeConverter());
            CONVERTERS.add(new ZonedDateTimeConverter());
            CONVERTERS.add(new LocalDateTimeConverter());
            CONVERTERS.add(new LocalDateConverter());
        }

        public static Instant parseDate(String date) {
            Objects.requireNonNull(date, "date");

            for (Function<String, Instant> converter : CONVERTERS) {
                try {
                    return converter.apply(date);
                } catch (java.time.format.DateTimeParseException ignored) {}
            }

            return null;
        }

        static class InstantConverter implements Function<String, Instant> {

            @Override
            public Instant apply(String date) {
                return Instant.parse(date);
            }
        }

        static class OffsetDateTimeConverter implements Function<String, Instant> {

            static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneOffset.UTC);

            @Override
            public Instant apply(String date) {
                return OffsetDateTime.parse(date, FORMATTER).toInstant();
            }
        }

        static class ZonedDateTimeConverter implements Function<String, Instant> {

            static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_ZONED_DATE_TIME.withZone(ZoneOffset.UTC);

            @Override
            public Instant apply(String date) {
                return ZonedDateTime.parse(date, FORMATTER).toInstant();
            }
        }

        static class LocalDateTimeConverter implements Function<String, Instant> {

            static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneOffset.UTC);

            @Override
            public Instant apply(String date) {
                return LocalDateTime.parse(date, FORMATTER).toInstant(ZoneOffset.UTC);
            }
        }

        static class LocalDateConverter implements Function<String, Instant> {

            static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneOffset.UTC);

            @Override
            public Instant apply(String date) {
                LocalDate localDate = LocalDate.parse(date, FORMATTER);

                return localDate.atStartOfDay().toInstant(ZoneOffset.UTC);
            }
        }
    }
}
