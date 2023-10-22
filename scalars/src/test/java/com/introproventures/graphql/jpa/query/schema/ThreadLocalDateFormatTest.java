package com.introproventures.graphql.jpa.query.schema;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Date;
import java.text.ParseException;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ThreadLocalDateFormatTest {

    ThreadLocalDateFormat threadLocalDateFormat = new ThreadLocalDateFormat("yyyy-MM-dd");

    @Test
    void parse() throws ParseException {
        // given
        String dateString = "1970-01-01";

        // when
        var date = threadLocalDateFormat.parse(dateString);

        // then
        assertThat(date).isEqualTo(Date.from(Instant.EPOCH));
    }

    @Test
    void format() {
        // given
        var date = Date.from(Instant.EPOCH);

        // when
        var dateString = threadLocalDateFormat.format(date);

        // then
        assertThat(dateString).isEqualTo("1970-01-01");
    }
}
