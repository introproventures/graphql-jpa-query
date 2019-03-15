package com.introproventures.graphql.jpa.query.schema.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.introproventures.graphql.jpa.query.schema.model.calculated.CalculatedEntity;

public class IntrospectionUtilsTest {

	// given
    Class<CalculatedEntity> entity = CalculatedEntity.class;

    
    @Test(expected=RuntimeException.class)
    public void testIsTransientNonExisting() throws Exception {
        // then
        assertThat(IntrospectionUtils.isTransient(entity, "notFound")).isFalse();
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
    }

    @Test
    public void testNotTransientFields() throws Exception {
    	// given
        Class<CalculatedEntity> entity = CalculatedEntity.class;
        
        // then
        assertThat(IntrospectionUtils.isTransient(entity, "id")).isFalse();
        assertThat(IntrospectionUtils.isTransient(entity, "info")).isFalse();
        assertThat(IntrospectionUtils.isTransient(entity, "title")).isFalse();
    }
    
    
}
