package com.introproventures.graphql.jpa.query.mutations.model;

import com.introproventures.graphql.jpa.query.mutations.annotation.GraphQLWriteEntityForRole;
import com.introproventures.graphql.jpa.query.mutations.annotation.GraphQLWriteEntityList;
import com.introproventures.graphql.jpa.query.mutations.annotation.GraphQLWriteType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@GraphQLWriteEntityList(
        @GraphQLWriteEntityForRole(value = {"user"}, operations = {GraphQLWriteType.ALL})
)
public class Student {

    @Id
    private long id;

    @Column(name = "name", nullable = true, length = 2000)
    private String name;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
