package com.miniflow.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class WorkflowAvroEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowAvroEventConsumer.class);

    @KafkaListener(
        topics = "workflow-instance-events", 
        groupId = "miniflow-avro-consumer",
        containerFactory = "avroKafkaListenerContainerFactory"
    )
    public void consumeInstanceEvent(Object event) {
        logger.info("Received Avro instance event: {}", event);
        logger.info("Event class: {}", event.getClass().getName());
    }

    @KafkaListener(
        topics = "workflow-step-events", 
        groupId = "miniflow-avro-consumer",
        containerFactory = "avroKafkaListenerContainerFactory"
    )
    public void consumeStepEvent(Object event) {
        logger.info("Received Avro step event: {}", event);
        logger.info("Event class: {}", event.getClass().getName());
    }
}

// Made with Bob
