package com.miniflow.core;

import com.miniflow.core.EngineModel.*;
import com.miniflow.persist.entity.WfTask;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class FlowLogger {
    private FlowLogger(){}

    public static String describe(ProcessDefinition def) {
        StringBuilder sb = new StringBuilder(256);
        sb.append("Process ").append(def.id).append(" (").append(def.name).append(")").append('\n');
        for (Node n : def.getNodes()) {
            sb.append("• ").append(n.type).append(" ").append(n.id)
                    .append(" [").append(n.name).append("]").append('\n');
            for (SequenceFlow f : n.outgoing) {
                sb.append("    └─(").append(f.condition).append(")─▶ ")
                        .append(f.to).append("  [flowId=").append(f.id).append("]").append('\n');
            }
        }
        return sb.toString();
    }
    
    /**
     * Log task information including due dates
     * @param task The task to log
     * @return A string representation of the task
     */
    public static String logTask(WfTask task) {
        StringBuilder sb = new StringBuilder(128);
        sb.append("Task: ").append(task.id).append('\n');
        sb.append("  Name: ").append(task.name).append('\n');
        sb.append("  State: ").append(task.state).append('\n');
        sb.append("  Node ID: ").append(task.nodeId).append('\n');
        sb.append("  Instance ID: ").append(task.instanceId).append('\n');
        sb.append("  Created At: ").append(formatDateTime(task.createdAt)).append('\n');
        
        if (task.dueDateTime != null) {
            sb.append("  Due Date: ").append(formatDateTime(task.dueDateTime)).append('\n');
            
            // Calculate if the task is overdue
            OffsetDateTime now = OffsetDateTime.now();
            if (task.state.equals("OPEN") && task.dueDateTime.isBefore(now)) {
                sb.append("  Status: OVERDUE\n");
            }
        } else {
            sb.append("  Due Date: Not set\n");
        }
        
        if (task.completedAt != null) {
            sb.append("  Completed At: ").append(formatDateTime(task.completedAt)).append('\n');
        }
        
        return sb.toString();
    }
    
    /**
     * Log multiple tasks
     * @param tasks The list of tasks to log
     * @return A string representation of all tasks
     */
    public static String logTasks(List<WfTask> tasks) {
        StringBuilder sb = new StringBuilder(256);
        sb.append("Tasks (").append(tasks.size()).append("):\n");
        
        for (WfTask task : tasks) {
            sb.append(logTask(task)).append('\n');
        }
        
        return sb.toString();
    }
    
    /**
     * Format a datetime for logging
     * @param dateTime The datetime to format
     * @return A formatted string representation of the datetime
     */
    private static String formatDateTime(OffsetDateTime dateTime) {
        if (dateTime == null) return "null";
        return dateTime.format(DateTimeFormatter.ISO_DATE_TIME);
    }
}