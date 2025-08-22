package com.miniflow.core;

import com.miniflow.core.EngineModel.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Utility methods for creating test data
 */
public class TestUtils {

    /**
     * Creates a simple process definition with a start event, user task, and end event
     * 
     * @return A simple process definition
     */
    public static ProcessDefinition createSimpleProcessDefinition() {
        // Create a builder for the process
        Builder builder = new Builder("simple", "Simple Process");
        
        // First create all nodes
        builder.start("start");
        builder.userTask("userTask", "User Task");
        builder.end("end");
        
        // Then create the flows between nodes
        builder.flow("start", "userTask");
        builder.flow("userTask", "end");
        
        return builder.build();
    }
    
    /**
     * Creates a process definition with a gateway
     * 
     * @return A process definition with a gateway
     */
    public static ProcessDefinition createGatewayProcessDefinition() {
        Builder builder = new Builder("gateway", "Gateway Process");
        
        // Create nodes
        builder.start("start");
        builder.exclusiveGateway("gateway", "Decision");
        builder.userTask("taskA", "Task A");
        builder.userTask("taskB", "Task B");
        builder.end("end");
        
        // Create flows
        builder.flow("start", "gateway");
        builder.flow("gateway", "taskA", vars -> "A".equals(vars.get("path")));
        builder.flow("gateway", "taskB", vars -> "B".equals(vars.get("path")));
        builder.flow("taskA", "end");
        builder.flow("taskB", "end");
        
        return builder.build();
    }
    
    /**
     * Creates a process definition with a parallel gateway
     * 
     * @return A process definition with parallel execution
     */
    public static ProcessDefinition createParallelProcessDefinition() {
        Builder builder = new Builder("parallel", "Parallel Process");
        
        // Create nodes
        builder.start("start");
        builder.parallelGateway("fork", "Fork");
        builder.userTask("taskA", "Task A");
        builder.userTask("taskB", "Task B");
        builder.parallelGateway("join", "Join");
        builder.end("end");
        
        // Create flows
        builder.flow("start", "fork");
        builder.flow("fork", "taskA");
        builder.flow("fork", "taskB");
        builder.flow("taskA", "join");
        builder.flow("taskB", "join");
        builder.flow("join", "end");
        
        return builder.build();
    }
    
    /**
     * Creates a random UUID
     * 
     * @return A random UUID
     */
    public static UUID randomUUID() {
        return UUID.randomUUID();
    }
    
    /**
     * Creates a map with test variables
     * 
     * @return A map with test variables
     */
    public static Map<String, Object> createTestVariables() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("status", "APPROVED");
        vars.put("amount", 1000);
        vars.put("user", "testuser");
        return vars;
    }
}

// Made with Bob
