package com.introproventures.graphql.jpa.query.localdatetime.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import lombok.Getter;

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

    @Column(name = "TIMESTAMP")
    Timestamp timestamp;

    @Column(name = "description")
    String description;
}
