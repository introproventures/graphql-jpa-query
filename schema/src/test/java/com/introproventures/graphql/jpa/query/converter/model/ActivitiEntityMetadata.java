package com.introproventures.graphql.jpa.query.converter.model;

import jakarta.persistence.MappedSuperclass;
import java.util.Objects;

@MappedSuperclass
public abstract class ActivitiEntityMetadata {

    protected String serviceName;
    protected String serviceFullName;
    protected String serviceVersion;
    protected String appName;
    protected String appVersion;
    protected String serviceType;

    public ActivitiEntityMetadata() {}

    public ActivitiEntityMetadata(
        String serviceName,
        String serviceFullName,
        String serviceVersion,
        String appName,
        String appVersion
    ) {
        this.serviceName = serviceName;
        this.serviceFullName = serviceFullName;
        this.serviceVersion = serviceVersion;
        this.appName = appName;
        this.appVersion = appVersion;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceFullName() {
        return serviceFullName;
    }

    public void setServiceFullName(String serviceFullName) {
        this.serviceFullName = serviceFullName;
    }

    public String getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(appName, appVersion, serviceFullName, serviceName, serviceType, serviceVersion);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        ActivitiEntityMetadata other = (ActivitiEntityMetadata) obj;
        return (
            Objects.equals(appName, other.appName) &&
            Objects.equals(appVersion, other.appVersion) &&
            Objects.equals(serviceFullName, other.serviceFullName) &&
            Objects.equals(serviceName, other.serviceName) &&
            Objects.equals(serviceType, other.serviceType) &&
            Objects.equals(serviceVersion, other.serviceVersion)
        );
    }
}
