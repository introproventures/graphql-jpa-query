package com.introproventures.graphql.jpa.query.schema.model.floating;

import java.math.BigDecimal;
import javax.persistence.Entity;
import javax.persistence.Id;
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
