package com.introproventures.graphql.jpa.query.web;

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotEmpty;

@ConfigurationProperties(prefix = "spring.graphql.jpa.query.web")
public class GraphQLControllerProperties {
    private boolean enabled;

    @NotEmpty
    private String path;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
