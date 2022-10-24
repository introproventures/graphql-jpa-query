/*
 * Copyright 2017 IntroPro Ventures, Inc. and/or its affiliates.
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
package com.introproventures.graphql.jpa.query.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@ConfigurationProperties(prefix="spring.graphql.jpa.query")
@Validated
public class GraphQLJpaQueryProperties {

    /**
     * Provides the name of GraphQL schema. This is required attribute.
     */
    @NotEmpty
    private String name;
    
    /**
     * Provides the description of GraphQL schema. Cannot be null.
     */
    @NotEmpty
    private String description;

    /**
     * Enable or disable distinct parameter.
     */
    private boolean isUseDistinctParameter = false;

    /**
     * Enable or disable distinct distinct sql query fetcher.
     */
    private boolean isDefaultDistinct = true;

    /**
     * Set default value for optional argument for join fetch collections.
     */
    private boolean toManyDefaultOptional = true;

    /**
     * Enable or disable GraphQL Relay Connection support. Default is false
     */
    private boolean enableRelay = false;
    
    /**
     * Enable or disable QraphQL module services.
     */
    private boolean enabled;

    /**
     * Web path for web controller
     * Use 'spring.graphql.jpa.query.web.path' to customize default /graphql path
     */
    @Deprecated
    private String path;

    /**
     * Default Constructor 
     */
    GraphQLJpaQueryProperties() {
    }
    
    /**
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the useDistinctParameter
     */
    public boolean isUseDistinctParameter() {
        return isUseDistinctParameter;
    }

    /**
     * @param useDistinctParameter the useDistinctParameter to set
     */
    public void setUseDistinctParameter(boolean useDistinctParameter) {
        this.isUseDistinctParameter = useDistinctParameter;
    }

    /**
     * @return the distinctFetcher
     */
    public boolean isDefaultDistinct() {
        return isDefaultDistinct;
    }

    /**
     * @param isDefaultDistinct the distinctFetcher to set
     */
    public void setDefaultDistinct(boolean isDefaultDistinct) {
        this.isDefaultDistinct = isDefaultDistinct;
    }

    /**
     * @return the enabled
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * @param enabled the enabled to set
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return this.path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    
    public boolean isToManyDefaultOptional() {
        return toManyDefaultOptional;
    }

    
    public void setToManyDefaultOptional(boolean toManyDefaultOptional) {
        this.toManyDefaultOptional = toManyDefaultOptional;
    }

    
    public boolean isEnableRelay() {
        return enableRelay;
    }

    
    public void setEnableRelay(boolean enableRelay) {
        this.enableRelay = enableRelay;
    }
    
}
