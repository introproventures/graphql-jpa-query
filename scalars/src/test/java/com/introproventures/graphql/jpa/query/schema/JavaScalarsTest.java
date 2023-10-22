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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.introproventures.graphql.jpa.query.schema.JavaScalars.GraphQLObjectCoercing;
import com.introproventures.graphql.jpa.query.schema.fixtures.VariableValue;
import graphql.language.BooleanValue;
import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactory;
import org.junit.jupiter.api.Test;

public class JavaScalarsTest {

    @Test
    public void long2LocalDateTime() {
        //given
        Coercing<?, ?> coercing = JavaScalars.of(LocalDateTime.class).getCoercing();

        LocalDateTime localDateTime = LocalDateTime.of(2017, 02, 02, 12, 30, 15);
        long input = localDateTime.toEpochSecond(ZoneId.systemDefault().getRules().getOffset(localDateTime));

        //when
        Object result = coercing.serialize(input);

        //then
        assertThat(result).isInstanceOf(LocalDateTime.class);

        LocalDateTime resultLDT = (LocalDateTime) result;

        assert resultLDT.getDayOfMonth() == 2;
        assert resultLDT.getMonth() == Month.FEBRUARY;
        assert resultLDT.getYear() == 2017;
        assert resultLDT.getHour() == 12;
        assert resultLDT.getMinute() == 30;
        assert resultLDT.getSecond() == 15;
    }

    @Test
    public void string2LocalDateTime() {
        //given
        Coercing<?, ?> coercing = JavaScalars.of(LocalDateTime.class).getCoercing();
        final String input = "2017-02-02T12:30:15";

        //when
        Object result = coercing.serialize(input);

        //then
        assertThat(result).isInstanceOf(LocalDateTime.class);

        LocalDateTime resultLDT = (LocalDateTime) result;

        assert resultLDT.getDayOfMonth() == 2;
        assert resultLDT.getMonth() == Month.FEBRUARY;
        assert resultLDT.getYear() == 2017;
        assert resultLDT.getHour() == 12;
        assert resultLDT.getMinute() == 30;
        assert resultLDT.getSecond() == 15;
    }

    @Test
    public void long2LocalDate() {
        // given
        Coercing<?, ?> coercing = JavaScalars.of(LocalDate.class).getCoercing();
        LocalDateTime localDateTime = LocalDateTime.of(2017, 02, 02, 0, 0, 0);
        long input = localDateTime.toEpochSecond(ZoneId.systemDefault().getRules().getOffset(localDateTime));

        //when
        Object result = coercing.serialize(input);

        //then
        assertThat(result).isInstanceOf(LocalDate.class);

        LocalDate resultLDT = (LocalDate) result;

        assert resultLDT.getDayOfMonth() == 2;
        assert resultLDT.getMonth() == Month.FEBRUARY;
        assert resultLDT.getYear() == 2017;
    }

    @Test
    public void string2LocalDate() {
        //given
        Coercing<?, ?> coercing = JavaScalars.of(LocalDate.class).getCoercing();
        final String input = "2017-02-02";

        //when
        Object result = coercing.serialize(input);

        //then
        assertThat(result).isInstanceOf(LocalDate.class);

        LocalDate resultLDT = (LocalDate) result;

        assert resultLDT.getDayOfMonth() == 2;
        assert resultLDT.getMonth() == Month.FEBRUARY;
        assert resultLDT.getYear() == 2017;
    }

    @Test
    public void testLocalTimeParseLiteralValue() {
        //given
        Coercing<?, ?> coercing = JavaScalars.of(LocalTime.class).getCoercing();

        //then
        assertThat(coercing.parseLiteral(new StringValue("00:00:00"))).isEqualTo(LocalTime.MIDNIGHT);
        assertThat(coercing.parseLiteral(new StringValue("10:15:30"))).isEqualTo(LocalTime.of(10, 15, 30));
        assertThat(coercing.parseLiteral(new StringValue("17:59:59"))).isEqualTo(LocalTime.of(17, 59, 59));
        assertThat(coercing.parseLiteral(new StringValue("not a time"))).isNull();
    }

