package com.miniflow.rest;

import com.miniflow.persist.entity.WfInstance;
import com.miniflow.persist.entity.WfTask;
import com.miniflow.service.WorkflowAvroEventService;
import com.miniflow.util.AvroSerializationUtil;
import org.apache.avro.specific.SpecificRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/avro")
public class AvroTestController {

    private static final Logger logger = LoggerFactory.getLogger(AvroTestController.class);
    
    @Autowired
    private WorkflowAvroEventService avroEventService;

    /**
     * Test endpoint to publish an instance event using Avro serialization
     */
    @PostMapping("/instance-event")
    public ResponseEntity<Map<String, Object>> publishInstanceEvent(
            @RequestParam(defaultValue = "INSTANCE_CREATED") String eventType,
            @RequestParam(required = false) String businessKey) {
        
        try {
            // Create a mock instance
            WfInstance instance = new WfInstance();
            instance.id = UUID.randomUUID();
            instance.processId = "test-process";
            instance.businessKey = businessKey != null ? businessKey : "test-business-key";
            instance.status = "ACTIVE";
            
            // Create some test variables
            Map<String, Object> variables = new HashMap<>();
            variables.put("stringVar", "test-value");
            variables.put("intVar", 123);
            variables.put("boolVar", true);
            variables.put("doubleVar", 123.45);
            
            // Publish the event
            if ("INSTANCE_CREATED".equals(eventType)) {
                avroEventService.publishInstanceCreatedEvent(instance, variables);
            } else {
                avroEventService.publishInstanceCompletedEvent(instance, variables);
            }
            
            // Return success response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("instanceId", instance.id.toString());
            response.put("eventType", eventType);
            response.put("timestamp", Instant.now().toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error publishing Avro instance event", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Test endpoint to publish a task event using Avro serialization
     */
    @PostMapping("/task-event")
    public ResponseEntity<Map<String, Object>> publishTaskEvent(
            @RequestParam(defaultValue = "TASK_CREATED") String eventType,
            @RequestParam(required = false) String businessKey) {
        
        try {
            // Create a mock instance
            WfInstance instance = new WfInstance();
            instance.id = UUID.randomUUID();
            instance.processId = "test-process";
            instance.businessKey = businessKey != null ? businessKey : "test-business-key";
            instance.status = "ACTIVE";
            
            // Create a mock task
            WfTask task = new WfTask();
            task.id = UUID.randomUUID();
            task.instanceId = instance.id;
            task.nodeId = "task1";
            task.name = "Test Task";
            task.state = "CREATED";
            
            // Create some test variables
            Map<String, Object> variables = new HashMap<>();
            variables.put("stringVar", "test-value");
            variables.put("intVar", 123);
            variables.put("boolVar", true);
            variables.put("doubleVar", 123.45);
            
            // Publish the event
            String nodeType = "userTask";
            if ("TASK_CREATED".equals(eventType)) {
                avroEventService.publishTaskCreatedEvent(task, instance, nodeType, variables);
            } else {
                avroEventService.publishTaskCompletedEvent(task, instance, nodeType, variables);
            }
            
            // Return success response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("instanceId", instance.id.toString());
            response.put("taskId", task.id.toString());
            response.put("eventType", eventType);
            response.put("timestamp", Instant.now().toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error publishing Avro task event", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Test endpoint to demonstrate serialization and deserialization of Avro records
     */
    @GetMapping("/serialize-test")
    public ResponseEntity<Map<String, Object>> testSerialization() {
        try {
            // Create a test record using reflection
            Class<?> recordClass = Class.forName("com.miniflow.events.WorkflowInstanceEvent");
            
            // Get the newBuilder method
            Object builder = recordClass.getMethod("newBuilder").invoke(null);
            
            // Set the parameters on the builder
            Map<String, Object> params = new HashMap<>();
            params.put("eventId", UUID.randomUUID().toString());
            params.put("eventType", "TEST_EVENT");
            params.put("timestamp", Instant.now().toEpochMilli());
            params.put("instanceId", UUID.randomUUID().toString());
            params.put("processId", "test-process");
            params.put("businessKey", "test-business-key");
            params.put("status", "ACTIVE");
            
            Map<CharSequence, Object> variables = new HashMap<>();
            variables.put("stringVar", "test-value");
            variables.put("intVar", 123);
            variables.put("boolVar", true);
            variables.put("doubleVar", 123.45);
            params.put("variables", variables);
            
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
            SpecificRecord record = (SpecificRecord) builder.getClass().getMethod("build").invoke(builder);
            
            // Serialize the record
            ByteBuffer serialized = AvroSerializationUtil.serializeToByteBuffer(record);
            
            // Deserialize the record using reflection
            Object deserialized = recordClass.getMethod("fromByteBuffer", ByteBuffer.class).invoke(null, serialized);
            
            // Return success response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("originalRecord", record.toString());
            response.put("deserializedRecord", deserialized.toString());
            response.put("recordsEqual", record.equals(deserialized));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error in Avro serialization test", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
}

// Made with Bob
