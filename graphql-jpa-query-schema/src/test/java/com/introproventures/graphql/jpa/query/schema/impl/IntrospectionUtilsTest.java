package com.introproventures.graphql.jpa.query.schema.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.introproventures.graphql.jpa.query.schema.model.calculated.CalculatedEntity;

public class IntrospectionUtilsTest {

	// given
    private final Class<CalculatedEntity> entity = CalculatedEntity.class;

    @Test(expected = RuntimeException.class)
    public void testIsTransientNonExisting() throws Exception {
        // then
        assertThat(IntrospectionUtils.isTransient(entity, "notFound")).isFalse();
    }

    @Test(expected = RuntimeException.class)
    public void testIsIgnoredNonExisting() throws Exception {
        // then
        assertThat(IntrospectionUtils.isIgnored(entity, "notFound")).isFalse();
    }
    
    @Test
    public void testIsTransientClass() throws Exception {
        // then
        assertThat(IntrospectionUtils.isTransient(entity, "class")).isFalse();
    }

    @Test
    public void testIsTransientFunction() throws Exception {
        // then
        assertThat(IntrospectionUtils.isTransient(entity, "fieldFun")).isTrue();
        assertThat(IntrospectionUtils.isTransient(entity, "hideFieldFunction")).isFalse();
    }

    @Test
    public void testIsTransientFields() throws Exception {
        // then
        assertThat(IntrospectionUtils.isTransient(entity, "fieldFun")).isTrue();
        assertThat(IntrospectionUtils.isTransient(entity, "fieldMem")).isTrue();
        assertThat(IntrospectionUtils.isTransient(entity, "hideField")).isTrue();
        assertThat(IntrospectionUtils.isTransient(entity, "logic")).isTrue();
        assertThat(IntrospectionUtils.isTransient(entity, "transientModifier")).isTrue();
        assertThat(IntrospectionUtils.isTransient(entity, "parentTransientModifier")).isTrue();
        assertThat(IntrospectionUtils.isTransient(entity, "parentTransient")).isTrue();
        assertThat(IntrospectionUtils.isTransient(entity, "parentTransientGetter")).isTrue();
    }

    @Test
    public void testNotTransientFields() throws Exception {
    	// then
        assertThat(IntrospectionUtils.isTransient(entity, "id")).isFalse();
        assertThat(IntrospectionUtils.isTransient(entity, "info")).isFalse();
        assertThat(IntrospectionUtils.isTransient(entity, "title")).isFalse();
        assertThat(IntrospectionUtils.isTransient(entity, "parentField")).isFalse();
    }

    @Test
    public void testByPassSetMethod() throws Exception {
        // then
        assertThat(IntrospectionUtils.isTransient(entity,"something")).isFalse();
    }

    @Test
    public void shouldIgnoreMethodsThatAreAnnotatedWithGraphQLIgnore() {
        //then
        assertThat(IntrospectionUtils.isIgnored(entity, "propertyIgnoredOnGetter")).isTrue();
        assertThat(IntrospectionUtils.isIgnored(entity, "ignoredTransientValue")).isTrue();
        assertThat(IntrospectionUtils.isIgnored(entity, "hideField")).isTrue();
        assertThat(IntrospectionUtils.isIgnored(entity, "parentGraphQLIgnore")).isTrue();
    }
}
