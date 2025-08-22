package com.miniflow.core;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ExprTest {

    @Test
    public void testSimpleExpressions() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("a", 5);
        vars.put("b", "hello");
        vars.put("c", true);

        // Test simple variable access
        assertEquals(5, Expr.eval("a", vars));
        assertEquals("hello", Expr.eval("b", vars));
        assertTrue((Boolean) Expr.eval("c", vars));
        
        // Test with SpEL syntax
        assertEquals(5, Expr.eval("#{a}", vars));
        assertEquals("hello", Expr.eval("#{b}", vars));
        assertTrue((Boolean) Expr.eval("#{c}", vars));
    }

    @Test
    public void testComplexExpressions() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("status", "APPROVED");
        vars.put("amount", 1500);
        vars.put("user", "admin");
        
        // Test simple equality
        assertTrue(Expr.evalLogical("status == 'APPROVED'", vars));
        
        // Test simple comparison
        assertTrue(Expr.evalLogical("amount > 1000", vars));
        
        // Test with SpEL syntax
        assertTrue(Expr.evalLogical("#{status == 'APPROVED'}", vars));
        assertTrue(Expr.evalLogical("#{amount > 1000}", vars));
    }

    @Test
    public void testNullAndMissingVariables() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("a", null);
        vars.put("b", "value");
        
        // Test null variable
        assertNull(Expr.eval("a", vars));
        
        // Test missing variable
        assertNull(Expr.eval("c", vars));
        
        // Test hasVariable method
        assertFalse(Expr.hasVariable("c", vars));
        assertTrue(Expr.hasVariable("b", vars));
        assertTrue(Expr.hasVariable("a", vars)); // 'a' exists but is null
        
        // Test with null map
        assertNull(Expr.eval("a", null));
        
        // Test with null expression
        assertNull(Expr.eval(null, vars));
    }
}

// Made with Bob
