package com.introproventures.graphql.jpa.query.schema.model.embedded;

import com.introproventures.graphql.jpa.query.annotation.GraphQLDescription;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import java.io.Serializable;
import lombok.Data;

@Entity(name = "Boat")
@Data
public class Boat {

    @Embeddable
    @Data
    public static class BoatId implements Serializable {

        private static final long serialVersionUID = 1L;

        String id;
        String country;
    }

    @EmbeddedId
    @GraphQLDescription("Primary Key for the Boat Class")
    BoatId boatId;

    @Embedded
    Engine engine;
}
