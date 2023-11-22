/*
 * Copyright 2017 IntroPro Ventures Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.introproventures.graphql.jpa.query.schema.model.book;

import com.introproventures.graphql.jpa.query.annotation.GraphQLDescription;
import com.introproventures.graphql.jpa.query.annotation.GraphQLIgnore;
import com.introproventures.graphql.jpa.query.annotation.GraphQLIgnoreFilter;
import com.introproventures.graphql.jpa.query.annotation.GraphQLIgnoreOrder;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@EqualsAndHashCode
@ToString
public class Book {

    @Id
    Long id;

    String title;

    @GraphQLIgnoreOrder
    @GraphQLIgnoreFilter
    String description;

    @GraphQLDescription("The price of the book visible only by authorized users")
    Double price;

    @ElementCollection(fetch = FetchType.LAZY)
    @GraphQLDescription("A set of user-defined tags")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<String> tags = new LinkedHashSet<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Author author;

    @Enumerated(EnumType.STRING)
    Genre genre;

    Date publicationDate;

    @ElementCollection(fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Set<Publisher> publishers;

    @Transient
    @GraphQLIgnore
    public String getAuthorName() {
        return author.getName();
    }
}
