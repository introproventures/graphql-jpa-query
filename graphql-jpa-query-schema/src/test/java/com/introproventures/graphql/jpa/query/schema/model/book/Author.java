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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@EqualsAndHashCode(exclude={"books","phoneNumbers"}) // Fixes NPE in Hibernate when initializing loaded collections #1
public class Author {
	@Id
	Long id;

	String name;

	@OneToMany(mappedBy="author", fetch=FetchType.LAZY)
	Collection<Book> books;
	
	@ElementCollection(fetch=FetchType.LAZY) 
	@CollectionTable(name = "author_phone_numbers", joinColumns = @JoinColumn(name = "author_id")) 
	@Column(name = "phone_number")
	private Set<String> phoneNumbers = new HashSet<>();	
	
	@Enumerated(EnumType.STRING)
    Genre genre;	
}
