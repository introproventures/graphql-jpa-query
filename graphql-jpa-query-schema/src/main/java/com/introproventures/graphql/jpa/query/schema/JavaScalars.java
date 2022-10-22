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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

import graphql.scalar.GraphqlFloatCoercing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;

/**
 * Provides Registry to resolve GraphQL Query Java Scalar Types
 *
 * @author Igor Dianov
 *
 */
public class JavaScalars {

    private static final BigInteger LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);
    private static final BigInteger LONG_MIN = BigInteger.valueOf(Long.MIN_VALUE);
    private static final BigInteger INT_MAX = BigInteger.valueOf(Integer.MAX_VALUE);
    private static final BigInteger INT_MIN = BigInteger.valueOf(Integer.MIN_VALUE);
    private static final BigInteger BYTE_MAX = BigInteger.valueOf(Byte.MAX_VALUE);
    private static final BigInteger BYTE_MIN = BigInteger.valueOf(Byte.MIN_VALUE);
    private static final BigInteger SHORT_MAX = BigInteger.valueOf(Short.MAX_VALUE);
    private static final BigInteger SHORT_MIN = BigInteger.valueOf(Short.MIN_VALUE);

    static final Logger log = LoggerFactory.getLogger(JavaScalars.class);

    private static HashMap<Class<?>, GraphQLScalarType> scalarsRegistry = new HashMap<Class<?>, GraphQLScalarType>();

    public static GraphQLScalarType GraphQLLong = GraphQLScalarType.newScalar()
                                                                   .name("Long")
                                                                   .description("Long type")
                                                                   .coercing(new GraphqlLongCoercing())
                                                                   .build();

    public static GraphQLScalarType GraphQLShort = GraphQLScalarType.newScalar()
                                                                    .name("Short")
                                                                    .description("Short type")
                                                                    .coercing(new GraphqlShortCoercing())
                                                                    .build();

    public static GraphQLScalarType GraphQLBigInteger = GraphQLScalarType.newScalar()
                                                                         .name("BigInteger")
                                                                         .description("BigInteger type")
                                                                         .coercing(new GraphqlBigIntegerCoercing())
                                                                         .build();

    public static GraphQLScalarType GraphQLChar = GraphQLScalarType.newScalar()
                                                                   .name("Char")
                                                                   .description("Char type")
                                                                   .coercing(new GraphqlCharCoercing())
                                                                   .build();

    public static GraphQLScalarType GraphQLByte = GraphQLScalarType.newScalar()
                                                                   .name("Byte")
                                                                   .description("Byte type")
                                                                   .coercing(new GraphqlByteCoercing())
                                                                   .build();

    public static GraphQLScalarType GraphQLBigDecimal = GraphQLScalarType.newScalar()
                                                                         .name("BigDecimal")
                                                                         .description("BigDecimal type")
                                                                         .coercing(new GraphqlBigDecimalCoercing())
                                                                         .build();


    private static JavaScalars instance = new JavaScalars();

    static {
        scalarsRegistry.put(String.class, Scalars.GraphQLString);

        scalarsRegistry.put(Integer.class, Scalars.GraphQLInt);
        scalarsRegistry.put(int.class, Scalars.GraphQLInt);

        scalarsRegistry.put(Short.class, JavaScalars.GraphQLShort);
        scalarsRegistry.put(short.class, JavaScalars.GraphQLShort);

        scalarsRegistry.put(Float.class, Scalars.GraphQLFloat);
        scalarsRegistry.put(float.class, Scalars.GraphQLFloat);

        scalarsRegistry.put(Double.class, Scalars.GraphQLFloat);
        scalarsRegistry.put(double.class, Scalars.GraphQLFloat);

        scalarsRegistry.put(Long.class, JavaScalars.GraphQLLong);
        scalarsRegistry.put(long.class, JavaScalars.GraphQLLong);

        scalarsRegistry.put(Boolean.class, Scalars.GraphQLBoolean);
        scalarsRegistry.put(boolean.class, Scalars.GraphQLBoolean);

        scalarsRegistry.put(BigInteger.class, JavaScalars.GraphQLBigInteger);

        scalarsRegistry.put(char.class, JavaScalars.GraphQLChar);
        scalarsRegistry.put(Character.class, JavaScalars.GraphQLChar);

        scalarsRegistry.put(Byte.class, JavaScalars.GraphQLByte);
        scalarsRegistry.put(byte.class, JavaScalars.GraphQLByte);

        scalarsRegistry.put(BigDecimal.class, JavaScalars.GraphQLBigDecimal);

        scalarsRegistry.put(LocalDateTime.class, newScalarType("LocalDateTime", "LocalDateTime type", new GraphQLLocalDateTimeCoercing()));
        scalarsRegistry.put(LocalDate.class, newScalarType("LocalDate", "LocalDate type", new GraphQLLocalDateCoercing()));
        scalarsRegistry.put(LocalTime.class, newScalarType("LocalTime", "LocalTime type", new GraphQLLocalTimeCoercing()));
        scalarsRegistry.put(Date.class, newScalarType("Date", "Date type", new GraphQLDateCoercing()));
        scalarsRegistry.put(UUID.class, newScalarType("UUID", "UUID type", new GraphQLUUIDCoercing()));
        scalarsRegistry.put(Object.class, newScalarType("Object", "Object type", new GraphQLObjectCoercing()));
        scalarsRegistry.put(java.sql.Date.class, newScalarType("SqlDate", "SQL Date type", new GraphQLSqlDateCoercing()));
        scalarsRegistry.put(java.sql.Timestamp.class, newScalarType("SqlTimestamp", "SQL Timestamp type", new GraphQLSqlTimestampCoercing()));
        scalarsRegistry.put(Byte[].class, newScalarType("ByteArray", "ByteArray type", new GraphQLLOBCoercing()));
        scalarsRegistry.put(Instant.class, newScalarType("Instant", "Instant type", new GraphQLInstantCoercing()));
        scalarsRegistry.put(ZonedDateTime.class, newScalarType("ZonedDateTime", "ZonedDateTime type", new GraphQLZonedDateTimeCoercing()));
        scalarsRegistry.put(OffsetDateTime.class, newScalarType("OffsetDateTime", "OffsetDateTime type", new GraphQLOffsetDateTimeCoercing()));
    }

    public static GraphQLScalarType of(Class<?> key) {
        return scalarsRegistry.computeIfAbsent(key, JavaScalars::computeGraphQLScalarType);
    }

    protected static GraphQLScalarType computeGraphQLScalarType(Class<?> key) {
        String typeName = key.getSimpleName();
        String description = typeName+" Scalar Object Type";
        
        return newScalarType(typeName, description ,new GraphQLObjectCoercing());
    }

    public static <T extends Coercing> GraphQLScalarType newScalarType(String name, String description, T coercing) {
        return newScalar().name(name)
                          .description(description)
                          .coercing(coercing)
                          .build();
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
            }else if (input instanceof LocalDate) {
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
    };

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
            return LocalDateTime.ofInstant(Instant.ofEpochSecond(input), TimeZone.getDefault().toZoneId()).toLocalDate();
        }

        private LocalDate parseStringToLocalDate(String input) {
            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException e) {
                log.warn("Failed to parse Date from input: " + input, e);
                return null;
            }
        }
    };
    
    public static class GraphQLLocalTimeCoercing implements Coercing<LocalTime, String> {
        
        public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME.withZone(ZoneOffset.UTC);        
        
        private LocalTime convertImpl(Object input) {
            if (input instanceof String) {
                try {
                    return LocalTime.parse((String) input, FORMATTER);
                } catch (DateTimeParseException ignored) {
                }
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

    public static class GraphQLDateCoercing implements Coercing<Object, Object> {
        final String dateFormatString;


        /**
         * Default to pattern 'yyyy-MM-dd'
         */
        public GraphQLDateCoercing() {
        	dateFormatString = "yyyy-MM-dd";
        }

        /**
         * Parse date strings according to the provided SimpleDateFormat pattern
         * 
         * @param dateFormatString e.g. "yyyy-MM-dd'T'HH:mm:ss.SSSXXX" for "2001-07-04T12:08:56.235-07:00"
         */
        public GraphQLDateCoercing(String dateFormatString) {
        	this.dateFormatString = dateFormatString;
        }

        @Override
        public Object serialize(Object input) {
            if (input instanceof String) {
                return parseStringToDate((String) input);
            } else if (input instanceof Date) {
                return new SimpleDateFormat(dateFormatString).format(input);
            } else if (input instanceof Long) {
                return new Date(((Long) input).longValue());
            } else if (input instanceof Integer) {
                return new Date(((Integer) input).longValue());
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
                return parseStringToDate(((StringValue) input).getValue());
            } else if (input instanceof IntValue) {
                BigInteger value = ((IntValue) input).getValue();
                return new Date(value.longValue());
            }
            return null;
        }

        private Date parseStringToDate(String input) {
        	DateFormat df = new SimpleDateFormat(dateFormatString);
        	
            try {
                return df.parse(input);
            } catch (ParseException e) {
                log.warn("Failed to parse Date from input: " + input, e);
                return null;
            }
        }
    };

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
    };

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
    };

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
    };

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
    };

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
    };
    
    public static class GraphQLSqlDateCoercing implements Coercing<Object, Object> {

        @Override
        public Object serialize(Object input) {
            if (input instanceof String) {
                return parseStringToDate((String) input);
            } else if (input instanceof Date) {
                return new java.sql.Date(((Date) input).getTime());
            } else if (input instanceof Long) {
                return new java.sql.Date(((Long) input).longValue());
            } else if (input instanceof Integer) {
                return new java.sql.Date(((Integer) input).longValue());
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
                return parseStringToDate(((StringValue) input).getValue());
            } else if (input instanceof IntValue) {
                BigInteger value = ((IntValue) input).getValue();
                return new java.sql.Date(value.longValue());
            }
            return null;
        }

        private java.sql.Date parseStringToDate(String input) {
            try {
                return new java.sql.Date(DateFormat.getInstance().parse(input).getTime());
            } catch (ParseException e) {
                log.warn("Failed to parse SQL Date from input: " + input, e);
                return null;
            }
        }
    }

    public static class GraphQLSqlTimestampCoercing implements Coercing<Timestamp, Object> {

        private Timestamp doConvert(Object input) {
            if (input instanceof Long) {
                return new Timestamp(Long.class.cast(input));
            } else if (input instanceof String) {
                Instant instant = DateTimeHelper.parseDate(String.class.cast(input));
                
                return Timestamp.from(instant);
            } else if (input instanceof Timestamp) {
                return Timestamp.class.cast(input);
            }
            
            return null;
        }
        
        @Override
        public Object serialize(Object input) {
            Timestamp result = doConvert(input);
            
            if (result == null) {
                throw new CoercingSerializeException("Invalid value '" + input + "' for Timestamp");
            }
            
            return DateTimeFormatter.ISO_INSTANT.format(result.toInstant());
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
            
            if (IntValue.class.isInstance(input)) {
                value = IntValue.class.cast(input).getValue().longValue(); 
            } 
            else if (StringValue.class.isInstance(input)) {
                value = StringValue.class.cast(input).getValue();
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
                return input;
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
                throw new IllegalArgumentException(
                                                   "Expected AST type 'StringValue' but was '" + value + "'.");
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
                return values.stream()
                             .map(v -> parseLiteral(v, variables))
                             .collect(Collectors.toList());
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
    
    public final static class DateTimeHelper {

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
                } catch (java.time.format.DateTimeParseException ignored) {
                }
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
                return OffsetDateTime.parse(date, FORMATTER)
                                     .toInstant();
            }
        }
        
        static class ZonedDateTimeConverter implements Function<String, Instant> {

            static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_ZONED_DATE_TIME.withZone(ZoneOffset.UTC);

            @Override
            public Instant apply(String date) {
                return ZonedDateTime.parse(date, FORMATTER)
                                    .toInstant();
            }
        }
        
        
        static class LocalDateTimeConverter implements Function<String, Instant> {

            static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneOffset.UTC);

            @Override
            public Instant apply(String date) {
                return LocalDateTime.parse(date, FORMATTER)
                                    .toInstant(ZoneOffset.UTC);
            }
        }    

        static class LocalDateConverter implements Function<String, Instant> {

            static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneOffset.UTC);

            @Override
            public Instant apply(String date) {
                LocalDate localDate = LocalDate.parse(date, FORMATTER);

                return localDate.atStartOfDay()
                                .toInstant(ZoneOffset.UTC);
            }
        }            
    }

    /**
     * This represents the "Long" type which is a representation of java.lang.Long
     */
    public static class GraphqlLongCoercing implements Coercing<Long, Long> {

        private Long convertImpl(Object input) {
            if (input instanceof Long) {
                return (Long) input;
            } else if (isNumberIsh(input)) {
                BigDecimal value;
                try {
                    value = new BigDecimal(input.toString());
                } catch (NumberFormatException e) {
                    return null;
                }
                try {
                    return value.longValueExact();
                } catch (ArithmeticException e) {
                    return null;
                }
            } else {
                return null;
            }

        }

        @Override
        public Long serialize(Object input) {
            Long result = convertImpl(input);
            if (result == null) {
                throw new CoercingSerializeException(
                        "Expected type 'Long' but was '" + typeName(input) + "'."
                );
            }
            return result;
        }

        @Override
        public Long parseValue(Object input) {
            Long result = convertImpl(input);
            if (result == null) {
                throw new CoercingParseValueException(
                        "Expected type 'Long' but was '" + typeName(input) + "'."
                );
            }
            return result;
        }

        @Override
        public Long parseLiteral(Object input) {
            if (input instanceof StringValue) {
                try {
                    return Long.parseLong(((StringValue) input).getValue());
                } catch (NumberFormatException e) {
                    throw new CoercingParseLiteralException(
                            "Expected value to be a Long but it was '" + String.valueOf(input) + "'"
                    );
                }
            } else if (input instanceof IntValue) {
                BigInteger value = ((IntValue) input).getValue();
                if (value.compareTo(LONG_MIN) < 0 || value.compareTo(LONG_MAX) > 0) {
                    throw new CoercingParseLiteralException(
                            "Expected value to be in the Long range but it was '" + value.toString() + "'"
                    );
                }
                return value.longValue();
            }
            throw new CoercingParseLiteralException(
                    "Expected AST type 'IntValue' or 'StringValue' but was '" + typeName(input) + "'."
            );
        }
    }

    public static class GraphqlShortCoercing implements Coercing<Short, Short> {

        private Short convertImpl(Object input) {
            if (input instanceof Short) {
                return (Short) input;
            } else if (isNumberIsh(input)) {
                BigDecimal value;
                try {
                    value = new BigDecimal(input.toString());
                } catch (NumberFormatException e) {
                    return null;
                }
                try {
                    return value.shortValueExact();
                } catch (ArithmeticException e) {
                    return null;
                }
            } else {
                return null;
            }

        }

        @Override
        public Short serialize(Object input) {
            Short result = convertImpl(input);
            if (result == null) {
                throw new CoercingSerializeException(
                        "Expected type 'Short' but was '" + typeName(input) + "'."
                );
            }
            return result;
        }

        @Override
        public Short parseValue(Object input) {
            Short result = convertImpl(input);
            if (result == null) {
                throw new CoercingParseValueException(
                        "Expected type 'Short' but was '" + typeName(input) + "'."
                );
            }
            return result;
        }

        @Override
        public Short parseLiteral(Object input) {
            if (!(input instanceof IntValue)) {
                throw new CoercingParseLiteralException(
                        "Expected AST type 'IntValue' but was '" + typeName(input) + "'."
                );
            }
            BigInteger value = ((IntValue) input).getValue();
            if (value.compareTo(SHORT_MIN) < 0 || value.compareTo(SHORT_MAX) > 0) {
                throw new CoercingParseLiteralException(
                        "Expected value to be in the Short range but it was '" + value.toString() + "'"
                );
            }
            return value.shortValue();
        }
    }

    public static class GraphqlBigIntegerCoercing implements Coercing<BigInteger, BigInteger> {

        private BigInteger convertImpl(Object input) {
            if (isNumberIsh(input)) {
                BigDecimal value;
                try {
                    value = new BigDecimal(input.toString());
                } catch (NumberFormatException e) {
                    return null;
                }
                try {
                    return value.toBigIntegerExact();
                } catch (ArithmeticException e) {
                    return null;
                }
            }
            return null;

        }

        @Override
        public BigInteger serialize(Object input) {
            BigInteger result = convertImpl(input);
            if (result == null) {
                throw new CoercingSerializeException(
                        "Expected type 'BigInteger' but was '" + typeName(input) + "'."
                );
            }
            return result;
        }

        @Override
        public BigInteger parseValue(Object input) {
            BigInteger result = convertImpl(input);
            if (result == null) {
                throw new CoercingParseValueException(
                        "Expected type 'BigInteger' but was '" + typeName(input) + "'."
                );
            }
            return result;
        }

        @Override
        public BigInteger parseLiteral(Object input) {
            if (input instanceof StringValue) {
                try {
                    return new BigDecimal(((StringValue) input).getValue()).toBigIntegerExact();
                } catch (NumberFormatException | ArithmeticException e) {
                    throw new CoercingParseLiteralException(
                            "Unable to turn AST input into a 'BigInteger' : '" + String.valueOf(input) + "'"
                    );
                }
            } else if (input instanceof IntValue) {
                return ((IntValue) input).getValue();
            } else if (input instanceof FloatValue) {
                try {
                    return ((FloatValue) input).getValue().toBigIntegerExact();
                } catch (ArithmeticException e) {
                    throw new CoercingParseLiteralException(
                            "Unable to turn AST input into a 'BigInteger' : '" + String.valueOf(input) + "'"
                    );
                }
            }
            throw new CoercingParseLiteralException(
                    "Expected AST type 'IntValue', 'StringValue' or 'FloatValue' but was '" + typeName(input) + "'."
            );
        }
    }

    public static class GraphqlCharCoercing implements Coercing<Character, Character> {

        private Character convertImpl(Object input) {
            if (input instanceof String && ((String) input).length() == 1) {
                return ((String) input).charAt(0);
            } else if (input instanceof Character) {
                return (Character) input;
            } else {
                return null;
            }

        }

        @Override
        public Character serialize(Object input) {
            Character result = convertImpl(input);
            if (result == null) {
                throw new CoercingSerializeException(
                        "Expected type 'Char' but was '" + typeName(input) + "'."
                );
            }
            return result;
        }

        @Override
        public Character parseValue(Object input) {
            Character result = convertImpl(input);
            if (result == null) {
                throw new CoercingParseValueException(
                        "Expected type 'Char' but was '" + typeName(input) + "'."
                );
            }
            return result;
        }

        @Override
        public Character parseLiteral(Object input) {
            if (!(input instanceof StringValue)) {
                throw new CoercingParseLiteralException(
                        "Expected AST type 'StringValue' but was '" + typeName(input) + "'."
                );
            }
            String value = ((StringValue) input).getValue();
            if (value.length() != 1) {
                throw new CoercingParseLiteralException(
                        "Empty 'StringValue' provided."
                );
            }
            return value.charAt(0);
        }
    }

    public static class GraphqlByteCoercing implements Coercing<Byte, Byte> {

        private Byte convertImpl(Object input) {
            if (input instanceof Byte) {
                return (Byte) input;
            } else if (isNumberIsh(input)) {
                BigDecimal value;
                try {
                    value = new BigDecimal(input.toString());
                } catch (NumberFormatException e) {
                    return null;
                }
                try {
                    return value.byteValueExact();
                } catch (ArithmeticException e) {
                    return null;
                }
            } else {
                return null;
            }

        }

        @Override
        public Byte serialize(Object input) {
            Byte result = convertImpl(input);
            if (result == null) {
                throw new CoercingSerializeException(
                        "Expected type 'Byte' but was '" + typeName(input) + "'."
                );
            }
            return result;
        }

        @Override
        public Byte parseValue(Object input) {
            Byte result = convertImpl(input);
            if (result == null) {
                throw new CoercingParseValueException(
                        "Expected type 'Byte' but was '" + typeName(input) + "'."
                );
            }
            return result;
        }

        @Override
        public Byte parseLiteral(Object input) {
            if (!(input instanceof IntValue)) {
                throw new CoercingParseLiteralException(
                        "Expected AST type 'IntValue' but was '" + typeName(input) + "'."
                );
            }
            BigInteger value = ((IntValue) input).getValue();
            if (value.compareTo(BYTE_MIN) < 0 || value.compareTo(BYTE_MAX) > 0) {
                throw new CoercingParseLiteralException(
                        "Expected value to be in the Byte range but it was '" + value.toString() + "'"
                );
            }
            return value.byteValue();
        }
    }

    public static class GraphqlBigDecimalCoercing implements Coercing<BigDecimal, BigDecimal> {

        private BigDecimal convertImpl(Object input) {
            if (isNumberIsh(input)) {
                try {
                    return new BigDecimal(input.toString());
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;

        }

        @Override
        public BigDecimal serialize(Object input) {
            BigDecimal result = convertImpl(input);
            if (result == null) {
                throw new CoercingSerializeException(
                        "Expected type 'BigDecimal' but was '" + typeName(input) + "'."
                );
            }
            return result;
        }

        @Override
        public BigDecimal parseValue(Object input) {
            BigDecimal result = convertImpl(input);
            if (result == null) {
                throw new CoercingParseValueException(
                        "Expected type 'BigDecimal' but was '" + typeName(input) + "'."
                );
            }
            return result;
        }

        @Override
        public BigDecimal parseLiteral(Object input) {
            if (input instanceof StringValue) {
                try {
                    return new BigDecimal(((StringValue) input).getValue());
                } catch (NumberFormatException e) {
                    throw new CoercingParseLiteralException(
                            "Unable to turn AST input into a 'BigDecimal' : '" + String.valueOf(input) + "'"
                    );
                }
            } else if (input instanceof IntValue) {
                return new BigDecimal(((IntValue) input).getValue());
            } else if (input instanceof FloatValue) {
                return ((FloatValue) input).getValue();
            }
            throw new CoercingParseLiteralException(
                    "Expected AST type 'IntValue', 'StringValue' or 'FloatValue' but was '" + typeName(input) + "'."
            );
        }
    }

    static boolean isNumberIsh(Object input) {
        return input instanceof Number || input instanceof String;
    }

    static String typeName(Object input) {
        if (input == null) {
            return "null";
        }

        return input.getClass().getSimpleName();
    }

}
