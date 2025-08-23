package com.miniflow.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class WorkflowEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowEventConsumer.class);

    @KafkaListener(topics = "workflow-instance-events", groupId = "miniflow-test-consumer")
    public void consumeInstanceEvent(Map<String, Object> event) {
        logger.info("Received instance event: {}", event);
    }

    @KafkaListener(topics = "workflow-step-events", groupId = "miniflow-test-consumer")
    public void consumeStepEvent(Map<String, Object> event) {
        logger.info("Received step event: {}", event);
    }
}

// Made with Bob
