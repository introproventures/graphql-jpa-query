package com.introproventures.graphql.jpa.query.mutations.model.book;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Getter
@Setter
@ToString
public class Alias {
    @Id
    Long id;

    @Column(name = "name", nullable = true, length = 256)
    String name;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "a_id")
    Author author;


}
