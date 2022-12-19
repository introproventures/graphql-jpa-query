package com.introproventures.graphql.jpa.query.schema;

import java.util.NoSuchElementException;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import com.introproventures.graphql.jpa.query.AbstractSpringBootTestSupport;
import com.introproventures.graphql.jpa.query.schema.impl.EntityIntrospector;
import com.introproventures.graphql.jpa.query.schema.impl.EntityIntrospector.EntityIntrospectionResult;
import com.introproventures.graphql.jpa.query.schema.impl.EntityIntrospector.EntityIntrospectionResult.AttributePropertyDescriptor;
import com.introproventures.graphql.jpa.query.schema.model.calculated.CalculatedEntity;
import com.introproventures.graphql.jpa.query.schema.model.calculated.ParentCalculatedEntity;
import com.introproventures.graphql.jpa.query.schema.model.metamodel.ClassWithCustomMetamodel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = EntityIntrospectorTest.Application.class)
public class EntityIntrospectorTest extends AbstractSpringBootTestSupport {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class Application {
    }

    @Autowired
    private EntityManager entityManager;
    
	// given
    private final Class<CalculatedEntity> entityClass = CalculatedEntity.class;
    
    private EntityIntrospectionResult subject;
    
    @Before
    public void setUp() {
        ManagedType<CalculatedEntity> entityType = entityManager.getMetamodel()
                                                                .managedType(entityClass);
        
        this.subject = EntityIntrospector.introspect(entityType);
    }

    
    @Test(expected = NoSuchElementException.class)
    public void testResultOfNoSuchElementException() throws Exception {
        // then
        EntityIntrospector.resultOf(Object.class);
    }
    
    @Test(expected = NoSuchElementException.class)
    public void testIsTransientNonExisting() throws Exception {
        // then
        assertThat(subject.isTransient("notFound")).isFalse();
    }

    @Test(expected = NoSuchElementException.class)
    public void testIsIgnoredNonExisting() throws Exception {
        // then
        assertThat(subject.isIgnored("notFound")).isFalse();
    }
    
    @Test
    public void shouldExcludeClassPropertyDescriptor() throws Exception {
        // then
        assertThat(subject.getPropertyDescriptor("class")).isEmpty();
    }

    @Test
    public void testIsTransientFunction() throws Exception {
        // then
        assertThat(subject.isTransient("fieldFun")).isTrue();
        assertThat(subject.isTransient("hideFieldFunction")).isTrue();
    } 

    @Test
    public void testIsPersistentFunction() throws Exception {
        // then
        assertThat(subject.isPersistent("fieldFun")).isFalse();
        assertThat(subject.isPersistent("hideFieldFunction")).isFalse();
    }
    
    @Test
    public void testIsTransientFields() throws Exception {
        // then
        assertThat(subject.isTransient("fieldFun")).isTrue();
        assertThat(subject.isTransient("fieldMem")).isTrue();
        assertThat(subject.isTransient("hideField")).isTrue();
        assertThat(subject.isTransient("logic")).isTrue();
        assertThat(subject.isTransient("transientModifier")).isTrue();
        assertThat(subject.isTransient("parentTransientModifier")).isTrue();
        assertThat(subject.isTransient("parentTransient")).isTrue();
        assertThat(subject.isTransient("parentTransientGetter")).isTrue();
    }

    @Test
    public void testNotTransientFields() throws Exception {
    	// then
        assertThat(subject.isTransient("id")).isFalse();
        assertThat(subject.isTransient("info")).isFalse();
        assertThat(subject.isTransient("title")).isFalse();
        assertThat(subject.isTransient("parentField")).isFalse();
    }

    @Test
    public void testByPassSetMethod() throws Exception {
        // then
        assertThat(subject.isTransient("something")).isTrue();
    }

    @Test
    public void shouldIgnoreMethodsThatAreAnnotatedWithGraphQLIgnore() {
        //then
        assertThat(subject.isIgnored("propertyIgnoredOnGetter")).isTrue();
        assertThat(subject.isIgnored("ignoredTransientValue")).isTrue();
        assertThat(subject.isIgnored("hideField")).isTrue();
        assertThat(subject.isIgnored("parentGraphQLIgnore")).isTrue();

        assertThat(subject.isIgnored("transientModifier")).isFalse();
        assertThat(subject.isIgnored("parentTransientModifier")).isFalse();
        assertThat(subject.isIgnored("parentTransient")).isFalse();
        assertThat(subject.isIgnored("parentTransientGetter")).isFalse();
    }

