package com.miniflow.parser;

import com.miniflow.core.EngineModel;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ComplexExpressionTest {

    @Test
    public void testParseComplexExpressions() {
        // Load the BPMN file with complex expressions
        InputStream is = getClass().getClassLoader().getResourceAsStream("bpmn/complex-expressions.bpmn");
        assertNotNull(is, "BPMN file not found");
        
        // Parse the BPMN file
        EngineModel.ProcessDefinition def = SimpleBpmnParser.parse(is);
        
        // Verify the process definition
        assertEquals("complex-expressions", def.id);
        assertEquals("Complex Expressions Process", def.name);
        
        // Get the nodes
        EngineModel.Node gateway1 = def.getNode("gateway1");
        assertNotNull(gateway1);
        assertEquals(EngineModel.NodeType.EXCLUSIVE_GATEWAY, gateway1.type);
        assertEquals(2, gateway1.outgoing.size());
        
        // Test the first condition with variables that should make it true
        Map<String, Object> highValueVars = new HashMap<>();
        highValueVars.put("status", "APPROVED");
        highValueVars.put("amount", 2000);
        
        // The first flow should be chosen (to taskA)
        EngineModel.SequenceFlow flow = gateway1.outgoing.get(0);
        assertTrue(flow.condition.eval(highValueVars));
        assertEquals("taskA", flow.to);
        
        // Test with variables that should make the second condition true
        Map<String, Object> standardVars = new HashMap<>();
        standardVars.put("status", "PENDING");
        standardVars.put("amount", 500);
        
        // The second flow should be chosen (to taskB)
        flow = gateway1.outgoing.get(1);
        assertTrue(flow.condition.eval(standardVars));
        assertEquals("taskB", flow.to);
        
        // Get the second gateway
        EngineModel.Node gateway2 = def.getNode("gateway2");
        assertNotNull(gateway2);
        assertEquals(EngineModel.NodeType.EXCLUSIVE_GATEWAY, gateway2.type);
        assertEquals(2, gateway2.outgoing.size());
        
        // Test the complex condition with admin user
        Map<String, Object> adminVars = new HashMap<>();
        adminVars.put("user", "admin");
        adminVars.put("status", "REJECTED");
        adminVars.put("priority", 3);
        
        // The first flow should be chosen (to taskC) because user is admin
        flow = gateway2.outgoing.get(0);
        assertTrue(flow.condition.eval(adminVars));
        assertEquals("taskC", flow.to);
        
        // Test with high priority approved task
        Map<String, Object> highPriorityVars = new HashMap<>();
        highPriorityVars.put("user", "regular");
        highPriorityVars.put("status", "APPROVED");
        highPriorityVars.put("priority", 8);
        
        // The first flow should be chosen (to taskC) because status is APPROVED and priority > 5
        assertTrue(flow.condition.eval(highPriorityVars));
        
        // Test with regular user, low priority
        Map<String, Object> regularVars = new HashMap<>();
        regularVars.put("user", "regular");
        regularVars.put("status", "APPROVED");
        regularVars.put("priority", 3);
        
        // The second flow should be chosen (to taskD)
        flow = gateway2.outgoing.get(1);
        assertTrue(flow.condition.eval(regularVars));
        assertEquals("taskD", flow.to);
    }
}

// Made with Bob
