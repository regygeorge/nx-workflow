package com.miniflow.dto;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public interface TaskSummaryView {
    UUID getTaskId();
    UUID getInstanceId();
    String getProcessId();
    String getProcessName();
    String getNodeId();
    String getStepName();
    String getAssignee();
    OffsetDateTime getCreatedAt();
    OffsetDateTime getDueDateTime();
    OffsetDateTime getCompletedAt();
    String getState();
    String getBusinessKey();
    Map<String,Object> getVariables();
    String getFormKey();
}
