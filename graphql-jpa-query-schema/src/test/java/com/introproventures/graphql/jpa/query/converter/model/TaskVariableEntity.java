package com.introproventures.graphql.jpa.query.converter.model;

import java.util.Date;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity(name="TaskVariable")
@Table(name = "TASK_VARIABLE")
public class TaskVariableEntity extends AbstractVariableEntity {

    private String taskId;
    
    @JsonIgnore
    @ManyToOne(optional = true, fetch=FetchType.LAZY)
    @JoinColumn(name = "taskId", referencedColumnName = "id", insertable = false, updatable = false, nullable = true
            , foreignKey = @jakarta.persistence.ForeignKey(value = ConstraintMode.NO_CONSTRAINT, name = "none"))
    private TaskEntity task;    
    
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
    
    public TaskEntity getTask() {
        return this.task;
    }

    public void setTask(TaskEntity taskEntity) {
        this.task = taskEntity;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(taskId);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        TaskVariableEntity other = (TaskVariableEntity) obj;
        return Objects.equals(taskId, other.taskId);
    }
    
    
}
