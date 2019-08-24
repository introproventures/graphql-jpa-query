package com.introproventures.graphql.jpa.query.schema.model.calculated;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import com.introproventures.graphql.jpa.query.annotation.GraphQLIgnore;

import lombok.Data;

@MappedSuperclass
@Data
public class ParentCalculatedEntity {

    private Integer parentField; // persistent property
    
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
    
    private String parentTransientGetter; 

    private String parentGraphQLIgnoreGetter; 

    private String parentTransientGraphQLIgnoreGetter; 
    
    @Transient // transient getter property
    public String getParentTransientGetter() {
        return parentTransientGetter;
    }
    
    @GraphQLIgnore
    @Transient // transient getter property
    public String getParentTransientGraphQLIgnoreGetter() {
        return parentTransientGraphQLIgnoreGetter;
    }
    
    @GraphQLIgnore 
    public String getParentGraphQLIgnoreGetter() {
        return parentGraphQLIgnoreGetter;
    }
    
}
