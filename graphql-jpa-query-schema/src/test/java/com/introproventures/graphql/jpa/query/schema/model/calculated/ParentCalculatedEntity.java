package com.introproventures.graphql.jpa.query.schema.model.calculated;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import com.introproventures.graphql.jpa.query.annotation.GraphQLDescription;
import com.introproventures.graphql.jpa.query.annotation.GraphQLIgnore;

import lombok.Data;

@GraphQLDescription("ParentCalculatedEntity description")
@MappedSuperclass
@Data
public class ParentCalculatedEntity {

    private Integer parentField; // persistent property
    
    @GraphQLDescription("parentTransientModifier")
    private transient String parentTransientModifier; // transient property

    @GraphQLIgnore
    private transient String parentTransientModifierGraphQLIgnore; // transient property
    
    @Transient
    private String parentTransient; // transient property

    @GraphQLIgnore
    @Transient
    private String parentTransientGraphQLIgnore; // transient property
    
    @GraphQLIgnore
    private String parentGraphQLIgnore; 
    
    @Transient // transient getter property
    private String parentTransientGetter; 

    private String parentGraphQLIgnoreGetter; 

    private String parentTransientGraphQLIgnoreGetter;

    private String propertyDuplicatedInChild;

    @GraphQLDescription("getParentTransientGetter")
    public String getParentTransientGetter() {
        return parentTransientGetter;
    }
    
    @GraphQLIgnore
    public String getParentTransientGraphQLIgnoreGetter() {
        return parentTransientGraphQLIgnoreGetter;
    }
    
    @GraphQLIgnore 
    public String getParentGraphQLIgnoreGetter() {
        return parentGraphQLIgnoreGetter;
    }
    
}
