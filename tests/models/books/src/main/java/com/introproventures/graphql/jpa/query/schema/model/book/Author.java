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

import jakarta.persistence.Basic;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import java.util.HashSet;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@EqualsAndHashCode // Fixes NPE in Hibernate when initializing loaded collections #1
public class Author {

    @Id
    Long id;

    String name;

    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Set<Book> books;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "author_phone_numbers", joinColumns = @JoinColumn(name = "author_id"))
    @Column(name = "phone_number")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<String> phoneNumbers = new HashSet<>();

    @Enumerated(EnumType.STRING)
    Genre genre;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private byte[] profilePicture;
}
