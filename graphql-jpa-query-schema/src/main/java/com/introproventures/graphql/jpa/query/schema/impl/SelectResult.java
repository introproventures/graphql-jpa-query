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

import java.util.Collections;
import java.util.List;

class SelectResult<T> {
    private final Long total;
    private final Long pages;
    private final List<T> select;

    private SelectResult(Builder<T> builder) {
        this.total = builder.total;
        this.pages = builder.pages;
        this.select = builder.select;
    }
    
    SelectResult() {
        this.total = null;
        this.pages = null;
        this.select = Collections.emptyList();
    }
    
    public Long getTotal() {
        return total;
    }

    public Long getPages() {
        return pages;
    }
    
    public List<T> getSelect() {
        return select;
    }

    /**
     * Creates builder to build {@link SelectResult}.
     * @return created builder
     */
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    /**
     * Builder to build {@link SelectResult}.
     */
    public static final class Builder<T> {

        private Long total;
        private Long pages;
        private List<T> select = Collections.emptyList();

        private Builder() {
        }

        public Builder<T> withTotal(Long total) {
            this.total = total;
            return this;
        }

        public Builder<T> withPages(Long pages) {
            this.pages = pages;
            return this;
        }

        public Builder<T> withSelect(List<T> select) {
            this.select = select;
            return this;
        }

        public SelectResult<T> build() {
            return new SelectResult<>(this);
        }
    }
}
