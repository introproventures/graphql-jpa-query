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

package com.introproventures.graphql.jpa.query.schema.model.starwars;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.introproventures.graphql.jpa.query.annotation.GraphQLDescription;

import lombok.Data;

@Entity
@GraphQLDescription("Database driven enumeration")
@Data
public class CodeList {

    @GraphQLDescription("Primary Key for the Code List Class")
    Long id;

    String type;
    String code;
    Integer sequence;
    boolean active;
    String description;

    CodeList parent;

    //JPA annotations moved to getters to test that @GraphQLDescription can be placed on the field when the JPA annotation is on the getter
    @Id
    public Long getId() {
        return(id);
    }
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @GraphQLDescription("The CodeList's parent CodeList")
    public CodeList getParent() {
        return(parent);
    }
}
