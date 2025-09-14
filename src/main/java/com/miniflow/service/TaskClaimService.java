package com.miniflow.service;


import com.miniflow.dto.TaskSummaryDTO;
import com.miniflow.persist.entity.WfTask;
import com.miniflow.persist.repo.WfTaskCandidateRepo;
import com.miniflow.persist.repo.WfTaskRepo;
import java.util.*;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskClaimService {

    private final WfTaskRepo taskRepo;
    private final WfTaskCandidateRepo candidateRepo;

    public TaskClaimService(WfTaskRepo taskRepo, WfTaskCandidateRepo candidateRepo) {
        this.taskRepo = taskRepo;
        this.candidateRepo = candidateRepo;
    }

    public Page<TaskSummaryDTO> listClaimables(String user, Collection<String> groups, Pageable pageable) {
        return taskRepo.findClaimables(user, sanitize(groups), pageable);
    }

    @Transactional
    public TaskSummaryDTO claim(UUID taskId, String user, Collection<String> groups) {
        // 1) Fast fail if task not open/unassigned
        WfTask t = taskRepo.findById(taskId)
                .orElseThrow(() -> new NoSuchElementException("Task not found: " + taskId));
        if (!"OPEN".equalsIgnoreCase(t.state))
            throw new IllegalStateException("Task not OPEN");
        if (t.assignee != null)
            throw new IllegalStateException("Task already assigned to: " + t.assignee);

        // 2) Authorization: must be candidate user or in candidate group
        long allowed = candidateRepo.countMembership(taskId, user, sanitize(groups));
        if (allowed == 0)
            throw new SecurityException("User '" + user + "' is not a candidate for this task");

        // 3) Atomic claim (handles race: returns 0 if someone else beat you)
        int updated = taskRepo.claimIfUnassigned(taskId, user);
        if (updated == 0)
            throw new IllegalStateException("Task was claimed by someone else");

        return taskRepo.findSummary(taskId).orElseThrow();
    }

    private static List<String> sanitize(Collection<String> in) {
        if (in == null) return List.of();
        return in.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();
    }
}