    @Test
    public void testLocalTimeSerializeValue() {
        //given
        Coercing<?, ?> coercing = JavaScalars.of(LocalTime.class).getCoercing();

        //then
        assertThat(coercing.serialize(LocalTime.MIDNIGHT)).isEqualTo("00:00:00");
        assertThat(coercing.serialize(LocalTime.of(10, 15, 30))).isEqualTo("10:15:30");
        assertThat(coercing.serialize(LocalTime.of(17, 59, 59))).isEqualTo("17:59:59");
        assertThat(coercing.serialize(LocalTime.of(17, 59, 59, (int) TimeUnit.MILLISECONDS.toNanos(277))))
            .isEqualTo("17:59:59.277");
    }

    @Test
    public void testLocalTimeSerializeInvalidValue() {
        //given
        Coercing<?, ?> coercing = JavaScalars.of(LocalTime.class).getCoercing();

        //then
        assertThat(catchThrowable(() -> coercing.serialize(""))).isInstanceOf(CoercingSerializeException.class);
        assertThat(catchThrowable(() -> coercing.serialize("not a time")))
            .isInstanceOf(CoercingSerializeException.class);
        assertThat(catchThrowable(() -> coercing.serialize(new Object())))
            .isInstanceOf(CoercingSerializeException.class);
    }

    @Test
    public void testLocalTimeParseValue() {
        //given
        Coercing<?, ?> coercing = JavaScalars.of(LocalTime.class).getCoercing();

        //then
        assertThat(coercing.parseValue("00:00:00")).isEqualTo(LocalTime.MIDNIGHT);
        assertThat(coercing.parseValue("10:15:30")).isEqualTo(LocalTime.of(10, 15, 30));
        assertThat(coercing.parseValue("17:59:59")).isEqualTo(LocalTime.of(17, 59, 59));
        assertThat(coercing.parseValue("17:59:59.277"))
            .isEqualTo(LocalTime.of(17, 59, 59, (int) TimeUnit.MILLISECONDS.toNanos(277)));
    }

    @Test
    public void testLocalTimeParseValueInvlidValue() {
        //given
        Coercing<?, ?> coercing = JavaScalars.of(LocalTime.class).getCoercing();

        //then
        assertThat(catchThrowable(() -> coercing.parseValue(""))).isInstanceOf(CoercingParseValueException.class);
        assertThat(catchThrowable(() -> coercing.parseValue("not a time")))
            .isInstanceOf(CoercingParseValueException.class);
        assertThat(catchThrowable(() -> coercing.parseValue(new Object())))
            .isInstanceOf(CoercingParseValueException.class);
    }

    @Test
    public void testNonExistingJavaScalarShouldDefaultToObjectCoercing() {
        //given
        GraphQLScalarType scalarType = JavaScalars.of(VariableValue.class);

        //then
        Coercing<?, ?> coercing = scalarType.getCoercing();

        assertThat(coercing).isInstanceOf(GraphQLObjectCoercing.class);
        assertThat(scalarType.getName()).isEqualTo("VariableValue");
    }

    @Test
    public void testRegisterJavaScalarWithObjectCoercing() {
        //given
        GraphQLScalarType graphQLScalarType = JavaScalars.newScalarType(
            "Map",
            "Map Object Type",
            new GraphQLObjectCoercing()
        );
        JavaScalars.register(Map.class, graphQLScalarType);

        //when
        GraphQLScalarType scalarType = JavaScalars.of(Map.class);

        //then
        Coercing<?, ?> coercing = scalarType.getCoercing();

        assertThat(coercing).isInstanceOf(GraphQLObjectCoercing.class);
        assertThat(scalarType.getName()).isEqualTo("Map");
    }

    @Test
    public void string2OffsetDateTime() {
        //given
        Coercing<?, ?> coercing = JavaScalars.of(OffsetDateTime.class).getCoercing();
        final String input = "2017-02-02T12:30:15+07:00";

        //when
        Object result = coercing.serialize(input);

        //then
        assertThat(result).isInstanceOf(OffsetDateTime.class);

        OffsetDateTime resultLDT = (OffsetDateTime) result;

        assert resultLDT.getDayOfMonth() == 2;
        assert resultLDT.getMonth() == Month.FEBRUARY;
        assert resultLDT.getYear() == 2017;
        assert resultLDT.getHour() == 12;
        assert resultLDT.getMinute() == 30;
        assert resultLDT.getSecond() == 15;
        assert resultLDT.getOffset() == ZoneOffset.of("+07:00");
    }

