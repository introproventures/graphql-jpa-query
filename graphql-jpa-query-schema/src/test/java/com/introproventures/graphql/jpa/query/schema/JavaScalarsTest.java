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

import java.time.*;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.introproventures.graphql.jpa.query.converter.model.VariableValue;
import com.introproventures.graphql.jpa.query.schema.JavaScalars.GraphQLObjectCoercing;
import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

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
        assertThat(coercing.serialize(LocalTime.of(17, 59, 59, (int) TimeUnit.MILLISECONDS.toNanos(277)))).isEqualTo("17:59:59.277");
    }

    @Test(expected = CoercingSerializeException.class)
    public void testLocalTimeSerializeInvalidValue() {
        //given
        Coercing<?, ?> coercing = JavaScalars.of(LocalTime.class).getCoercing();

        //then
        coercing.serialize("");
        coercing.serialize("not a time");
        coercing.serialize(new Object());

        fail("Should throw CoercingSerializeException");
    }

    @Test
    public void testLocalTimeParseValue() {
        //given
        Coercing<?, ?> coercing = JavaScalars.of(LocalTime.class).getCoercing();

        //then
        assertThat(coercing.parseValue("00:00:00")).isEqualTo(LocalTime.MIDNIGHT);
        assertThat(coercing.parseValue("10:15:30")).isEqualTo(LocalTime.of(10, 15, 30));
        assertThat(coercing.parseValue("17:59:59")).isEqualTo(LocalTime.of(17, 59, 59));
        assertThat(coercing.parseValue("17:59:59.277")).isEqualTo(LocalTime.of(17, 59, 59, (int) TimeUnit.MILLISECONDS.toNanos(277)));
    }

    @Test(expected = CoercingParseValueException.class)
    public void testLocalTimeParseValueInvlidValue() {
        //given
        Coercing<?, ?> coercing = JavaScalars.of(LocalTime.class).getCoercing();

        //then
        coercing.parseValue("");
        coercing.parseValue("not a time");
        coercing.parseValue(new Object());

        fail("Should throw CoercingParseValueException");
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
        JavaScalars.register(Map.class, new GraphQLScalarType("Map", "Map Object Type", new GraphQLObjectCoercing()));

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
        assertThat(result).isInstanceOf(Instant.class);

        OffsetDateTime resultLDT = OffsetDateTime.ofInstant((Instant) result, ZoneOffset.UTC);

        assert resultLDT.getYear() == 2019;
        assert resultLDT.getDayOfMonth() == 05;
        assert resultLDT.getMonth() == Month.AUGUST;
        assert resultLDT.getYear() == 2019;
        assert resultLDT.getHour() == 07;
        assert resultLDT.getMinute() == 15;
        assert resultLDT.getSecond() == 07;
    }
}
