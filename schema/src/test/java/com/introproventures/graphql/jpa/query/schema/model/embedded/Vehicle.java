package com.introproventures.graphql.jpa.query.schema.model.embedded;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;

@MappedSuperclass
@Data
public abstract class Vehicle {

    @Id
    String id;

}
