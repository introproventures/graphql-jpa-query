package com.introproventures.graphql.jpa.query.schema.model.metamodel;

import com.introproventures.graphql.jpa.query.annotation.GraphQLIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class ClassWithCustomMetamodel {

    @Id
    private Long id;

    private String publicValue;

    private String protectedValue;

    private String ignoredProtectedValue;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPublicValue() {
        return publicValue;
    }

    public void setPublicValue(String publicValue) {
        this.publicValue = publicValue;
    }

    protected String getProtectedValue() {
        return protectedValue;
    }

    protected void setProtectedValue(String protectedValue) {
        this.protectedValue = protectedValue;
    }

    @GraphQLIgnore
    protected String getIgnoredProtectedValue() {
        return ignoredProtectedValue;
    }

    protected void setIgnoredProtectedValue(String ignoredProtectedValue) {
        this.ignoredProtectedValue = ignoredProtectedValue;
    }
}