    @Test
    public void shouldNotIgnoreMethodsThatAreNotAnnotatedWithGraphQLIgnore() {
        //then
        assertThat(subject.isNotIgnored("propertyIgnoredOnGetter")).isFalse();
        assertThat(subject.isNotIgnored("ignoredTransientValue")).isFalse();
        assertThat(subject.isNotIgnored("hideField")).isFalse();
        assertThat(subject.isNotIgnored("parentGraphQLIgnore")).isFalse();
        
        assertThat(subject.isNotIgnored("transientModifier")).isTrue();
        assertThat(subject.isNotIgnored("parentTransientModifier")).isTrue();
        assertThat(subject.isNotIgnored("parentTransient")).isTrue();
        assertThat(subject.isNotIgnored("parentTransientGetter")).isTrue();
    }
    
    @SuppressWarnings("rawtypes")
    @Test
    public void shouldGetClassesInHierarchy() {
        //when
        Class[] result = EntityIntrospector.resultOf(entityClass)
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
        EntityIntrospectionResult result = EntityIntrospector.resultOf(CalculatedEntity.class);
        
        // then
        assertThat(result.getPropertyDescriptors()).extracting(AttributePropertyDescriptor::getSchemaDescription)
                                                   .filteredOn(Optional::isPresent)
                                                   .extracting(Optional::get)
                                                   .containsOnly("i desc function",
                                                                 "getParentTransientGetter",
                                                                 "UppercaseGetter",
                                                                 "title",
                                                                 "transientModifier",
                                                                 "i desc member",
                                                                 "parentTransientModifier",
                                                                 "Uppercase",
                                                                 "protectedGetter");
    }

    @Test
    public void testGetPropertyDescriptorSchemaDescriptionByAttribute() throws Exception {
        Attribute<?, ?> attribute = Mockito.mock(Attribute.class);
        
        when(attribute.getName()).thenReturn("title");
        
        // when
        Optional<AttributePropertyDescriptor> result = EntityIntrospector.resultOf(CalculatedEntity.class)
                                                             .getPropertyDescriptor(attribute);
        // then
        assertThat(result.isPresent()).isTrue();
    }
    
    @Test
    public void testGetParentEntitySchemaDescription() throws Exception {
        // when
        EntityIntrospectionResult result = EntityIntrospector.resultOf(CalculatedEntity.class);
        
        // then
        assertThat(result.getSchemaDescription()).contains("ParentCalculatedEntity description");
        assertThat(result.hasSchemaDescription()).isTrue();
    }    

