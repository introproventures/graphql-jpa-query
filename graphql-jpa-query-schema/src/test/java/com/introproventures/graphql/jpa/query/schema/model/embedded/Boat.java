
package com.introproventures.graphql.jpa.query.schema.model.embedded;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;

@Entity(name = "Boat")
@Data
public class Boat  {

    @Id
    String id;

    @Embedded
    Engine engine;
}
