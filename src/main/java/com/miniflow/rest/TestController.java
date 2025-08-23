package com.miniflow.rest;

import com.miniflow.persist.entity.WfInstance;
import com.miniflow.service.WorkflowAvroEventService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private WorkflowAvroEventService eventService;

    @GetMapping("/publish")
    public ResponseEntity<String> testPublish() {
        // Create a mock instance
        WfInstance instance = new WfInstance();
        instance.id = UUID.randomUUID();
        instance.processId = "test-process";
        instance.businessKey = "test-key";
        instance.status = "RUNNING";
        
        // Create some test variables
        Map<String, Object> variables = new HashMap<>();
        variables.put("stringVar", "test value");
        variables.put("numberVar", 123);
        variables.put("booleanVar", true);
        
        // Publish an event
        eventService.publishInstanceCreatedEvent(instance, variables);
        
        return ResponseEntity.ok("Event published successfully. Check logs for consumer output.");
    }
}

// Made with Bob
