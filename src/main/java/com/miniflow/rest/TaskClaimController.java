package com.miniflow.rest;


import java.util.*;
import java.util.stream.Stream;

import com.miniflow.dto.TaskSummaryDTO;
import com.miniflow.service.Caller;
import com.miniflow.service.TaskClaimService;
import org.springframework.data.domain.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskClaimController {

    private final TaskClaimService svc;
    public TaskClaimController(TaskClaimService svc) {
        this.svc = svc;
    }

    @GetMapping("/claimable")
    public Page<TaskSummaryDTO> claimables(@AuthenticationPrincipal Jwt jwt, Pageable pageable) {
        var c = Caller.fromJwt(jwt);
        return svc.listClaimables(c.user(), c.groups(), pageable);
    }

    @PostMapping("/{taskId}/claim")
    public TaskSummaryDTO claim(@PathVariable UUID taskId, @AuthenticationPrincipal Jwt jwt) {
        var c = Caller.fromJwt(jwt);
        return svc.claim(taskId, c.user(), c.groups());
    }

    public static class ClaimRequest {
        public String user;
        public List<String> groups;
    }

    private static List<String> parseCsv(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        return Stream.of(csv.split("[,]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();
    }
}