    @Test
    public void string2ZonedDateTime() {
        //given
        Coercing<?, ?> coercing = JavaScalars.of(ZonedDateTime.class).getCoercing();
        final String input = "2019-08-05T13:47:57.428260700+07:00[Asia/Bangkok]";

        //when
        Object result = coercing.serialize(input);

        //then
        assertThat(result).isInstanceOf(ZonedDateTime.class);

        ZonedDateTime resultLDT = (ZonedDateTime) result;

        assert resultLDT.getDayOfMonth() == 05;
        assert resultLDT.getMonth() == Month.AUGUST;
        assert resultLDT.getYear() == 2019;
        assert resultLDT.getHour() == 13;
        assert resultLDT.getMinute() == 47;
        assert resultLDT.getSecond() == 57;
        assert resultLDT.getNano() == 428260700;
        assert resultLDT.getOffset() == ZoneOffset.of("+07:00");
    }

    @Test
    public void string2Instant() {
        //given
        Coercing<?, ?> coercing = JavaScalars.of(Instant.class).getCoercing();
        final String input = "2019-08-05T07:15:07.199582Z";
        Instant instant = Instant.parse(input);

        //        when
        Object result = coercing.serialize(instant);

        //then
        assertThat(result).isInstanceOf(String.class);

        OffsetDateTime resultLDT = OffsetDateTime.ofInstant(Instant.parse(result.toString()), ZoneOffset.UTC);

        assert resultLDT.getYear() == 2019;
        assert resultLDT.getDayOfMonth() == 05;
        assert resultLDT.getMonth() == Month.AUGUST;
        assert resultLDT.getHour() == 07;
        assert resultLDT.getMinute() == 15;
        assert resultLDT.getSecond() == 07;
    }

    @Test
    public void testTimestampSerialize() {
        //given
        Coercing<?, ?> subject = JavaScalars.of(Timestamp.class).getCoercing();
        Instant expected = Instant.parse("2019-08-05T07:15:07Z");

        final Timestamp input = new Timestamp(expected.toEpochMilli());

        //when
        Object result = subject.serialize(input);

        //then
        assertThat(result).asString().isEqualTo(expected.toString());

        //when
        result = subject.serialize(expected.toString());

        //then
        assertThat(result).asString().isEqualTo(expected.toString());

        //when
        result = subject.serialize(expected.toEpochMilli());

        //then
        assertThat(result).asString().isEqualTo(expected.toString());
    }

    @Test
    public void testTimestampParseValue() {
        //given
        Coercing<?, ?> coercing = JavaScalars.of(Timestamp.class).getCoercing();
        Instant instant = Instant.parse("2019-08-05T07:15:07Z");
        Timestamp expected = new Timestamp(instant.toEpochMilli());

        //when
        Object result = coercing.parseValue(instant.toString());

        //then
        assertThat(result)
            .asInstanceOf(new InstanceOfAssertFactory<>(Timestamp.class, Assertions::assertThat))
            .isEqualTo(expected);
    }

    @Test
    public void testTimestampParseLiteralStringValue() {
        //given
        Coercing<?, ?> coercing = JavaScalars.of(Timestamp.class).getCoercing();
        Instant instant = Instant.parse("2019-08-05T07:15:07Z");
        Timestamp expected = new Timestamp(instant.toEpochMilli());
        StringValue input = StringValue.newStringValue(instant.toString()).build();

        //when
        Object result = coercing.parseLiteral(input);

        //then
        assertThat(result)
            .asInstanceOf(new InstanceOfAssertFactory<>(Timestamp.class, Assertions::assertThat))
            .isEqualTo(expected);
    }

