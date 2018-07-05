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

import java.util.ArrayList;
import java.util.List;

public class User {
    private String name;
    private String userId;
    private List<String> roles = new ArrayList<>(2);

    public String getName() {
        return name;
    }

    public String getUserId() {
        return userId;
    }

    public List<String> getRoles() {
        return roles;
    }

    public final static class Builder {
        private User user;
        public Builder() {
            user = new User();
        }

        public Builder name(String name) {
            user.name = name;
            return this;
        }

        public Builder userId(String userId) {
            user.userId = userId;
            return this;
        }

        public Builder withRole(String role) {
            user.roles.add(role);
            return this;
        }

        public User build() { return user; }
    }
}
