package com.introproventures.graphql.jpa.query.schema.model.embedded;

import jakarta.persistence.Embeddable;

@Embeddable
public class Engine {

    String identification;
    Long hp;
}
