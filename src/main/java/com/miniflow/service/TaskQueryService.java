package com.miniflow.service;



import com.miniflow.persist.entity.WfTask;
import com.miniflow.persist.repo.WfTaskRepo;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskQueryService {
    private final WfTaskRepo taskRepo;

    public TaskQueryService(WfTaskRepo taskRepo) {
        this.taskRepo = taskRepo;
    }

    public List<WfTask> byAssignee(String assignee, String state, Pageable pageable) {
        // Default to OPEN if not provided
        String effectiveStatus = (state == null || state.isBlank()) ? "OPEN" : state;
        return taskRepo.findByAssigneeAndState(assignee, effectiveStatus);
    }
}
