package com.introproventures.graphql.jpa.query.schema.model.uuid;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import com.introproventures.graphql.jpa.query.annotation.GraphQLDescription;

@Entity
@GraphQLDescription("Database Thing with UUID field")

public class Thing {

    @Id
    @GraphQLDescription("Primary Key for the Thing Class")
    @Column(columnDefinition = "uuid")
    UUID id;

    String type;

    /**
     * @return the type
     */
    public String getType() {
        return this.type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the id
     */
    public UUID getId() {
        return this.id;
    }
}