    @Test
    public void testTimestampParseLiteralWrongValue() {
        //given
        Coercing<?, ?> coercing = new JavaScalars.GraphQLTimestampCoercing();
        Object input = Boolean.valueOf("true");

        //when
        assertThat(catchThrowable(() -> coercing.parseLiteral(input)))
            .isInstanceOf(CoercingParseLiteralException.class);
    }

    @Test
    public void testTimestampParseValueWrongValue() {
        //given
        Coercing<?, ?> coercing = new JavaScalars.GraphQLTimestampCoercing();
        Object input = Boolean.valueOf("true");

        //when
        assertThat(catchThrowable(() -> coercing.parseValue(input))).isInstanceOf(CoercingParseValueException.class);
    }

    @Test
    public void testTimestampSerializeWrongValue() {
        //given
        Coercing<?, ?> coercing = new JavaScalars.GraphQLTimestampCoercing();
        Object input = BooleanValue.newBooleanValue(true).build();

        //when
        assertThat(catchThrowable(() -> coercing.serialize(input))).isInstanceOf(CoercingSerializeException.class);
    }

    @Test
    public void testTimestampParseLiteralIntValue() {
        //given
        Coercing<?, ?> coercing = JavaScalars.of(Timestamp.class).getCoercing();
        Instant instant = Instant.parse("2019-08-05T07:15:07Z");
        Timestamp expected = new Timestamp(instant.toEpochMilli());
        IntValue input = IntValue.newIntValue(BigInteger.valueOf(instant.toEpochMilli())).build();

        //when
        Object result = coercing.parseLiteral(input);

        //then
        assertThat(result)
            .asInstanceOf(new InstanceOfAssertFactory<>(Timestamp.class, Assertions::assertThat))
            .isEqualTo(expected);
    }

    @Test
    public void testTimestampParseLiteralStringValueOffsetDateTime() {
        //given
        Coercing<?, ?> coercing = JavaScalars.of(Timestamp.class).getCoercing();
        OffsetDateTime offsetDateTime = OffsetDateTime.parse("2020-09-06T11:45:27-07:00");
        Instant instant = offsetDateTime.toInstant();

        Timestamp expected = new Timestamp(instant.toEpochMilli());
        StringValue input = StringValue.newStringValue(offsetDateTime.toString()).build();

        //when
        Object result = coercing.parseLiteral(input);

        //then
        assertThat(result)
            .asInstanceOf(new InstanceOfAssertFactory<>(Timestamp.class, Assertions::assertThat))
            .isEqualTo(expected);
    }

    @Test
    public void testTimestampParseLiteralStringValueZonedDateTime() {
        //given
        Coercing<?, ?> coercing = JavaScalars.of(Timestamp.class).getCoercing();
        ZonedDateTime zonedDateTime = ZonedDateTime.parse("2020-09-06T11:45:27-07:00[America/Los_Angeles]");
        Instant instant = zonedDateTime.toInstant();

        Timestamp expected = new Timestamp(instant.toEpochMilli());
        StringValue input = StringValue.newStringValue(zonedDateTime.toString()).build();

        //when
        Object result = coercing.parseLiteral(input);

        //then
        assertThat(result)
            .asInstanceOf(new InstanceOfAssertFactory<>(Timestamp.class, Assertions::assertThat))
            .isEqualTo(expected);
    }

    @Test
    public void testTimestampParseLiteralStringValueLocalDateTime() {
        //given
        Coercing<?, ?> coercing = new JavaScalars.GraphQLTimestampCoercing();
        LocalDateTime localDateTime = LocalDateTime.parse(
            "2019-08-05T07:15:07",
            DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.of("UTC"))
        );
        Instant instant = localDateTime.toInstant(ZoneOffset.UTC);

        Timestamp expected = new Timestamp(instant.toEpochMilli());
        StringValue input = StringValue.newStringValue(localDateTime.toString()).build();

        //when
        Object result = coercing.parseLiteral(input);

