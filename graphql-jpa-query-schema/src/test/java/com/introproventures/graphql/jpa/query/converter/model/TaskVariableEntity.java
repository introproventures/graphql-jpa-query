package com.introproventures.graphql.jpa.query.converter.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity(name="TaskVariable")
@Table(name = "TASK_VARIABLE")
public class TaskVariableEntity extends AbstractVariableEntity {

    private String taskId;
    
    public TaskVariableEntity() {
    }

    public TaskVariableEntity(Long id,
                          String type,
                          String name,
                          String processInstanceId,
                          String serviceName,
                          String serviceFullName,
                          String serviceVersion,
                          String appName,
                          String appVersion,
                          String taskId,
                          Date createTime,
                          Date lastUpdatedTime,
                          String executionId) {
        super(id,
              type,
              name,
              processInstanceId,
              serviceName,
              serviceFullName,
              serviceVersion,
              appName,
              appVersion,
              createTime,
              lastUpdatedTime,
              executionId);

        this.taskId = taskId;
    }

    public String getTaskId() {
        return taskId;
    }
    
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public boolean isTaskVariable() {
        return true;
    }
    
}