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

package com.introproventures.graphql.jpa.query.schema.impl;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

enum Logical {
    AND,
    OR,
    NOT,
    EXISTS,
    NOT_EXISTS;

    private static Set<String> names = EnumSet
        .allOf(Logical.class)
        .stream()
        .map(it -> it.name().toString())
        .collect(Collectors.toSet());

    public static Set<String> names() {
        return names;
    }
}
