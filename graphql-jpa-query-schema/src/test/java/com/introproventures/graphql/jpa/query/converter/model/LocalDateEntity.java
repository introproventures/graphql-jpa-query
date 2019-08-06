package com.introproventures.graphql.jpa.query.converter.model;

import lombok.Getter;

import javax.persistence.*;
import java.time.*;

@Table(name = "LOCAL_DATE")
@Entity(name = "localDate")
@Getter
public class LocalDateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "LOCALDATE")
    LocalDate localDate;

    @Column(name = "LOCALDATETIME")
    LocalDateTime localDateTime;

    @Column(name = "OFFSETDATETIME")
    OffsetDateTime offsetDateTime;

    @Column(name = "ZONEDDATETIME")
    ZonedDateTime zonedDateTime;

    @Column(name = "INSTANT")
    Instant instant;

    @Column(name = "description")
    String description;
}
