package com.introproventures.graphql.jpa.query.schema.impl;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;

class ResultStreamWrapperTest {

    @Test
    void wrap() {
        List result = ResultStreamWrapper.wrap(Collections.emptyList(), 0);

        assertThat(result).hasSize(0);
        assertThat(result.equals(result)).isTrue();
        assertThat(result.hashCode()).isEqualTo(System.identityHashCode(result));
    }

    @Test
    void _hashCode() {
        List result = ResultStreamWrapper.wrap(Collections.emptyList(), 0);

        assertThat(result.hashCode()).isEqualTo(System.identityHashCode(result));
    }

    @Test
    void equals() {
        List result = ResultStreamWrapper.wrap(Collections.emptyList(), 0);

        assertThat(result.equals(result)).isTrue();
    }


    @Test
    void hasSize() {
        List result = ResultStreamWrapper.wrap(Collections.emptyList(), 0);

        assertThat(result).hasSize(0);
    }

    @Test
    void iterator() {
        List result = ResultStreamWrapper.wrap(Collections.emptyList(), 0);

        assertThat(result.iterator()).isNotNull();
    }

    @Test
    void spliterator() {
        List result = ResultStreamWrapper.wrap(Collections.emptyList(), 0);

        assertThat(result.spliterator()).isNotNull();
    }

    @Test
    void methodNotSupported() {
        List result = ResultStreamWrapper.wrap(Collections.emptyList(), 0);

        Exception error = catchException(() -> result.get(0));

        assertThat(error).isInstanceOf(UnsupportedOperationException.class);
    }
}
