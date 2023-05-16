package com.introproventures.graphql.jpa.query.schema.model.uuid;

import com.introproventures.graphql.jpa.query.annotation.GraphQLDescription;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.util.UUID;

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
