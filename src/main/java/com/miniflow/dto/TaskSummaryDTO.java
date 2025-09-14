package com.miniflow.dto;


import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public class TaskSummaryDTO {
    public final UUID taskId;
    public final UUID instanceId;
    public final String processId;
    public final String processName;
    public final String nodeId;
    public final String stepName;
    public final String assignee;
    public final OffsetDateTime createdAt;
    public final OffsetDateTime dueDateTime;
    public final OffsetDateTime completedAt;
    public final String state;
    public final String businessKey;
    public final Map<String, Object> variables; // If your mapping isn’t Map, change to Object
    public final String formKey;

    public TaskSummaryDTO(UUID taskId, UUID instanceId,
                          String processId, String processName,
                          String nodeId, String stepName,
                          String assignee, OffsetDateTime createdAt,
                          OffsetDateTime dueDateTime, OffsetDateTime completedAt,
                          String state, String businessKey,
                          Map<String, Object> variables,
                          String formKey) {
        this.taskId = taskId;
        this.instanceId = instanceId;
        this.processId = processId;
        this.processName = processName;
        this.nodeId = nodeId;
        this.stepName = stepName;
        this.assignee = assignee;
        this.createdAt = createdAt;
        this.dueDateTime = dueDateTime;
        this.completedAt = completedAt;
        this.state = state;
        this.businessKey = businessKey;
        this.variables = variables;
        this.formKey = formKey;
    }
}
