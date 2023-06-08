package com.introproventures.graphql.jpa.query.schema.model.book;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Embeddable
public class Zipcode implements Serializable {

    String mainCode;

    String codeSuffix;
}
