package com.introproventures.graphql.jpa.query.schema.model.embedded;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import lombok.Data;

@MappedSuperclass
@Data
public abstract class Vehicle {

    @Id
    String id;

}
