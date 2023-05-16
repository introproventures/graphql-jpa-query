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

public class PagedResult<T> {

    private final long limit;
    private final long total;
    private final long pages;
    private final int offset;
    private final List<T> select;

    private PagedResult(Builder<T> builder) {
        this.limit = builder.limit;
        this.total = builder.total;
        this.offset = builder.offset;
        this.select = builder.select;
        this.pages = ((Double) Math.ceil(total / (double) limit)).longValue();
    }

    public Long getTotal() {
        return total;
    }

    public Long getPages() {
        return pages;
    }

    public Integer getOffset() {
        return offset;
    }

    public List<T> getSelect() {
        return select;
    }

    public long getLimit() {
        return limit;
    }

    /**
     * Creates builder to build {@link PagedResult}.
     * @return created builder
     */
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    /**
     * Builder to build {@link PagedResult}.
     */
    public static final class Builder<T> {

        private long limit;
        private long total;
        private long pages;
        private int offset;
        private List<T> select = Collections.emptyList();

        private Builder() {}

        /**
         * Builder method for limit parameter.
         * @param limit field to set
         * @return builder
         */
        public Builder<T> withLimit(long limit) {
            this.limit = limit;
            return this;
        }

        /**
         * Builder method for total parameter.
         * @param total field to set
         * @return builder
         */
        public Builder<T> withTotal(long total) {
            this.total = total;
            return this;
        }

        /**
         * Builder method for pages parameter.
         * @param pages field to set
         * @return builder
         */
        public Builder<T> withPages(long pages) {
            this.pages = pages;
            return this;
        }

        /**
         * Builder method for offset parameter.
         * @param offset field to set
         * @return builder
         */
        public Builder<T> withOffset(int offset) {
            this.offset = offset;
            return this;
        }

        /**
         * Builder method for select parameter.
         * @param select field to set
         * @return builder
         */
        public Builder<T> withSelect(List<T> select) {
            this.select = select;
            return this;
        }

        /**
         * Builder method of the builder.
         * @return built class
         */
        public PagedResult<T> build() {
            return new PagedResult<>(this);
        }
    }
}
