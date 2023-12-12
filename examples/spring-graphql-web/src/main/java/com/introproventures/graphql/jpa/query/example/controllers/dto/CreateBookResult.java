package com.introproventures.graphql.jpa.query.example.controllers.dto;

public record CreateBookResult(Long id) {
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private Long id;

        private Builder() {}

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public CreateBookResult build() {
            return new CreateBookResult(id);
        }
    }
}
