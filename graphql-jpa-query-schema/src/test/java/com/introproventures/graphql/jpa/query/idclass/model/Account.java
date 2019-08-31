package com.introproventures.graphql.jpa.query.idclass.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

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