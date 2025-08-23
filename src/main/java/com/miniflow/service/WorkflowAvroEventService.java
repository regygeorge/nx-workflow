package com.miniflow.service;


import com.miniflow.avro.AvroSchemas;
import com.miniflow.persist.entity.WfInstance;
import com.miniflow.persist.entity.WfTask;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowAvroEventService {

    private static final String INSTANCE_TOPIC = "workflow-instance-events-avro";
    private static final String STEP_TOPIC     = "workflow-step-events-avro";

    private static final Schema INSTANCE_EVENT_SCHEMA = AvroSchemas.loadSchema("/avro/InstanceEvent.avsc");
    private static final Schema STEP_EVENT_SCHEMA     = AvroSchemas.loadSchema("/avro/WorkflowStepEvent.avsc");

    private final KafkaTemplate<String, Object> avroKafkaTemplate;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Convert Map<String, Object> → Map<String, String> (JSON for complex values)
     * to satisfy an Avro field declared as: {"type":"map","values":"string"}.
     */
    private Map<String, String> toStringMap(Map<String, Object> vars) {
        if (vars == null || vars.isEmpty()) return Map.of();
        Map<String, String> out = new HashMap<>(vars.size());
        vars.forEach((k, v) -> {
            if (v == null) {
                out.put(k, null);
            } else if (v instanceof CharSequence || v instanceof Number || v instanceof Boolean) {
                out.put(k, String.valueOf(v));
            } else {
                try {
                    out.put(k, MAPPER.writeValueAsString(v));
                } catch (Exception e) {
                    out.put(k, String.valueOf(v));
                }
            }
        });
        return out;
    }

    public void publishInstanceCreatedEvent(WfInstance instance, Map<String, Object> variables) {
        try {
            GenericRecord record = new GenericRecordBuilder(INSTANCE_EVENT_SCHEMA)
                    .set("eventId", UUID.randomUUID().toString())
                    .set("eventType", "INSTANCE_CREATED")
                    .set("timestamp", Instant.now().toEpochMilli())
                    .set("instanceId", instance.id.toString())
                    .set("processId", instance.processId)          // null ok if schema allows
                    .set("businessKey", instance.businessKey)      // null ok if schema allows
                    .set("status", instance.status)
                    .set("variables", toStringMap(variables))
                    .build();

            avroKafkaTemplate.send(INSTANCE_TOPIC, instance.id.toString(), record);
            log.info("Published Avro instance CREATED event for instance {}", instance.id);
        } catch (Exception e) {
            log.error("Failed to publish Avro instance CREATED event", e);
        }
    }

    public void publishInstanceCompletedEvent(WfInstance instance, Map<String, Object> variables) {
        try {
            GenericRecord record = new GenericRecordBuilder(INSTANCE_EVENT_SCHEMA)
                    .set("eventId", UUID.randomUUID().toString())
                    .set("eventType", "INSTANCE_COMPLETED")
                    .set("timestamp", Instant.now().toEpochMilli())
                    .set("instanceId", instance.id.toString())
                    .set("processId", instance.processId)
                    .set("businessKey", instance.businessKey)
                    .set("status", instance.status)
                    .set("variables", toStringMap(variables))
                    .build();

            avroKafkaTemplate.send(INSTANCE_TOPIC, instance.id.toString(), record);
            log.info("Published Avro instance COMPLETED event for instance {}", instance.id);
        } catch (Exception e) {
            log.error("Failed to publish Avro instance COMPLETED event", e);
        }
    }

    public void publishTaskCreatedEvent(WfTask task, WfInstance instance, String nodeType, Map<String, Object> variables) {
        try {
            GenericRecord record = new GenericRecordBuilder(STEP_EVENT_SCHEMA)
                    .set("eventId", UUID.randomUUID().toString())
                    .set("eventType", "TASK_CREATED")
                    .set("timestamp", Instant.now().toEpochMilli())
                    .set("instanceId", instance.id.toString())
                    .set("processId", instance.processId)
                    .set("businessKey", instance.businessKey)
                    .set("nodeId", task.nodeId)
                    .set("nodeName", task.name)
                    .set("nodeType", nodeType)
                    .set("taskId", task.id.toString())
                    .set("status", task.state)
                    .set("variables", toStringMap(variables))
                    .build();

            avroKafkaTemplate.send(STEP_TOPIC, task.id.toString(), record);
            log.info("Published Avro TASK CREATED event for task {}", task.id);
        } catch (Exception e) {
            log.error("Failed to publish Avro TASK CREATED event", e);
        }
    }

    public void publishTaskCompletedEvent(WfTask task, WfInstance instance, String nodeType, Map<String, Object> variables) {
        try {
            GenericRecord record = new GenericRecordBuilder(STEP_EVENT_SCHEMA)
                    .set("eventId", UUID.randomUUID().toString())
                    .set("eventType", "TASK_COMPLETED")  // ✅ fixed
                    .set("timestamp", Instant.now().toEpochMilli())
                    .set("instanceId", instance.id.toString())
                    .set("processId", instance.processId)
                    .set("businessKey", instance.businessKey)
                    .set("nodeId", task.nodeId)
                    .set("nodeName", task.name)
                    .set("nodeType", nodeType)
                    .set("taskId", task.id.toString())
                    .set("status", task.state)
                    .set("variables", toStringMap(variables))
                    .build();

            avroKafkaTemplate.send(STEP_TOPIC, task.id.toString(), record);
            log.info("Published Avro TASK COMPLETED event for task {}", task.id);
        } catch (Exception e) {
            log.error("Failed to publish Avro TASK COMPLETED event", e);
        }
    }
}
