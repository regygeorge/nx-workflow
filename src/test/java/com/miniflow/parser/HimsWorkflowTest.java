package com.miniflow.parser;

import com.miniflow.core.EngineModel;
import com.miniflow.core.Expr;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class HimsWorkflowTest {

    private EngineModel.ProcessDefinition processDefinition;
    private EngineModel.Node gatewayNode;
    private EngineModel.SequenceFlow labFlow;
    private EngineModel.SequenceFlow defaultFlow;

    @BeforeEach
    public void setup() {
        // Load and parse the HIMS BPMN file
        InputStream is = getClass().getClassLoader().getResourceAsStream("bpmn/op/HIMS.bpmn");
        assertNotNull(is, "HIMS BPMN file not found");
        
        processDefinition = SimpleBpmnParser.parse(is);
        assertNotNull(processDefinition, "Process definition should not be null");
        
        // Get the gateway node
        gatewayNode = processDefinition.getNode("Gateway_lab_check");
        assertNotNull(gatewayNode, "Gateway node should not be null");
        assertEquals(EngineModel.NodeType.EXCLUSIVE_GATEWAY, gatewayNode.type);
        assertEquals(2, gatewayNode.outgoing.size());
        
        // Find the lab flow and default flow
        for (EngineModel.SequenceFlow flow : gatewayNode.outgoing) {
            if (flow.to.equals("id_lab")) {
                labFlow = flow;
            } else if (flow.to.equals("Event_01583nz")) {
                defaultFlow = flow;
            }
        }
        
        assertNotNull(labFlow, "Lab flow should not be null");
        assertNotNull(defaultFlow, "Default flow should not be null");
    }

    @Test
    public void testLabRequiredTrue() {
        // Test with lab_required = true
        Map<String, Object> vars = new HashMap<>();
        vars.put("lab_required", true);
        
        // Lab flow should be taken
        assertTrue(labFlow.condition.eval(vars), 
                "Lab flow should be taken when lab_required is true");
    }

    @Test
    public void testLabRequiredFalse() {
        // Test with lab_required = false
        Map<String, Object> vars = new HashMap<>();
        vars.put("lab_required", false);
        
        // Lab flow should not be taken
        assertFalse(labFlow.condition.eval(vars), 
                "Lab flow should not be taken when lab_required is false");
        
        // Default flow should be taken
        assertTrue(defaultFlow.condition.eval(vars), 
                "Default flow should be taken when lab_required is false");
    }

    @Test
    public void testLabRequiredNotSet() {
        // Test with lab_required not set
        Map<String, Object> vars = new HashMap<>();
        
        // Lab flow should not be taken
        assertFalse(labFlow.condition.eval(vars), 
                "Lab flow should not be taken when lab_required is not set");
        
        // Default flow should be taken
        assertTrue(defaultFlow.condition.eval(vars), 
                "Default flow should be taken when lab_required is not set");
    }

    @Test
    public void testLabRequiredNonBoolean() {
        // Test with lab_required set to a non-boolean value
        Map<String, Object> vars = new HashMap<>();
        vars.put("lab_required", "yes");
        
        // Lab flow should not be taken
        assertFalse(labFlow.condition.eval(vars), 
                "Lab flow should not be taken when lab_required is not a boolean");
        
        // Default flow should be taken
        assertTrue(defaultFlow.condition.eval(vars), 
                "Default flow should be taken when lab_required is not a boolean");
    }

    @Test
    public void testLabRequiredNull() {
        // Test with lab_required explicitly set to null
        Map<String, Object> vars = new HashMap<>();
        vars.put("lab_required", null);
        
        // Lab flow should not be taken
        assertFalse(labFlow.condition.eval(vars), 
                "Lab flow should not be taken when lab_required is null");
        
        // Default flow should be taken
        assertTrue(defaultFlow.condition.eval(vars), 
                "Default flow should be taken when lab_required is null");
    }

    @Test
    public void testDirectExpressionEvaluation() {
        // Test direct evaluation of the expressions using Expr class
        Map<String, Object> vars = new HashMap<>();
        vars.put("lab_required", true);
        
        // Evaluate the lab_required == true expression
        assertTrue(Expr.evalLogical("lab_required == true", vars),
                "Expression 'lab_required == true' should evaluate to true");
        
        vars.put("lab_required", false);
        
        // Evaluate the lab_required == false expression
        assertTrue(Expr.evalLogical("lab_required == false", vars),
                "Expression 'lab_required == false' should evaluate to true");
        
        // Test with SpEL syntax
        vars.put("lab_required", true);
        assertTrue(Expr.evalLogical("#{lab_required == true}", vars),
                "Expression '#{lab_required == true}' should evaluate to true");
    }
}

// Made with Bob
