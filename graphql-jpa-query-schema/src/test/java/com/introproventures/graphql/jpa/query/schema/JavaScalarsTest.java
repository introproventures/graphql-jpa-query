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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;

import org.junit.Test;

import com.introproventures.graphql.jpa.query.schema.JavaScalars;

import graphql.schema.Coercing;

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

    public void long2LocalDate() {
        // given
        Coercing<?,?> coercing = JavaScalars.of(LocalDate.class).getCoercing();
        LocalDateTime localDateTime = LocalDateTime.of(2017, 02, 02, 0, 0, 0);
        long input = localDateTime.toEpochSecond(ZoneId.systemDefault().getRules().getOffset(localDateTime));

        //when
        Object result = coercing.serialize(input);

        //then
        assertThat(result).isInstanceOf(LocalDateTime.class);

        LocalDateTime resultLDT = (LocalDateTime) result;
        
        assert resultLDT.getDayOfMonth() == 2;
        assert resultLDT.getMonth() == Month.FEBRUARY;
        assert resultLDT.getYear() == 2017;
    }

    public void string2LocalDate() {
        //given
        Coercing<?,?> coercing = JavaScalars.of(LocalDate.class).getCoercing();
        final String input = "2017-02-02";

        //when
        Object result = coercing.serialize(input);

        //then
        assertThat(result).isInstanceOf(LocalDateTime.class);

        LocalDateTime resultLDT = (LocalDateTime) result;
        
        assert resultLDT.getDayOfMonth() == 2;
        assert resultLDT.getMonth() == Month.FEBRUARY;
        assert resultLDT.getYear() == 2017;
    }
}
