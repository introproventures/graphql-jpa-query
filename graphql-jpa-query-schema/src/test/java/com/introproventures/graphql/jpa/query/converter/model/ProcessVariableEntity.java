package com.introproventures.graphql.jpa.query.converter.model;


import java.util.Date;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity(name="ProcessVariable")
@Table(name = "PROCESS_VARIABLE")
public class ProcessVariableEntity extends AbstractVariableEntity {

    public ProcessVariableEntity() {
    }

    public ProcessVariableEntity(Long id,
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
        
    }

    public String getTaskId() {
        return null;
    }
    
    public boolean isTaskVariable() {
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        return true;
    }

}
