package com.introproventures.graphql.jpa.query.idclass.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.Data;

@Entity
@IdClass(AccountId.class)
@Data
public class Account {

    @Id
    private String accountNumber;

    @Id
    private String accountType;

    private String description;
}
