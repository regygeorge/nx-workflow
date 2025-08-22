package com.miniflow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
public class MiniFlowIntegrationTest {

    @Test
    public void testExpressionEvaluation() {
        // Create test variables
        Map<String, Object> vars = new HashMap<>();
        vars.put("status", "APPROVED");
        vars.put("amount", 1500);
        vars.put("user", "admin");
        
        // Test logical AND
        boolean result = com.miniflow.core.Expr.evalLogical("#{status == 'APPROVED' && amount > 1000}", vars);
        assertTrue(result);
        
        // Test logical OR
        result = com.miniflow.core.Expr.evalLogical("#{status == 'REJECTED' || user == 'admin'}", vars);
        assertTrue(result);
        
        // Test complex condition
        result = com.miniflow.core.Expr.evalLogical("#{(status == 'APPROVED' || status == 'PENDING') && (amount > 1000 || user == 'admin')}", vars);
        assertTrue(result);
    }
}

// Made with Bob
