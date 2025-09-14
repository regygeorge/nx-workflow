package com.miniflow.core;

import java.util.Map;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;


/**
 * Expression evaluator for BPMN conditions using Spring Expression Language (SpEL)
 */
public class Expr {
    private static final ExpressionParser parser = new SpelExpressionParser();

    /**
     * Evaluates a Spring Expression Language (SpEL) expression with the given variables
     * 
     * @param expr The SpEL expression to evaluate
     * @param vars The variables to use in the evaluation context
     * @return The result of the expression evaluation
     */
    public static Object eval(String expr, Map<String, Object> vars) {
        if (expr == null || expr.isBlank()) {
            return null;
        }
        
        // Handle SpEL expressions with #{...} or ${...} syntax
        String actualExpr = expr;
        if ((expr.startsWith("#{") && expr.endsWith("}")) ||
            (expr.startsWith("${") && expr.endsWith("}"))||
                (expr.startsWith("={") && expr.endsWith("}"))) {

            actualExpr = expr.substring(2, expr.length() - 1);
        }
        
        // For BPMN expressions, convert direct property access to map access
        // Convert "status == 'APPROVED'" to "['status'] == 'APPROVED'"
        if (vars != null) {
            for (String key : vars.keySet()) {
                // Replace standalone variable references that aren't already prefixed
                actualExpr = actualExpr.replaceAll("\\b" + key + "\\b(?![\\w\\.])", "['"+key+"']");
            }
        }
        
        StandardEvaluationContext context = new StandardEvaluationContext();
        
        // Add variables to the context
        if (vars != null) {
            // Set the root object first
            context.setRootObject(vars);
            
            // Then add variables as variables
            for (Map.Entry<String, Object> entry : vars.entrySet()) {
                context.setVariable(entry.getKey(), entry.getValue());
            }
        }
        
        // Parse and evaluate the expression
        try {
            Expression expression = parser.parseExpression(actualExpr);
            return expression.getValue(context);
        } catch (Exception e) {
            // Return null for any evaluation errors
            return null;
        }
    }

    /**
     * Evaluates a logical expression and returns a boolean result
     * 
     * @param expr The logical expression to evaluate
     * @param vars The variables to use in the evaluation context
     * @return The boolean result of the expression, or false if evaluation fails
     */
    public static boolean evalLogical(String expr, Map<String, Object> vars) {
        Object result = eval(expr, vars);
        
        if (result instanceof Boolean) {
            return (Boolean) result;
        } else if (result instanceof String) {
            // Try to convert string to boolean
            String str = ((String) result).toLowerCase();
            if (str.equals("true") || str.equals("yes") || str.equals("1")) {
                return true;
            }
        } else if (result instanceof Number) {
            // Non-zero numbers are true
            return ((Number) result).doubleValue() != 0;
        }
        
        // Handle null or non-boolean results
        return false;
    }

    /** Evaluate and coerce to String (null on errors). */
    public static String evalString(String expr, Map<String,Object> vars) {
        Object r = eval(expr, vars);
        return (r == null) ? null : String.valueOf(r);
    }


    /**
     * Checks if a variable exists in the variables map
     * 
     * @param varName The variable name to check
     * @param vars The variables map
     * @return true if the variable exists and is not null
     */
    public static boolean hasVariable(String varName, Map<String, Object> vars) {
        if (vars == null || varName == null || varName.isBlank()) {
            return false;
        }
        return vars.containsKey(varName);
    }

}