    @Test
    public void testUppercasePropertyNamesAreSupported() throws Exception {
        // when
        EntityIntrospectionResult result = EntityIntrospector.resultOf(CalculatedEntity.class);
        
        // then
        assertThat(result.getPropertyDescriptor("Uppercase")).isPresent();

        assertThat(result.getPropertyDescriptor("Uppercase")
                         .get())
                         .extracting(AttributePropertyDescriptor::isIgnored)
                         .isEqualTo(false);

        assertThat(result.getPropertyDescriptor("Uppercase")
                         .get())
                         .extracting(AttributePropertyDescriptor::isTransient)
                         .isEqualTo(false);
        
        assertThat(result.getPropertyDescriptor("Uppercase")
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
                         .isEqualTo(false);

        assertThat(result.getPropertyDescriptor("uppercaseGetterIgnore")
                         .get())
                         .extracting(AttributePropertyDescriptor::isIgnored)
                         .isEqualTo(true);
    }
    
    @Test
    public void testPrivateModifierOnGetterProperty() throws Exception {
        // when
        EntityIntrospectionResult result = EntityIntrospector.resultOf(CalculatedEntity.class);
        
        // then
        assertThat(subject.isIgnored("age")).isFalse();
        assertThat(subject.isPersistent("age")).isTrue();
        assertThat(subject.isTransient("age")).isFalse();
        
        assertThat(result.getPropertyDescriptor("age")).isPresent();
        assertThat(result.getPropertyDescriptor("age")
                         .get()
                         .getReadMethod())
                         .isEmpty();
    }
    
    @Test
    public void testProtectedModifierOnGetterProperty() throws Exception {
        // when
        EntityIntrospectionResult result = EntityIntrospector.resultOf(CalculatedEntity.class);
        
        // then
        assertThat(subject.isIgnored("protectedGetter")).isFalse();
        assertThat(subject.isPersistent("protectedGetter")).isTrue();
        assertThat(subject.isTransient("protectedGetter")).isFalse();
        
        assertThat(result.getPropertyDescriptor("protectedGetter")).isPresent();
        assertThat(result.getPropertyDescriptor("protectedGetter")
                         .get()
                         .getReadMethod())
                         .isPresent();
    }      

    @Test
    public void shouldNotFailWhenPropertyIsDuplicatedInParentAndChild() {
        // given
        // There is a duplicated property in parent and child

        // then
        assertThatCode(() -> EntityIntrospector.resultOf(CalculatedEntity.class)).doesNotThrowAnyException();
    }

    @Test
    public void shouldCorrectlyIntrospectPropertyDuplicatedInParentAndChild() {
        // given
        // There is a duplicated property in parent and child

        // when
        EntityIntrospectionResult introspectionResult = EntityIntrospector.resultOf(CalculatedEntity.class);

        // then
        Optional<AttributePropertyDescriptor> propertyOverriddenInChild = introspectionResult.getPropertyDescriptor("propertyDuplicatedInChild");
        assertThat(propertyOverriddenInChild).isPresent();
    }
    
    @Test
    public void testGetTransientPropertyDescriptors() {
        // given
        ManagedType<?> managedType = entityManager.getMetamodel().entity(CalculatedEntity.class);
        
        // when
        EntityIntrospectionResult result = EntityIntrospector.introspect(managedType);
        
        // then
        assertThat(result.getTransientPropertyDescriptors()).extracting(AttributePropertyDescriptor::getName)
                                                            .containsOnly("fieldFun", 
                                                                          "fieldMem", 
                                                                          "hideField", 
                                                                          "logic", 
                                                                          "transientModifier", 
                                                                          "parentTransientModifier", 
                                                                          "parentTransient", 
                                                                          "parentTransientGetter",
                                                                          "uppercaseGetterIgnore",
                                                                          "hideFieldFunction",
                                                                          "transientModifierGraphQLIgnore",
                                                                          "customLogic",
                                                                          "parentTransientModifierGraphQLIgnore",
                                                                          "ignoredTransientValue",
                                                                          "something",
                                                                          "parentTransientGraphQLIgnore");
        ;
    }    
    
    @Test
    public void testGetPersistentPropertyDescriptors() {
        // given
        ManagedType<?> managedType = entityManager.getMetamodel().entity(CalculatedEntity.class);
        
        // when
        EntityIntrospectionResult result = EntityIntrospector.introspect(managedType);
        
        // then
        assertThat(result.getPersistentPropertyDescriptors()).extracting(AttributePropertyDescriptor::getName)
                                                             .containsOnly("Uppercase",
                                                                           "title",
                                                                           "parentGraphQLIgnore",
                                                                           "parentGraphQLIgnoreGetter",
                                                                           "propertyIgnoredOnGetter",
                                                                           "id",
                                                                           "info",
                                                                           "parentTransientGraphQLIgnoreGetter",
                                                                           "protectedGetter",
                                                                           "parentField",
                                                                           "UppercaseGetter",
                                                                           "propertyDuplicatedInChild",
                                                                           "age");
    }    

    @Test
    public void testGetIgnoredPropertyDescriptors() {
        // given
        ManagedType<?> managedType = entityManager.getMetamodel().entity(CalculatedEntity.class);
        
        // when
        EntityIntrospectionResult result = EntityIntrospector.introspect(managedType);
        
        // then
        assertThat(result.getIgnoredPropertyDescriptors()).extracting(AttributePropertyDescriptor::getName)
                                                          .containsOnly("uppercaseGetterIgnore",
                                                                        "hideFieldFunction",
                                                                        "parentGraphQLIgnore",
                                                                        "parentGraphQLIgnoreGetter",
                                                                        "transientModifierGraphQLIgnore",
                                                                        "propertyIgnoredOnGetter",
                                                                        "parentTransientModifierGraphQLIgnore",
                                                                        "ignoredTransientValue",
                                                                        "hideField",
                                                                        "parentTransientGraphQLIgnoreGetter",
                                                                        "parentTransientGraphQLIgnore");
    }

    @Test
    public void shouldIntrospectEntityWithCustomMetamodel() {
        //given
        EntityType<ClassWithCustomMetamodel> entity = entityManager.getMetamodel()
                                                                   .entity(ClassWithCustomMetamodel.class);

        //when
        EntityIntrospectionResult result = EntityIntrospector.introspect(entity);

        //then
        assertThat(result.getPropertyDescriptors())
                .extracting(AttributePropertyDescriptor::getName)
                .containsOnly("id",
                              "publicValue",
                              "protectedValue",
                              "ignoredProtectedValue");
    }
}
