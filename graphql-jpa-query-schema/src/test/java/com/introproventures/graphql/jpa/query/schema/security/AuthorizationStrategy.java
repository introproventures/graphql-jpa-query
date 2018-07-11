/*
 * Copyright IBM Corporation 2018
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
package com.introproventures.graphql.jpa.query.schema.security;

import graphql.schema.GraphQLDirective;

import com.introproventures.graphql.jpa.query.schema.IQueryAuthorizationStrategy;
import java.util.Map;

public class AuthorizationStrategy implements IQueryAuthorizationStrategy {
    @Override
    public boolean isAuthorized(Object context, GraphQLDirective directive) {
        if (directive == null)
            return false;

        User user = (User) ((Map<String, Object>) context).get("userContext");

        if (user == null)
            return false;

        if (!user.getRoles().contains (directive.getArgument("role").getValue()))
            return false;

        return true;
    }
}

