package com.introproventures.graphql.jpa.query.schema.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.when;

import java.util.NoSuchElementException;
import java.util.Optional;

import javax.persistence.metamodel.Attribute;

import org.junit.Test;
import org.mockito.Mockito;

import com.introproventures.graphql.jpa.query.schema.impl.IntrospectionUtils.EntityIntrospectionResult;
import com.introproventures.graphql.jpa.query.schema.impl.IntrospectionUtils.EntityIntrospectionResult.AttributePropertyDescriptor;
import com.introproventures.graphql.jpa.query.schema.impl.IntrospectionUtils.EntityIntrospectionResult.FieldDescriptor;
import com.introproventures.graphql.jpa.query.schema.model.calculated.CalculatedEntity;
import com.introproventures.graphql.jpa.query.schema.model.calculated.ParentCalculatedEntity;

public class IntrospectionUtilsTest {

	// given
    private final Class<CalculatedEntity> entity = CalculatedEntity.class;

    @Test(expected = NoSuchElementException.class)
    public void testIsTransientNonExisting() throws Exception {
        // then
        assertThat(IntrospectionUtils.isTransient(entity, "notFound")).isFalse();
    }

    @Test(expected = NoSuchElementException.class)
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
    public void testIsPersistentFunction() throws Exception {
        // then
        assertThat(IntrospectionUtils.isPersistent(entity, "fieldFun")).isFalse();
        assertThat(IntrospectionUtils.isPersistent(entity, "hideFieldFunction")).isTrue();
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

        assertThat(IntrospectionUtils.isIgnored(entity, "transientModifier")).isFalse();
        assertThat(IntrospectionUtils.isIgnored(entity, "parentTransientModifier")).isFalse();
        assertThat(IntrospectionUtils.isIgnored(entity, "parentTransient")).isFalse();
        assertThat(IntrospectionUtils.isIgnored(entity, "parentTransientGetter")).isFalse();
    }

    @Test
    public void shouldNotIgnoreMethodsThatAreNotAnnotatedWithGraphQLIgnore() {
        //then
        assertThat(IntrospectionUtils.isNotIgnored(entity, "propertyIgnoredOnGetter")).isFalse();
        assertThat(IntrospectionUtils.isNotIgnored(entity, "ignoredTransientValue")).isFalse();
        assertThat(IntrospectionUtils.isNotIgnored(entity, "hideField")).isFalse();
        assertThat(IntrospectionUtils.isNotIgnored(entity, "parentGraphQLIgnore")).isFalse();
        
        assertThat(IntrospectionUtils.isNotIgnored(entity, "transientModifier")).isTrue();
        assertThat(IntrospectionUtils.isNotIgnored(entity, "parentTransientModifier")).isTrue();
        assertThat(IntrospectionUtils.isNotIgnored(entity, "parentTransient")).isTrue();
        assertThat(IntrospectionUtils.isNotIgnored(entity, "parentTransientGetter")).isTrue();
    }
    
    @SuppressWarnings("rawtypes")
    @Test
    public void shouldGetClassesInHierarchy() {
        //when
        Class[] result = IntrospectionUtils.introspect(entity)
                                           .getClasses()
                                           .toArray(Class[]::new);
        
        //then
        assertThat(result).containsExactly(CalculatedEntity.class, 
                                           ParentCalculatedEntity.class,
                                           Object.class);
    }
    
    @Test
    public void testGetPropertyDescriptorsSchemaDescription() throws Exception {
        // when
        EntityIntrospectionResult result = IntrospectionUtils.introspect(CalculatedEntity.class);
        
        // then
        assertThat(result.getPropertyDescriptors()).extracting(AttributePropertyDescriptor::getSchemaDescription)
                                                   .filteredOn(Optional::isPresent)
                                                   .extracting(Optional::get)
                                                   .containsOnly("i desc function",
                                                                 "getParentTransientGetter",
                                                                 "UppercaseGetter");
        
        assertThat(result.getFieldDescriptors()).extracting(FieldDescriptor::getSchemaDescription)
                                                .filteredOn(Optional::isPresent)
                                                .extracting(Optional::get)
                                                .containsOnly("title",
                                                              "transientModifier",
                                                              "i desc member",
                                                              "parentTransientModifier",
                                                              "Uppercase");
        
    }

