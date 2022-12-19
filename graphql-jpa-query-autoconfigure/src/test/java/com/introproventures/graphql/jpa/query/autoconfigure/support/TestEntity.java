package com.introproventures.graphql.jpa.query.autoconfigure.support;

import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.Data;

@Entity
@Data
public class TestEntity {
    @Id
    private Long id;
}
