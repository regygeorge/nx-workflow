package com.miniflow.parser;

import com.miniflow.core.EngineModel;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class HimsExpressionTest {

    @Test
    public void testHimsExpressions() {
        // Load the HIMS BPMN file
        InputStream is = getClass().getClassLoader().getResourceAsStream("bpmn/op/HIMS.bpmn");
        assertNotNull(is, "HIMS BPMN file not found");
        
        // Parse the BPMN file
        EngineModel.ProcessDefinition def = SimpleBpmnParser.parse(is);
        
        // Verify the process definition
        assertEquals("hims", def.id);
        assertEquals("HIMS", def.name);
        
        // Get the gateway node
        EngineModel.Node gateway = def.getNode("Gateway_lab_check");
        assertNotNull(gateway);
        assertEquals(EngineModel.NodeType.EXCLUSIVE_GATEWAY, gateway.type);
        assertEquals(2, gateway.outgoing.size());
        
        // Test with lab_required = true
        Map<String, Object> labRequiredTrue = new HashMap<>();
        labRequiredTrue.put("lab_required", true);
        
        // Find the flow to lab task
        EngineModel.SequenceFlow labFlow = null;
        EngineModel.SequenceFlow defaultFlow = null;
        for (EngineModel.SequenceFlow flow : gateway.outgoing) {
            if (flow.to.equals("id_lab")) {
                labFlow = flow;
            } else {
                defaultFlow = flow;
            }
        }
        assertNotNull(labFlow, "Flow to lab task not found");
        assertNotNull(defaultFlow, "Default flow not found");
        
        // Test the condition with lab_required = true
        assertTrue(labFlow.condition.eval(labRequiredTrue),
                "Lab flow condition should evaluate to true when lab_required is true");
        
        // Test with lab_required = false
        Map<String, Object> labRequiredFalse = new HashMap<>();
        labRequiredFalse.put("lab_required", false);
        
        // Test the lab flow with lab_required = false
        assertFalse(labFlow.condition.eval(labRequiredFalse),
                "Lab flow condition should evaluate to false when lab_required is false");
        
        // Test the default flow - should always be true
        assertTrue(defaultFlow.condition.eval(labRequiredFalse),
                "Default flow condition should always evaluate to true");
        
        // Test with lab_required not set
        Map<String, Object> noLabRequired = new HashMap<>();
        
        // Lab flow should be false when lab_required is not set
        assertFalse(labFlow.condition.eval(noLabRequired),
                "Lab flow condition should evaluate to false when lab_required is not set");
        
        // Default flow should be true when lab_required is not set
        assertTrue(defaultFlow.condition.eval(noLabRequired),
                "Default flow condition should evaluate to true when lab_required is not set");
        
        // Test with lab_required set to a non-boolean value
        Map<String, Object> nonBooleanLabRequired = new HashMap<>();
        nonBooleanLabRequired.put("lab_required", "some string");
        
        // Lab flow should be false when lab_required is not a boolean
        assertFalse(labFlow.condition.eval(nonBooleanLabRequired),
                "Lab flow condition should evaluate to false when lab_required is not a boolean");
        
        // Default flow should be true when lab_required is not a boolean
        assertTrue(defaultFlow.condition.eval(nonBooleanLabRequired),
                "Default flow condition should evaluate to true when lab_required is not a boolean");
    }
}

// Made with Bob
