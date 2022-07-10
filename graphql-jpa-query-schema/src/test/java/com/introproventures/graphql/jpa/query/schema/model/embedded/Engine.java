package com.introproventures.graphql.jpa.query.schema.model.embedded;

import javax.persistence.Embeddable;

@Embeddable
public class Engine {

    String identification;
    Long hp;

}
