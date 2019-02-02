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

import com.introproventures.graphql.jpa.query.annotation.GraphQLDescription;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@GraphQLDescription("Represents an electromechanical robot in the Star Wars Universe")
@Data
@EqualsAndHashCode(callSuper=true)
public class Droid extends Character {

    String primaryFunction;

    //description moved to getter to test it gets picked up
    @GraphQLDescription("Documents the primary purpose this droid serves")
    public String getPrimaryFunction() {
        return(primaryFunction);
    }
}
