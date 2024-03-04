package com.introproventures.graphql.jpa.query.autoconfigure.support;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class TestChildEntity {

    @Id
    String id;
}
