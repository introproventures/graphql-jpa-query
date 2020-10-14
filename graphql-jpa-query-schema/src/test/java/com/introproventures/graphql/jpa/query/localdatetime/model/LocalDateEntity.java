package com.introproventures.graphql.jpa.query.localdatetime.model;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

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
