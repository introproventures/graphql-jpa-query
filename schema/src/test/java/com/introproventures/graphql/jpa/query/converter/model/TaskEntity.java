package com.introproventures.graphql.jpa.query.converter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import org.springframework.format.annotation.DateTimeFormat;

@Entity(name = "Task")
@Table(
    name = "TASK",
    indexes = {
        @Index(name = "task_status_idx", columnList = "status", unique = false),
        @Index(name = "task_processInstance_idx", columnList = "processInstanceId", unique = false),
    }
)
public class TaskEntity extends ActivitiEntityMetadata {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    private String assignee;
    private String name;
    private String description;
    private String businessKey;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date createdDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date dueDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date claimedDate;

    private int priority;
    private String processDefinitionId;
    private String processInstanceId;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    private String owner;
    private String parentTaskId;
    private String formKey;
    private Date completedDate;
    private Long duration;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date lastModified;

    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date createdTo;

    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date createdFrom;

    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date lastModifiedTo;

    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date lastModifiedFrom;

    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date lastClaimedTo;

    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date lastClaimedFrom;

    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date completedTo;

    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date completedFrom;

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "taskId",
        referencedColumnName = "id",
        insertable = false,
        updatable = false,
        foreignKey = @jakarta.persistence.ForeignKey(value = ConstraintMode.NO_CONSTRAINT, name = "none")
    )
    private Set<TaskVariableEntity> variables;

    public TaskEntity() {}

    public TaskEntity(
        String id,
        String assignee,
        String name,
        String description,
        Date createTime,
        Date dueDate,
        int priority,
        String processDefinitionId,
        String processInstanceId,
        String serviceName,
        String serviceFullName,
        String serviceVersion,
        String appName,
        String appVersion,
        TaskStatus status,
        Date lastModified,
        Date claimedDate,
        String owner,
        String parentTaskId,
        String formKey
    ) {
        super(serviceName, serviceFullName, serviceVersion, appName, appVersion);
        this.id = id;
        this.assignee = assignee;
        this.name = name;
        this.description = description;
        this.createdDate = createTime;
        this.dueDate = dueDate;
        this.priority = priority;
        this.processDefinitionId = processDefinitionId;
        this.processInstanceId = processInstanceId;
        this.status = status;
        this.lastModified = lastModified;
        this.claimedDate = claimedDate;
        this.owner = owner;
        this.parentTaskId = parentTaskId;
        this.formKey = formKey;
    }

    public String getId() {
        return id;
    }

    public String getAssignee() {
        return assignee;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public int getPriority() {
        return priority;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public boolean isStandAlone() {
        return processInstanceId == null;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    @Transient
    public Date getLastModifiedTo() {
        return lastModifiedTo;
    }

    public void setLastModifiedTo(Date lastModifiedTo) {
        this.lastModifiedTo = lastModifiedTo;
    }

    @Transient
    public Date getLastModifiedFrom() {
        return lastModifiedFrom;
    }

    public Date getClaimedDate() {
        return claimedDate;
    }

    public void setClaimedDate(Date claimedDate) {
        this.claimedDate = claimedDate;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setLastModifiedFrom(Date lastModifiedFrom) {
        this.lastModifiedFrom = lastModifiedFrom;
    }

    /**
     * @return the variableEntities
     */
    public Set<TaskVariableEntity> getVariables() {
        return this.variables;
    }

    /**
     * @param variables the variableEntities to set
     */
    public void setVariables(Set<TaskVariableEntity> variables) {
        this.variables = variables;
    }

    public String getParentTaskId() {
        return parentTaskId;
    }

    public void setParentTaskId(String parentTaskId) {
        this.parentTaskId = parentTaskId;
    }

    public String getFormKey() {
        return formKey;
    }

    public void setFormKey(String formKey) {
        this.formKey = formKey;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Transient
    public Date getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(Date endDate) {
        this.completedDate = endDate;
    }

    @Transient
    public Date getCreatedTo() {
        return createdTo;
    }

    public void setCreatedTo(Date createdTo) {
        this.createdTo = createdTo;
    }

    @Transient
    public Date getCreatedFrom() {
        return createdFrom;
    }

    public void setCreatedFrom(Date createdFrom) {
        this.createdFrom = createdFrom;
    }

    @Transient
    public Date getLastClaimedTo() {
        return lastClaimedTo;
    }

    public void setLastClaimedTo(Date lastClaimedTo) {
        this.lastClaimedTo = lastClaimedTo;
    }

    @Transient
    public Date getLastClaimedFrom() {
        return lastClaimedFrom;
    }

    public void setLastClaimedFrom(Date lastClaimedFrom) {
        this.lastClaimedFrom = lastClaimedFrom;
    }

    @Transient
    public Date getCompletedTo() {
        return completedTo;
    }

    public void setCompletedTo(Date completedTo) {
        this.completedTo = completedTo;
    }

    @Transient
    public Date getCompletedFrom() {
        return completedFrom;
    }

    public void setCompletedFrom(Date completedFrom) {
        this.completedFrom = completedFrom;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(id);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        TaskEntity other = (TaskEntity) obj;
        return Objects.equals(id, other.id);
    }
}
