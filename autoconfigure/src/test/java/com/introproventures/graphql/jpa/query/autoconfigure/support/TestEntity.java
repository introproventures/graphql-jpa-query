package com.introproventures.graphql.jpa.query.autoconfigure.support;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.Set;
import lombok.Data;

@Entity
@Data
public class TestEntity {

    @Id
    private Long id;

    @OneToMany
    Set<TestChildEntity> children;
}
