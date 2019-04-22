package com.introproventures.graphql.jpa.query.converter.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.springframework.format.annotation.DateTimeFormat;

@MappedSuperclass
public abstract class AbstractVariableEntity extends ActivitiEntityMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;

    private String name;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private Date createTime;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private Date lastUpdatedTime;

    private String executionId;

    @Convert(converter = VariableValueJsonConverter.class)
    @Column(columnDefinition="text")
    private VariableValue<?> value;

    private Boolean markedAsDeleted = false;
    
    private String processInstanceId;

    public AbstractVariableEntity() {
    }

    public AbstractVariableEntity(Long id,
                          String type,
                          String name,
                          String processInstanceId,
                          String serviceName,
                          String serviceFullName,
                          String serviceVersion,
                          String appName,
                          String appVersion,
                          Date createTime,
                          Date lastUpdatedTime,
                          String executionId) {
        super(serviceName,
              serviceFullName,
              serviceVersion,
              appName,
              appVersion);
        this.id = id;
        this.type = type;
        this.name = name;
        this.processInstanceId = processInstanceId;
        this.createTime = createTime;
        this.lastUpdatedTime = lastUpdatedTime;
        this.executionId = executionId;
    }

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public void setLastUpdatedTime(Date lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public <T> void setValue(T value) {
        this.value = new VariableValue<>(value);
    }

    public <T> T getValue() {
        return (T) value.getValue();
    }

    public Boolean getMarkedAsDeleted() {
        return markedAsDeleted;
    }

    public void setMarkedAsDeleted(Boolean markedAsDeleted) {
        this.markedAsDeleted = markedAsDeleted;
    }

    
    public String getProcessInstanceId() {
        return processInstanceId;
    }

    
    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }
 }