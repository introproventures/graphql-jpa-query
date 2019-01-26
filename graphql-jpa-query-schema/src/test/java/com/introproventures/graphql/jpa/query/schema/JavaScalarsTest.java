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
import static org.assertj.core.api.Assertions.fail;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import org.junit.Test;

public class JavaScalarsTest {

    @Test
    public void long2LocalDateTime() {
        //given
        Coercing<?,?> coercing = JavaScalars.of(LocalDateTime.class).getCoercing();

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
        Coercing<?,?> coercing = JavaScalars.of(LocalDateTime.class).getCoercing();
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
        Coercing<?,?> coercing = JavaScalars.of(LocalDate.class).getCoercing();
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
        Coercing<?,?> coercing = JavaScalars.of(LocalDate.class).getCoercing();
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
        Coercing<?,?> coercing = JavaScalars.of(LocalTime.class).getCoercing();

        //then
        assertThat(coercing.parseLiteral(new StringValue("00:00:00"))).isEqualTo(LocalTime.MIDNIGHT);
        assertThat(coercing.parseLiteral(new StringValue("10:15:30"))).isEqualTo(LocalTime.of(10, 15, 30));
        assertThat(coercing.parseLiteral(new StringValue("17:59:59"))).isEqualTo(LocalTime.of(17, 59, 59));
        assertThat(coercing.parseLiteral(new StringValue("not a time"))).isNull();
    }
    
    @Test
    public void testLocalTimeSerializeValue() {
        //given
        Coercing<?,?> coercing = JavaScalars.of(LocalTime.class).getCoercing();

        //then
        assertThat(coercing.serialize(LocalTime.MIDNIGHT)).isEqualTo("00:00:00");
        assertThat(coercing.serialize(LocalTime.of(10, 15, 30))).isEqualTo("10:15:30");
        assertThat(coercing.serialize(LocalTime.of(17, 59, 59))).isEqualTo("17:59:59");
        assertThat(coercing.serialize(LocalTime.of(17, 59, 59, (int) TimeUnit.MILLISECONDS.toNanos(277)))).isEqualTo("17:59:59.277");
    }
    
    @Test(expected=CoercingSerializeException.class)
    public void testLocalTimeSerializeInvalidValue() {
        //given
        Coercing<?,?> coercing = JavaScalars.of(LocalTime.class).getCoercing();

        //then
        coercing.serialize("");
        coercing.serialize("not a time");
        coercing.serialize(new Object());
        
        fail("Should throw CoercingSerializeException");
    }    
    
    @Test
    public void testLocalTimeParseValue() {
        //given
        Coercing<?,?> coercing = JavaScalars.of(LocalTime.class).getCoercing();

        //then
        assertThat(coercing.parseValue("00:00:00")).isEqualTo(LocalTime.MIDNIGHT);
        assertThat(coercing.parseValue("10:15:30")).isEqualTo(LocalTime.of(10, 15, 30));
        assertThat(coercing.parseValue("17:59:59")).isEqualTo(LocalTime.of(17, 59, 59));
        assertThat(coercing.parseValue("17:59:59.277")).isEqualTo(LocalTime.of(17, 59, 59, (int) TimeUnit.MILLISECONDS.toNanos(277)));
    }

    @Test(expected=CoercingParseValueException.class)
    public void testLocalTimeParseValueInvlidValue() {
        //given
        Coercing<?,?> coercing = JavaScalars.of(LocalTime.class).getCoercing();

        //then
        //then
        coercing.parseValue("");
        coercing.parseValue("not a time");
        coercing.parseValue(new Object());
        
        fail("Should throw CoercingParseValueException");
    }
}
