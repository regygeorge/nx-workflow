package com.miniflow.service;

import com.miniflow.persist.entity.WfInstance;
import com.miniflow.persist.entity.WfTask;
import com.miniflow.util.AvroSerializationUtil;
import org.apache.avro.specific.SpecificRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class WorkflowAvroEventService {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowAvroEventService.class);
    private static final String INSTANCE_TOPIC = "workflow-instance-events-avro";
    private static final String STEP_TOPIC = "workflow-step-events-avro";

    @Autowired
    private KafkaTemplate<String, Object> avroKafkaTemplate;

    /**
     * Publish an instance created event using Avro serialization
     */
    public void publishInstanceCreatedEvent(WfInstance instance, Map<String, Object> variables) {
        try {
            // Create a builder for the Avro record
            Map<String, Object> builderParams = new HashMap<>();
            builderParams.put("eventId", UUID.randomUUID().toString());
            builderParams.put("eventType", "INSTANCE_CREATED");
            builderParams.put("timestamp", Instant.now().toEpochMilli());
            builderParams.put("instanceId", instance.id.toString());
            builderParams.put("processId", instance.processId);
            builderParams.put("businessKey", instance.businessKey);
            builderParams.put("status", instance.status);
            
            if (variables != null) {
                builderParams.put("variables", AvroSerializationUtil.convertVariables(variables));
            }
            
            // Create the Avro record using reflection
            SpecificRecord event = createAvroRecord("com.miniflow.events.WorkflowInstanceEvent", builderParams);
            
            // Send the event
            avroKafkaTemplate.send(INSTANCE_TOPIC, instance.id.toString(), event);
            logger.info("Published Avro instance created event for instance {}", instance.id);
        } catch (Exception e) {
            logger.error("Failed to publish Avro instance created event", e);
        }
    }

    /**
     * Publish an instance completed event using Avro serialization
     */
    public void publishInstanceCompletedEvent(WfInstance instance, Map<String, Object> variables) {
        try {
            // Create a builder for the Avro record
            Map<String, Object> builderParams = new HashMap<>();
            builderParams.put("eventId", UUID.randomUUID().toString());
            builderParams.put("eventType", "INSTANCE_COMPLETED");
            builderParams.put("timestamp", Instant.now().toEpochMilli());
            builderParams.put("instanceId", instance.id.toString());
            builderParams.put("processId", instance.processId);
            builderParams.put("businessKey", instance.businessKey);
            builderParams.put("status", instance.status);
            
            if (variables != null) {
                builderParams.put("variables", AvroSerializationUtil.convertVariables(variables));
            }
            
            // Create the Avro record using reflection
            SpecificRecord event = createAvroRecord("com.miniflow.events.WorkflowInstanceEvent", builderParams);
            
            // Send the event
            avroKafkaTemplate.send(INSTANCE_TOPIC, instance.id.toString(), event);
            logger.info("Published Avro instance completed event for instance {}", instance.id);
        } catch (Exception e) {
            logger.error("Failed to publish Avro instance completed event", e);
        }
    }

    /**
     * Publish a task created event using Avro serialization
     */
    public void publishTaskCreatedEvent(WfTask task, WfInstance instance, String nodeType, Map<String, Object> variables) {
        try {
            // Create a builder for the Avro record
            Map<String, Object> builderParams = new HashMap<>();
            builderParams.put("eventId", UUID.randomUUID().toString());
            builderParams.put("eventType", "TASK_CREATED");
            builderParams.put("timestamp", Instant.now().toEpochMilli());
            builderParams.put("instanceId", instance.id.toString());
            builderParams.put("processId", instance.processId);
            builderParams.put("businessKey", instance.businessKey);
            builderParams.put("nodeId", task.nodeId);
            builderParams.put("nodeName", task.name);
            builderParams.put("nodeType", nodeType);
            builderParams.put("taskId", task.id.toString());
            builderParams.put("status", task.state);
            
            if (variables != null) {
                builderParams.put("variables", AvroSerializationUtil.convertVariables(variables));
            }
            
            // Create the Avro record using reflection
            SpecificRecord event = createAvroRecord("com.miniflow.events.WorkflowStepEvent", builderParams);
            
            // Send the event
            avroKafkaTemplate.send(STEP_TOPIC, task.id.toString(), event);
            logger.info("Published Avro task created event for task {}", task.id);
        } catch (Exception e) {
            logger.error("Failed to publish Avro task created event", e);
        }
    }

    /**
     * Publish a task completed event using Avro serialization
     */
    public void publishTaskCompletedEvent(WfTask task, WfInstance instance, String nodeType, Map<String, Object> variables) {
        try {
            // Create a builder for the Avro record
            Map<String, Object> builderParams = new HashMap<>();
            builderParams.put("eventId", UUID.randomUUID().toString());
            builderParams.put("eventType", "TASK_COMPLETED");
            builderParams.put("timestamp", Instant.now().toEpochMilli());
            builderParams.put("instanceId", instance.id.toString());
            builderParams.put("processId", instance.processId);
            builderParams.put("businessKey", instance.businessKey);
            builderParams.put("nodeId", task.nodeId);
            builderParams.put("nodeName", task.name);
            builderParams.put("nodeType", nodeType);
            builderParams.put("taskId", task.id.toString());
            builderParams.put("status", task.state);
            
            if (variables != null) {
                builderParams.put("variables", AvroSerializationUtil.convertVariables(variables));
            }
            
            // Create the Avro record using reflection
            SpecificRecord event = createAvroRecord("com.miniflow.events.WorkflowStepEvent", builderParams);
            
            // Send the event
            avroKafkaTemplate.send(STEP_TOPIC, task.id.toString(), event);
            logger.info("Published Avro task completed event for task {}", task.id);
        } catch (Exception e) {
            logger.error("Failed to publish Avro task completed event", e);
        }
    }

    /**
     * Create an Avro record using reflection
     * 
     * @param className The fully qualified class name of the Avro record
     * @param params Map of parameter names and values to set on the builder
     * @return The created Avro record
     */
    @SuppressWarnings("unchecked")
    private SpecificRecord createAvroRecord(String className, Map<String, Object> params) {
        try {
            // Load the class
            Class<?> recordClass = Class.forName(className);
            
            // Get the newBuilder method
            Object builder = recordClass.getMethod("newBuilder").invoke(null);
            
            // Set the parameters on the builder
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String methodName = "set" + entry.getKey().substring(0, 1).toUpperCase() + entry.getKey().substring(1);
                
                // Find the appropriate setter method
                for (java.lang.reflect.Method method : builder.getClass().getMethods()) {
                    if (method.getName().equals(methodName) && method.getParameterCount() == 1) {
                        method.invoke(builder, entry.getValue());
                        break;
                    }
                }
            }
            
            // Build the record
            return (SpecificRecord) builder.getClass().getMethod("build").invoke(builder);
        } catch (Exception e) {
            logger.error("Failed to create Avro record", e);
            throw new RuntimeException("Failed to create Avro record", e);
        }
    }
}

// Made with Bob
