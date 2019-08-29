package com.introproventures.graphql.jpa.query.schema.model.calculated;

import java.time.LocalDate;
import java.time.Period;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import com.introproventures.graphql.jpa.query.annotation.GraphQLDescription;
import com.introproventures.graphql.jpa.query.annotation.GraphQLIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 
    2.1.1 Persistent Fields and Properties
    
    The persistent state of an entity is accessed by the persistence provider 
    runtime either via JavaBeans style property accessors or via instance variables. 
    A single access type (field or property access) applies to an entity hierarchy. 
    
    When annotations are used, the placement of the mapping annotations on either 
    the persistent fields or persistent properties of the entity class specifies the 
    access type as being either field - or property - based access respectively.
    
    If the entity has field-based access, the persistence provider runtime accesses 
    instance variables directly. All non-transient instance variables that are not 
    annotated with the Transient annotation are persistent. When field-based access 
    is used, the object/relational mapping annotations for the entity class annotate 
    the instance variables.
    
    If the entity has property-based access, the persistence provider runtime accesses 
    persistent state via the property accessor methods. All properties not annotated with 
    the  Transient annotation are persistent. The property accessor methods must be public 
    or protected. When property-based access is used, the object/relational mapping 
    annotations for the entity class annotate the getter property accessors.
    
    Mapping annotations cannot be applied to fields or properties that are transient or Transient.
    
    The behavior is unspecified if mapping annotations are applied to both persistent fields and 
    properties or if the XML descriptor specifies use of different access types within a class hierarchy.
 */

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
public class CalculatedEntity extends ParentCalculatedEntity {
    @Id
    Long id;

    @GraphQLDescription("title")
    String title;

    String info;
    
    @GraphQLDescription("Uppercase")
    String Uppercase;
    
    private Integer age;
    
    private Integer getAge(){
        return Period.between(LocalDate.now(), 
                              LocalDate.of(2000, 1, 1))
                     .getYears();
    }
    
    String UppercaseGetter;
    
    @GraphQLDescription("transientModifier")
    transient Integer transientModifier; // transient property

    @GraphQLIgnore
    transient Integer transientModifierGraphQLIgnore; // transient property

    @Transient
    boolean logic = true; // transient property
    
    @Transient
    @GraphQLDescription("i desc member")
    String fieldMem = "member";

    @Transient
    @GraphQLIgnore
    String hideField = "hideField";

    String propertyIgnoredOnGetter;
   
    @Transient
    @GraphQLDescription("i desc function")
    public String getFieldFun() {
        return title + " function";
    }

    @Transient
    public boolean isCustomLogic() {
        return false;
    }

    public String getHideFieldFunction() {
        return "getHideFieldFunction";
    }

    public void setSomething(int a){}

    @GraphQLIgnore
    public String getPropertyIgnoredOnGetter() {
        return propertyIgnoredOnGetter;
    }

    @Transient
    @GraphQLIgnore
    public String getIgnoredTransientValue(){
        return "IgnoredTransientValue";
    }
    
    @Transient
    @GraphQLDescription("UppercaseGetter")
    public String getUppercaseGetter() {
        return Uppercase;
    }

    @GraphQLIgnore
    public String getUppercaseGetterIgnore() {
        return Uppercase;
    }
    
}
