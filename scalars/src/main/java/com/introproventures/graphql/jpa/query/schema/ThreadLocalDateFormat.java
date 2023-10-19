package com.introproventures.graphql.jpa.query.schema;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

class ThreadLocalDateFormat {

    private final ThreadLocal<DateFormat> df;

    public ThreadLocalDateFormat(String formatString) {
        this.df = ThreadLocal.withInitial(() -> new SimpleDateFormat(formatString));
    }

    public Date parse(String dateString) throws ParseException {
        return df.get().parse(dateString);
    }

    public String format(Object date) {
        return df.get().format(date);
    }
}
