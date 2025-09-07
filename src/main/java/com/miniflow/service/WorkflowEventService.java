package com.miniflow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miniflow.persist.entity.WfInstance;
import com.miniflow.persist.entity.WfTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class WorkflowEventService {

    private static final String INSTANCE_TOPIC = "workflow-instance-events";
    private static final String STEP_TOPIC = "workflow-step-events";

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;

    public void publishInstanceCreatedEvent(WfInstance instance, Map<String, Object> variables) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", UUID.randomUUID().toString());
        event.put("eventType", "INSTANCE_CREATED");
        event.put("timestamp", Instant.now().toEpochMilli());
        event.put("instanceId", instance.id.toString());
        event.put("processId", instance.processId);
        event.put("businessKey", instance.businessKey);
        event.put("status", instance.status);
        
        // Convert variables to a format compatible with Avro
        Map<String, Object> convertedVars = convertVariables(variables);
        if (convertedVars != null) {
            event.put("variables", convertedVars);
        }

        kafkaTemplate.send(INSTANCE_TOPIC, instance.id.toString(), event);
    }

    public void publishInstanceCompletedEvent(WfInstance instance, Map<String, Object> variables) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", UUID.randomUUID().toString());
        event.put("eventType", "INSTANCE_COMPLETED");
        event.put("timestamp", Instant.now().toEpochMilli());
        event.put("instanceId", instance.id.toString());
        event.put("processId", instance.processId);
        event.put("businessKey", instance.businessKey);
        event.put("status", instance.status);
        
        // Convert variables to a format compatible with Avro
        Map<String, Object> convertedVars = convertVariables(variables);
        if (convertedVars != null) {
            event.put("variables", convertedVars);
        }

        kafkaTemplate.send(INSTANCE_TOPIC, instance.id.toString(), event);
    }

    public void publishTaskCreatedEvent(WfTask task, WfInstance instance, String nodeType, Map<String, Object> variables) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", UUID.randomUUID().toString());
        event.put("eventType", "TASK_CREATED");
        event.put("timestamp", Instant.now().toEpochMilli());
        event.put("instanceId", instance.id.toString());
        event.put("processId", instance.processId);
        event.put("businessKey", instance.businessKey);
        event.put("nodeId", task.nodeId);
        event.put("nodeName", task.name);
        event.put("nodeType", nodeType);
        event.put("taskId", task.id.toString());
        event.put("status", task.state);
        
        // Convert variables to a format compatible with Avro
        Map<String, Object> convertedVars = convertVariables(variables);
        if (convertedVars != null) {
            event.put("variables", convertedVars);
        }

        kafkaTemplate.send(STEP_TOPIC, task.id.toString(), event);
    }

    public void publishTaskCompletedEvent(WfTask task, WfInstance instance, String nodeType, Map<String, Object> variables) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", UUID.randomUUID().toString());
        event.put("eventType", "TASK_COMPLETED");
        event.put("timestamp", Instant.now().toEpochMilli());
        event.put("instanceId", instance.id.toString());
        event.put("processId", instance.processId);
        event.put("businessKey", instance.businessKey);
        event.put("nodeId", task.nodeId);
        event.put("nodeName", task.name);
        event.put("nodeType", nodeType);
        event.put("taskId", task.id.toString());
        event.put("status", task.state);
        
        // Convert variables to a format compatible with Avro
        Map<String, Object> convertedVars = convertVariables(variables);
        if (convertedVars != null) {
            event.put("variables", convertedVars);
        }

        kafkaTemplate.send(STEP_TOPIC, task.id.toString(), event);
    }

    private Map<String, Object> convertVariables(Map<String, Object> variables) {
        if (variables == null) {
            return null;
        }
        
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            Object value = entry.getValue();
            // Only include primitive types and strings
            if (value == null || value instanceof String || value instanceof Number || value instanceof Boolean) {
                result.put(entry.getKey(), value);
            } else {
                // Convert complex objects to string representation
                result.put(entry.getKey(), value.toString());
            }
        }
        return result;
    }
}

// Made with Bob
