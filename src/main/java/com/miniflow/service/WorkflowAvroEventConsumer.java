package com.miniflow.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Service;
@Slf4j
@Service
class WorkflowAvroEventConsumer {

    @KafkaListener(   topics = "workflow-instance-events",
            groupId = "miniflow-avro-consumer")
    public void consume(ConsumerRecord<String, GenericRecord> record,
                        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {

        GenericRecord value = record.value();

        System.out.println("📥 Topic: " + topic);
        System.out.println("🔑 Key:   " + record.key());
        System.out.println("🧾 Avro record: " + value);
        System.out.println("Patient: " + value.get("patient_id"));
        System.out.println("Doctor:  " + value.get("doctor_id"));
        System.out.println("Visit:   " + value.get("visit_type"));
    }


    @KafkaListener(topics = "workflow-instance-events-avro", groupId = "miniflow-test-consumer")
    public void consumeInstanceEvent(ConsumerRecord<String, GenericRecord> record,
                                     @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        GenericRecord value = record.value();

        log.info("Received instance event: {}", value.get("variables"));
    }

    @KafkaListener(topics = "workflow-step-events-avro", groupId = "miniflow-test-consumer")
    public void consumeStepEvent(ConsumerRecord<String, GenericRecord> record,
                                 @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        GenericRecord value = record.value();
        log.info("Received step event: {}", value.get("variables"));
    }



}
