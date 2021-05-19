package com.introproventures.graphql.jpa.query.introspection;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;


public class ArrayUtilTest {

    @Test
    public void testIsEmpty() {
        assertThat(ArrayUtil.isEmpty(null)).isTrue();

        assertThat(ArrayUtil.isEmpty(new String[0])).isTrue();;
        assertThat(ArrayUtil.isEmpty(new String[10])).isFalse();

        assertThat(ArrayUtil.isEmpty(new int[0])).isTrue();;
        assertThat(ArrayUtil.isEmpty(new int[10])).isFalse();

        assertThat(ArrayUtil.isEmpty(new Object())).isFalse(); // not an array
    }

    @Test
    public void testIsNotEmpty() {
        assertThat(ArrayUtil.isNotEmpty(null)).isFalse();

        assertThat(ArrayUtil.isNotEmpty(new String[0])).isFalse();;
        assertThat(ArrayUtil.isNotEmpty(new String[10])).isTrue();

        assertThat(ArrayUtil.isNotEmpty(new int[0])).isFalse();;
        assertThat(ArrayUtil.isNotEmpty(new int[10])).isTrue();

        assertThat(ArrayUtil.isNotEmpty(new Object())).isTrue(); // not an array
    }

    @Test
    public void testIndexOfObject() {
        assertThat(ArrayUtil.indexOf(null, "a")).isEqualTo(-1);
        assertThat(ArrayUtil.indexOf(new String[] { "a", null, "c" }, (String) null)).isEqualTo(1);
        assertThat(ArrayUtil.indexOf(new String[] { "a", "b", "c" }, (String) null)).isEqualTo(-1);
        assertThat(ArrayUtil.indexOf(new String[0], "a")).isEqualTo(-1);
        assertThat(ArrayUtil.indexOf(new String[] { "a", "a", "b", "a", "a", "b", "a", "a" }, "a")).isEqualTo(0);
        assertThat(ArrayUtil.indexOf(new String[] { "a", "a", "b", "a", "a", "b", "a", "a" }, "b")).isEqualTo(2);

        assertThat(ArrayUtil.indexOf(null, "a", 0)).isEqualTo(-1);
        assertThat(ArrayUtil.indexOf(new String[] { "a", null, "c" }, (String) null, 0)).isEqualTo(1);
        assertThat(ArrayUtil.indexOf(new String[0], "a", 0)).isEqualTo(-1);
        assertThat(ArrayUtil.indexOf(new String[] { "a", "a", "b", "a", "a", "b", "a", "a" }, "b", 0)).isEqualTo(2);
        assertThat(ArrayUtil.indexOf(new String[] { "a", "a", "b", "a", "a", "b", "a", "a" }, "b", 3)).isEqualTo(5);
        assertThat(ArrayUtil.indexOf(new String[] { "a", "a", "b", "a", "a", "b", "a", "a" }, "b", 9)).isEqualTo(-1);
        assertThat(ArrayUtil.indexOf(new String[] { "a", "a", "b", "a", "a", "b", "a", "a" }, "b", -1)).isEqualTo(2);    
    }

    @Test
    public void testAddAll() {
        assertThat((boolean[][]) ArrayUtil.addAll(null, null)).isNull();
        assertThat(ArrayUtil.addAll(null, new String[] {})).isEqualTo(new String[] {});
        assertThat(ArrayUtil.addAll(new String[] {}, null)).isEqualTo(new String[] {});
        assertThat(ArrayUtil.addAll(new String[] {}, new String[] {})).isEqualTo(new String[] {});
        assertThat(ArrayUtil.addAll(null, new String[] {"a"})).isEqualTo(new String[] {"a"});
        assertThat(ArrayUtil.addAll(new String[] {"a"}, null)).isEqualTo(new String[] {"a"});
        assertThat(ArrayUtil.addAll(new String[] {"a"}, new String[] {"b"})).isEqualTo(new String[] {"a", "b"});
    }


}
