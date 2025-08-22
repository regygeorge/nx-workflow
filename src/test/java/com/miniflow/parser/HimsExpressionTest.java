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
        for (EngineModel.SequenceFlow flow : gateway.outgoing) {
            if (flow.to.equals("id_lab")) {
                labFlow = flow;
                break;
            }
        }
        assertNotNull(labFlow, "Flow to lab task not found");
        
        // Test the condition with lab_required = true
        assertTrue(labFlow.condition.eval(labRequiredTrue), 
                "Lab flow condition should evaluate to true when lab_required is true");
        
        // Test with lab_required = false
        Map<String, Object> labRequiredFalse = new HashMap<>();
        labRequiredFalse.put("lab_required", false);
        
        // Find the flow to end event
        EngineModel.SequenceFlow endFlow = null;
        for (EngineModel.SequenceFlow flow : gateway.outgoing) {
            if (!flow.to.equals("id_lab")) {
                endFlow = flow;
                break;
            }
        }
        assertNotNull(endFlow, "Flow to end event not found");
        
        // Test the condition with lab_required = false
        assertTrue(endFlow.condition.eval(labRequiredFalse), 
                "End flow condition should evaluate to true when lab_required is false");
        
        // Cross-check: lab flow should be false when lab_required is false
        assertFalse(labFlow.condition.eval(labRequiredFalse), 
                "Lab flow condition should evaluate to false when lab_required is false");
        
        // Cross-check: end flow should be false when lab_required is true
        assertFalse(endFlow.condition.eval(labRequiredTrue), 
                "End flow condition should evaluate to false when lab_required is true");
    }
}

// Made with Bob
