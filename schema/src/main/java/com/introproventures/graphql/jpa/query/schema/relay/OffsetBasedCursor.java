package com.introproventures.graphql.jpa.query.schema.relay;

import graphql.relay.ConnectionCursor;
import graphql.relay.DefaultConnectionCursor;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This uses an encoding of offset from the page forward
 */
class OffsetBasedCursor {

    private static final java.util.Base64.Encoder encoder = java.util.Base64.getEncoder();
    private static final java.util.Base64.Decoder decoder = java.util.Base64.getDecoder();
    private static final Pattern offsetPattern = Pattern.compile("^offset=([0-9]*)");

    long offset;

    public OffsetBasedCursor() {
        this(0L);
    }

    OffsetBasedCursor(long offset) {
        this.offset = offset;
    }

    long getOffset() {
        return offset;
    }

    public static OffsetBasedCursor fromCursor(String cursor) {
        String s = decode(cursor);

        Matcher matcher = offsetPattern.matcher(s);
        if (!matcher.find()) {
            throwInvalidCursor(s);
        }
        String offset = matcher.group(1);
        return new OffsetBasedCursor(Long.parseLong(offset));
    }

    ConnectionCursor toConnectionCursor() {
        return new DefaultConnectionCursor(encode("offset=" + offset));
    }

    private String encode(String s) {
        return encoder.encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String s) {
        return new String(decoder.decode(s), StandardCharsets.UTF_8);
    }

    private static void throwInvalidCursor(String cursor) {
        throw new IllegalArgumentException("Invalid paged cursor provided : " + cursor);
    }
}
