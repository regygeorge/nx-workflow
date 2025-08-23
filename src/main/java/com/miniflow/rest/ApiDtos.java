// ---------------------------------------------------------------------------
// src/main/java/com/miniflow/rest/ApiDtos.java
// ---------------------------------------------------------------------------
package com.miniflow.rest;

import java.util.Map;

public class ApiDtos {

    public record DeployResponse(String processId) {

    }

    public record StartResponse(String instanceId, String processId, String businessKey, String tokenAt, boolean completed, Map<String, Object> variables) {

    }

    public record InstanceView(String instanceId, String processId, String businessKey, String tokenAt, boolean completed, Map<String, Object> variables) {

    }

    public record TaskView(String id, String instanceId, String nodeId, String name, String dueDateTime) {

    }
}
