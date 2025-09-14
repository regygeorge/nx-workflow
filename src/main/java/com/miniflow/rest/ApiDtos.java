// ---------------------------------------------------------------------------
// src/main/java/com/miniflow/rest/ApiDtos.java
// ---------------------------------------------------------------------------
package com.miniflow.rest;

import java.util.Map;

import com.miniflow.core.DbBackedEngine;
import com.miniflow.persist.entity.WfTask;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ApiDtos {

 
    // --- response shapes (as you had) ---
    public record DeployResponse(String processId) {}
    public record StartResponse(
            String instanceId,
            String processId,
            String businessKey,
            String tokenAt,
            boolean completed,
            Map<String, Object> variables) {}
    public record InstanceView(
            String instanceId,
            String processId,
            String businessKey,
            String tokenAt,
            boolean completed,
            Map<String, Object> variables) {}
    public record TaskView(
            String id,
            String instanceId,
            String nodeId,
            String name,
            String dueDateTime) {}
 

    // --- simple factories you can call from controllers/services ---

    public static DeployResponse fromProcessId(String processId) {
        return new DeployResponse(processId);
    }
 
    /** Map an engine snapshot (returned by start/instance) to StartResponse. */
    public static StartResponse fromEngineStart(DbBackedEngine.InstanceView iv, String businessKey) {
        return new StartResponse(
                iv.id.toString(),
                iv.processId,
                businessKey,
                iv.tokenAt(),
                iv.completed,
                iv.variables);
    }
 
   // public record InstanceView(String instanceId, String processId, String businessKey, String tokenAt, boolean completed, Map<String, Object> variables) {
 

    /** Map an engine snapshot (returned by instance/snapshot) to InstanceView. */
    public static InstanceView fromEngineInstance(DbBackedEngine.InstanceView iv, String businessKey) {
        return new InstanceView(
                iv.id.toString(),
                iv.processId,
                businessKey,
                iv.tokenAt(),
                iv.completed,
                iv.variables);
    }

    /** Map a DB task row to TaskView. If you don’t store due dates yet, we return null. */
    public static TaskView from(WfTask t) {
        String due = null; // set to t.getDueAt().toString() if you add a dueAt field
        return new TaskView(
                t.id.toString(),
                t.instanceId.toString(),
                t.nodeId,
                t.name,
                due);
    }

    public static List<TaskView> fromTasks(List<WfTask> tasks) {
        return tasks.stream().map(ApiDtos::from).toList();
    }
}
