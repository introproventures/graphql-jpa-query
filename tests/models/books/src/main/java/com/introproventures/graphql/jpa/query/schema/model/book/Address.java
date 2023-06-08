package com.introproventures.graphql.jpa.query.schema.model.book;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;

@Getter
@Setter
@ToString
@FieldNameConstants
@Embeddable
public class Address implements Serializable {

    String street;

    String state;

    String country;

    String city;

    @Embedded
    Zipcode zipcode;
}
