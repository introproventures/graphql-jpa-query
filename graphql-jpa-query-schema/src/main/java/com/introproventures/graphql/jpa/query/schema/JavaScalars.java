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
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.Collectors;

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
    
    private static JavaScalars instance = new JavaScalars();

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
        scalarsRegistry.put(java.sql.Date.class, new GraphQLScalarType("SqlDate", "SQL Date type", new GraphQLSqlDateCoercing()));
        scalarsRegistry.put(java.sql.Timestamp.class, new GraphQLScalarType("SqlTimestamp", "SQL Timestamp type", new GraphQLSqlTimestampCoercing()));
        scalarsRegistry.put(Byte[].class, new GraphQLScalarType("ByteArray", "ByteArray type", new GraphQLLOBCoercing()));    
    }

    public static GraphQLScalarType of(Class<?> key) {
        return scalarsRegistry.get(key);
    }

    public static JavaScalars register(Class<?> key, GraphQLScalarType value) {
        Assert.assertNotNull(key, "key parameter cannot be null.");
        Assert.assertNotNull(value, "value parameter cannot be null.");

        scalarsRegistry.put(key, value);

        return instance;
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
                return new SimpleDateFormat("yyyy-MM-dd").parse(input);
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
    public static class GraphQLSqlTimestampCoercing implements Coercing<Object, Object> {
        @Override
       public Object serialize(Object input) {
           if (input instanceof String) {
               return parseStringToTimestamp((String) input);
           } else if (input instanceof Date) {
               return new java.sql.Timestamp(((Date) input).getTime());
           } else if (input instanceof Long) {
               return new java.sql.Timestamp(((Long) input).longValue());
           } else if (input instanceof Integer) {
               return new java.sql.Timestamp(((Integer) input).longValue());
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
               return parseStringToTimestamp(((StringValue) input).getValue());
           } else if (input instanceof IntValue) {
               BigInteger value = ((IntValue) input).getValue();
               return new java.sql.Date(value.longValue());
           }
           return null;
       }
        private java.sql.Timestamp parseStringToTimestamp(String input) {
           try {
               return new java.sql.Timestamp(DateFormat.getInstance().parse(input).getTime());
           } catch (ParseException e) {
               log.warn("Failed to parse Timestamp from input: " + input, e);
               return null;
           }
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
            return parseFieldValue((Value) input,  Collections.emptyMap());
        }

        //recursively parse the input into a Map
        private Object parseFieldValue(Object value, Map<String, Object> variables) {
            if (!(value instanceof Value)) {
                throw new IllegalArgumentException(
                        "Expected AST type 'StringValue' but was '" + value + "'."
                );
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
            if (value instanceof ArrayValue) {
            	List<Value> values = ((ArrayValue) value).getValues();
                return values.stream()
                        .map(v -> parseFieldValue(v, variables))
                        .collect(Collectors.toList());
            }
            if (value instanceof ObjectValue) {
            	List<ObjectField> values = ((ObjectValue) value).getObjectFields();
                Map<String, Object> parsedValues = new LinkedHashMap<>();
                
                values.forEach(field -> {
                    Object parsedValue = parseFieldValue(field.getValue(), variables);
                    parsedValues.put(field.getName(), parsedValue);
                });
                return parsedValues;
            }
            
            //Should never happen, as it would mean the variable was not replaced by the parser
            throw new IllegalArgumentException("Unsupported scalar value type: " + value.getClass().getName());
        }

    }
    

}
