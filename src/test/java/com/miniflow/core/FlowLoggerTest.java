package com.miniflow.core;

import com.miniflow.persist.entity.WfTask;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class FlowLoggerTest {

    @Test
    public void testLogTaskWithDueDate() {
        // Create a task with a due date
        WfTask task = new WfTask();
        task.id = UUID.randomUUID();
        task.instanceId = UUID.randomUUID();
        task.nodeId = "task_1";
        task.name = "Test Task";
        task.state = "OPEN";
        task.createdAt = OffsetDateTime.now();
        task.dueDateTime = OffsetDateTime.now().plusDays(3); // Due in 3 days
        
        // Log the task
        String logOutput = FlowLogger.logTask(task);
        
        // Verify the log output contains the due date
        assertTrue(logOutput.contains("Due Date:"));
        assertTrue(logOutput.contains(task.dueDateTime.toString()));
        assertFalse(logOutput.contains("OVERDUE")); // Should not be overdue
        
        System.out.println("Task with future due date:");
        System.out.println(logOutput);
    }
    
    @Test
    public void testLogOverdueTask() {
        // Create an overdue task
        WfTask task = new WfTask();
        task.id = UUID.randomUUID();
        task.instanceId = UUID.randomUUID();
        task.nodeId = "task_2";
        task.name = "Overdue Task";
        task.state = "OPEN";
        task.createdAt = OffsetDateTime.now().minusDays(5);
        task.dueDateTime = OffsetDateTime.now().minusDays(2); // Due 2 days ago
        
        // Log the task
        String logOutput = FlowLogger.logTask(task);
        
        // Verify the log output indicates the task is overdue
        assertTrue(logOutput.contains("Due Date:"));
        assertTrue(logOutput.contains(task.dueDateTime.toString()));
        assertTrue(logOutput.contains("OVERDUE")); // Should be marked as overdue
        
        System.out.println("Overdue task:");
        System.out.println(logOutput);
    }
    
    @Test
    public void testLogCompletedTask() {
        // Create a completed task
        WfTask task = new WfTask();
        task.id = UUID.randomUUID();
        task.instanceId = UUID.randomUUID();
        task.nodeId = "task_3";
        task.name = "Completed Task";
        task.state = "COMPLETED";
        task.createdAt = OffsetDateTime.now().minusDays(3);
        task.dueDateTime = OffsetDateTime.now().plusDays(1); // Was due in 1 day
        task.completedAt = OffsetDateTime.now().minusDays(1); // Completed 1 day ago
        
        // Log the task
        String logOutput = FlowLogger.logTask(task);
        
        // Verify the log output contains completion information
        assertTrue(logOutput.contains("Due Date:"));
        assertTrue(logOutput.contains("Completed At:"));
        assertTrue(logOutput.contains(task.completedAt.toString()));
        assertFalse(logOutput.contains("OVERDUE")); // Should not be marked as overdue since it's completed
        
        System.out.println("Completed task:");
        System.out.println(logOutput);
    }
    
    @Test
    public void testLogMultipleTasks() {
        // Create multiple tasks
        WfTask task1 = new WfTask();
        task1.id = UUID.randomUUID();
        task1.instanceId = UUID.randomUUID();
        task1.nodeId = "task_1";
        task1.name = "Task 1";
        task1.state = "OPEN";
        task1.createdAt = OffsetDateTime.now();
        task1.dueDateTime = OffsetDateTime.now().plusDays(3);
        
        WfTask task2 = new WfTask();
        task2.id = UUID.randomUUID();
        task2.instanceId = UUID.randomUUID();
        task2.nodeId = "task_2";
        task2.name = "Task 2";
        task2.state = "OPEN";
        task2.createdAt = OffsetDateTime.now().minusDays(5);
        task2.dueDateTime = OffsetDateTime.now().minusDays(2);
        
        List<WfTask> tasks = Arrays.asList(task1, task2);
        
        // Log the tasks
        String logOutput = FlowLogger.logTasks(tasks);
        
        // Verify the log output contains information about both tasks
        assertTrue(logOutput.contains("Tasks (2):"));
        assertTrue(logOutput.contains("Task 1"));
        assertTrue(logOutput.contains("Task 2"));
        
        System.out.println("Multiple tasks:");
        System.out.println(logOutput);
    }
}

// Made with Bob
