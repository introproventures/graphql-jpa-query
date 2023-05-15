package com.introproventures.graphql.jpa.query.idclass.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class AccountId implements Serializable {

    private static final long serialVersionUID = 1L;

    private String accountNumber;
    private String accountType;

}