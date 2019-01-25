
package com.introproventures.graphql.jpa.query.schema.model.embedded;

import com.introproventures.graphql.jpa.query.annotation.GraphQLDescription;
import lombok.Data;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import java.io.Serializable;

@Entity(name = "Boat")
@Data
public class Boat  {

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
