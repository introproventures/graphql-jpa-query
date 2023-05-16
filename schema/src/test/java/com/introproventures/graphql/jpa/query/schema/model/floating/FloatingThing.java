package com.introproventures.graphql.jpa.query.schema.model.floating;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.math.BigDecimal;
import lombok.Data;

@Data
@Entity
public class FloatingThing {

    @Id
    private Long id;

    float floatValue;

    double doubleValue;

    BigDecimal bigDecimalValue;
}
