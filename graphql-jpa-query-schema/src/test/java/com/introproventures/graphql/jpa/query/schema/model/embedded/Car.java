
package com.introproventures.graphql.jpa.query.schema.model.embedded;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity(name = "Car")
@Data
@EqualsAndHashCode(callSuper=true)
public class Car extends Vehicle {

    String brand;
    
    // Shared between Car and Boat. Fixes https://github.com/introproventures/graphql-jpa-query/issues/91
    @Embedded
    Engine engine;    

}