    @Test
    public void testGetPropertyDescriptorSchemaDescriptionByAttribute() throws Exception {
        Attribute<?, ?> attribute = Mockito.mock(Attribute.class);
        
        when(attribute.getName()).thenReturn("title");
        
        // when
        Optional<AttributePropertyDescriptor> result = IntrospectionUtils.introspect(CalculatedEntity.class)
                                                             .getPropertyDescriptor(attribute);
        // then
        assertThat(result.isPresent()).isTrue();
    }
    
    @Test
    public void testGetParentEntitySchemaDescription() throws Exception {
        // when
        EntityIntrospectionResult result = IntrospectionUtils.introspect(CalculatedEntity.class);
        
        // then
        assertThat(result.getSchemaDescription()).contains("ParentCalculatedEntity description");
        assertThat(result.hasSchemaDescription()).isTrue();
    }    

    @Test
    public void testUppercasePropertyNamesAreSupported() throws Exception {
        // when
        EntityIntrospectionResult result = IntrospectionUtils.introspect(CalculatedEntity.class);
        
        // then
        assertThat(result.getFieldDescriptor("Uppercase")).isPresent();
        assertThat(result.getPropertyDescriptor("Uppercase")).isPresent();

        assertThat(result.getPropertyDescriptor("Uppercase")
                         .get())
                         .extracting(AttributePropertyDescriptor::isIgnored)
                         .isEqualTo(false);

        assertThat(result.getPropertyDescriptor("Uppercase")
                         .get())
                         .extracting(AttributePropertyDescriptor::isTransient)
                         .isEqualTo(false);
        
        assertThat(result.getFieldDescriptor("Uppercase")
                         .get()
                         .getSchemaDescription())
                         .contains("Uppercase");

        assertThat(result.getPropertyDescriptor("UppercaseGetter")
                         .get())
                         .extracting(AttributePropertyDescriptor::isIgnored)
                         .isEqualTo(false);
        
        assertThat(result.getPropertyDescriptor("UppercaseGetter")
                         .get()
                         .getSchemaDescription())
                         .contains("UppercaseGetter");
        
        assertThat(result.getPropertyDescriptor("UppercaseGetter")
                         .get())
                         .extracting(AttributePropertyDescriptor::isTransient)
                         .isEqualTo(true);

        assertThat(result.getPropertyDescriptor("UppercaseGetterIgnore")
                         .get())
                         .extracting(AttributePropertyDescriptor::isIgnored)
                         .isEqualTo(true);
    }
    
    @Test
    public void testPrivateModifierOnGetterProperty() throws Exception {
        // when
        EntityIntrospectionResult result = IntrospectionUtils.introspect(CalculatedEntity.class);
        
        // then
        assertThat(IntrospectionUtils.isIgnored(entity, "age")).isFalse();
        assertThat(IntrospectionUtils.isPersistent(entity, "age")).isTrue();
        assertThat(IntrospectionUtils.isTransient(entity, "age")).isFalse();
        
        assertThat(result.getFieldDescriptor("age")).isPresent();
        assertThat(result.getPropertyDescriptor("age")).isPresent();
        assertThat(result.getPropertyDescriptor("age")
                         .get()
                         .getReadMethod())
                         .isEmpty();
    }    

    @Test
    public void shouldNotFailWhenPropertyIsDuplicatedInParentAndChild() {
        // given
        // There is a duplicated property in parent and child

        // then
        assertThatCode(() -> IntrospectionUtils.introspect(CalculatedEntity.class)).doesNotThrowAnyException();
    }

    @Test
    public void shouldCorrectlyIntrospectPropertyDuplicatedInParentAndChild() {
        // given
        // There is a duplicated property in parent and child

        // when
        EntityIntrospectionResult introspectionResult = IntrospectionUtils.introspect(CalculatedEntity.class);

        // then
        Optional<AttributePropertyDescriptor> propertyOverriddenInChild = introspectionResult.getPropertyDescriptor("propertyDuplicatedInChild");
        assertThat(propertyOverriddenInChild).isPresent();
    }
}
