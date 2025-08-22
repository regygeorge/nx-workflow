package com.miniflow.parser;

import com.miniflow.core.EngineModel;
import com.miniflow.core.FlowLogger;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HimsFlowLoggerTest {

    @Test
    public void testLogHimsFlow() {
        // Load the HIMS BPMN file
        InputStream is = getClass().getClassLoader().getResourceAsStream("bpmn/op/HIMS.bpmn");
        assertNotNull(is, "HIMS BPMN file not found");
        
        // Parse the BPMN file
        EngineModel.ProcessDefinition def = SimpleBpmnParser.parse(is);
        
        // Log the process definition using FlowLogger
        String processDescription = FlowLogger.describe(def);
        
        // Print the process description to the console
        System.out.println("HIMS Process Description:");
        System.out.println("========================");
        System.out.println(processDescription);
        
        // This will show the condition expressions for each sequence flow
        // and help verify that they are correctly parsed
    }
}

// Made with Bob
