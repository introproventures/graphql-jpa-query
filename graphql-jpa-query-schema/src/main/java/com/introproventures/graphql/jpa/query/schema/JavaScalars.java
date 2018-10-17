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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graphql.Assert;
import graphql.Scalars;
import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.GraphQLScalarType;

/**
 * Provides Registry to resolve GraphQL Query Java Scalar Types
 *
 * @author Igor Dianov
 *
 */
public class JavaScalars {

    static final Logger log = LoggerFactory.getLogger(JavaScalars.class);

    private static HashMap<Class<?>, GraphQLScalarType> scalarsRegistry = new HashMap<Class<?>, GraphQLScalarType>();

    static {
        scalarsRegistry.put(String.class, Scalars.GraphQLString);

        scalarsRegistry.put(Integer.class, Scalars.GraphQLInt);
        scalarsRegistry.put(int.class, Scalars.GraphQLInt);

        scalarsRegistry.put(Short.class, Scalars.GraphQLShort);
        scalarsRegistry.put(short.class, Scalars.GraphQLShort);

        scalarsRegistry.put(Float.class, Scalars.GraphQLFloat);
        scalarsRegistry.put(float.class, Scalars.GraphQLFloat);

        scalarsRegistry.put(Double.class, Scalars.GraphQLFloat);
        scalarsRegistry.put(double.class, Scalars.GraphQLFloat);

        scalarsRegistry.put(Long.class, Scalars.GraphQLLong);
        scalarsRegistry.put(long.class, Scalars.GraphQLLong);

        scalarsRegistry.put(Boolean.class, Scalars.GraphQLBoolean);
        scalarsRegistry.put(boolean.class, Scalars.GraphQLBoolean);

        scalarsRegistry.put(BigInteger.class, Scalars.GraphQLBigInteger);

        scalarsRegistry.put(char.class, Scalars.GraphQLChar);
        scalarsRegistry.put(Character.class, Scalars.GraphQLChar);

        scalarsRegistry.put(Byte.class, Scalars.GraphQLByte);
        scalarsRegistry.put(byte.class, Scalars.GraphQLByte);

        scalarsRegistry.put(BigDecimal.class, Scalars.GraphQLBigDecimal);

        scalarsRegistry.put(LocalDateTime.class, new GraphQLScalarType("LocalDateTime", "LocalDateTime type", new GraphQLLocalDateTimeCoercing()));
        scalarsRegistry.put(LocalDate.class, new GraphQLScalarType("LocalDate", "LocalDate type", new GraphQLLocalDateCoercing()));
        scalarsRegistry.put(Date.class, new GraphQLScalarType("Date", "Date type", new GraphQLDateCoercing()));
        scalarsRegistry.put(UUID.class, new GraphQLScalarType("UUID", "UUID type", new GraphQLUUIDCoercing()));
        scalarsRegistry.put(Object.class, new GraphQLScalarType("Object", "Object type", new GraphQLObjectCoercing()));
    }

    public static GraphQLScalarType of(Class<?> key) {
        return scalarsRegistry.get(key);
    }

    public JavaScalars register(Class<?> key, GraphQLScalarType value) {
        Assert.assertNotNull(key, "key parameter cannot be null.");
        Assert.assertNotNull(value, "value parameter cannot be null.");

        scalarsRegistry.put(key, value);

        return this;
    }

    public static class GraphQLLocalDateTimeCoercing implements Coercing<Object, Object> {

        @Override
        public Object serialize(Object input) {
            if (input instanceof String) {
                return parseStringToLocalDateTime((String) input);
            } else if (input instanceof LocalDateTime) {
                return input;
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

        @Override
        public Object serialize(Object input) {
            if (input instanceof String) {
                return parseStringToLocalDate((String) input);
            } else if (input instanceof LocalDate) {
                return input;
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

    public static class GraphQLDateCoercing implements Coercing<Object, Object> {
        final DateFormat df;


        /**
         * Parse date strings matching DateFormat's locale-sensitive SHORT pattern,
         * see: https://docs.oracle.com/javase/tutorial/i18n/format/dateFormat.html 
         */
        public GraphQLDateCoercing() {
            df = DateFormat.getInstance();
        }

        /**
         * Parse date strings according to the provided SimpleDateFormat pattern
         * 
         * @param dateFormatString e.g. "yyyy-MM-dd'T'HH:mm:ss.SSSXXX" for "2001-07-04T12:08:56.235-07:00"
         */
        public GraphQLDateCoercing(String dateFormatString) {
            df = new SimpleDateFormat(dateFormatString);
        }

        @Override
        public Object serialize(Object input) {
            if (input instanceof String) {
                return parseStringToDate((String) input);
            } else if (input instanceof Date) {
                return input;
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
            try {
                return df.parse(input);
            } catch (ParseException e) {
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

    public static class GraphQLObjectCoercing implements Coercing<Object, Object> {

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

}