        //then
        assertThat(result)
            .asInstanceOf(new InstanceOfAssertFactory<>(Timestamp.class, Assertions::assertThat))
            .isEqualTo(expected);
    }

    @Test
    public void testTimestampParseLiteralStringValueLocalDate() {
        //given
        Coercing<?, ?> coercing = new JavaScalars.GraphQLTimestampCoercing();
        LocalDate localDate = LocalDate.parse(
            "2019-08-05",
            DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneId.of("UTC"))
        );
        Instant instant = localDate.atStartOfDay(ZoneId.of("UTC")).toInstant();

        Timestamp expected = new Timestamp(instant.toEpochMilli());
        StringValue input = StringValue.newStringValue(localDate.toString()).build();

        //when
        Object result = coercing.parseLiteral(input);

        //then
        assertThat(result)
            .asInstanceOf(new InstanceOfAssertFactory<>(Timestamp.class, Assertions::assertThat))
            .isEqualTo(expected);
    }

    @Test
    public void dateCoercionThreadSafe() throws InterruptedException, ExecutionException {
        //given
        String dateLiteral = "2018-06-22T10:00:00";
        Coercing<?, ?> subject = new JavaScalars.GraphQLDateCoercing();

        List<CompletableFuture<Object>> dates = new ArrayList<>();

        //when
        for (int i = 0; i < 1000; i++) {
            CompletableFuture<Object> task = CompletableFuture.supplyAsync(() -> subject.serialize(dateLiteral));
            dates.add(task);
        }

        CompletableFuture<Void> result = CompletableFuture.allOf(dates.toArray(new CompletableFuture[] {}));

        result.join();

        //then
        assertThat(result.isCompletedExceptionally()).isFalse();
    }

    @Test
    public void shouldParseJavaDate() throws InterruptedException, ExecutionException {
        //given
        Coercing<?, ?> subject = new JavaScalars.GraphQLDateCoercing("yyyy-MM-dd'T'HH:mm:ss.SSSX");

        // when
        Object result = subject.parseValue(new Date(Instant.EPOCH.toEpochMilli()));

        // then
        assertThat(result).isEqualTo("1970-01-01T00:00:00.000Z");
    }

    @Test
    public void shouldParseSqlDate() throws InterruptedException, ExecutionException {
        //given
        Coercing<?, ?> subject = new JavaScalars.GraphQLDateCoercing("yyyy-MM-dd'T'HH:mm:ss.SSSX");

        // when
        Object result = subject.parseValue(java.sql.Date.from(Instant.EPOCH));

        // then
        assertThat(result).isEqualTo("1970-01-01T00:00:00.000Z");
    }

    @Test
    public void testTimeParseLiteralStringValue() {
        //given
        Coercing<?, ?> coercing = new JavaScalars.GraphQLTimeCoercing();
        Time time = Time.valueOf("12:00:00");

        StringValue input = StringValue.newStringValue(time.toString()).build();

        //when
        Object result = coercing.parseLiteral(input);

        //then
        assertThat(result)
            .asInstanceOf(new InstanceOfAssertFactory<>(Time.class, Assertions::assertThat))
            .isEqualTo(time);
    }

    @Test
    public void testTimeParseLiteralIntegerValue() {
        //given
        Coercing<?, ?> coercing = new JavaScalars.GraphQLTimeCoercing();
        Time time = Time.valueOf("12:00:00");

        IntValue input = IntValue.newIntValue(BigInteger.valueOf(time.getTime())).build();

        //when
        Object result = coercing.parseLiteral(input);

        //then
        assertThat(result)
            .asInstanceOf(new InstanceOfAssertFactory<>(Time.class, Assertions::assertThat))
            .isEqualTo(time);
    }

    @Test
    public void testTimeSerializeValue() {
        //given
        Coercing<?, ?> coercing = JavaScalars.GraphQLTimeScalar.getCoercing();

        //then
        assertThat(coercing.serialize(Time.valueOf(LocalTime.MIDNIGHT))).isEqualTo("00:00:00");
        assertThat(coercing.serialize(Time.valueOf(LocalTime.of(10, 15, 30)))).isEqualTo("10:15:30");
        assertThat(coercing.serialize(Time.valueOf(LocalTime.of(17, 59, 59)))).isEqualTo("17:59:59");
        assertThat(coercing.serialize(Time.valueOf(LocalTime.of(17, 59, 59, (int) TimeUnit.MILLISECONDS.toNanos(277)))))
            .isEqualTo("17:59:59");
    }
}
