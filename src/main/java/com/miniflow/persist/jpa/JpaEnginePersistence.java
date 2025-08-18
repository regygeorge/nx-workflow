// ---------------------------------------------------------------------------
// src/main/java/com/miniflow/persist/jpa/JpaEnginePersistence.java
// ---------------------------------------------------------------------------
package com.miniflow.persist.jpa;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miniflow.persist.EnginePersistencePort;
import com.miniflow.persist.entity.WfInstance;
import com.miniflow.persist.entity.WfJoin;
import com.miniflow.persist.entity.WfTask;
import com.miniflow.persist.entity.WfToken;
import com.miniflow.persist.repo.WfInstanceRepo;
import com.miniflow.persist.repo.WfJoinRepo;
import com.miniflow.persist.repo.WfTaskRepo;
import com.miniflow.persist.repo.WfTokenRepo;
import com.miniflow.persist.repo.WfVariableRepo;

import jakarta.transaction.Transactional;

@Component
@Transactional
public class JpaEnginePersistence implements EnginePersistencePort {

    private final WfInstanceRepo instanceRepo;
    private final WfTokenRepo tokenRepo;
    private final WfTaskRepo taskRepo;
    private final WfJoinRepo joinRepo;
    private final WfVariableRepo varRepo;
    private final ObjectMapper om = new ObjectMapper();

    public JpaEnginePersistence(WfInstanceRepo i, WfTokenRepo t, WfTaskRepo tr, WfJoinRepo j, WfVariableRepo v) {
        this.instanceRepo = i;
        this.tokenRepo = t;
        this.taskRepo = tr;
        this.joinRepo = j;
        this.varRepo = v;
    }
// src/main/java/com/miniflow/persist/jpa/JpaEnginePersistence.java
// ...imports unchanged, except remove ObjectMapper imports for variables JSON...

    @Override
    public UUID createInstance(String processId, Map<String, Object> vars) {
        UUID id = UUID.randomUUID();
        WfInstance e = new WfInstance();
        e.id = id;
        e.processId = processId;
        e.status = "RUNNING";
        e.variables = new HashMap<>(vars == null ? Map.of() : vars); // <— no toJson
        e.createdAt = now();
        e.updatedAt = now();
        instanceRepo.save(e);
        return id;
    }

    @Override
    public void markInstanceCompleted(UUID instanceId) {

    }

    @Override
    public void updateVariables(UUID instanceId, java.util.function.UnaryOperator<Map<String,Object>> mutator) {
        WfInstance e = instanceRepo.findById(instanceId).orElseThrow();
        Map<String,Object> cur = e.variables == null ? new HashMap<>() : new HashMap<>(e.variables);
        Map<String,Object> next = mutator.apply(cur);
        e.variables = next; // <— no toJson
        e.updatedAt = now();
        instanceRepo.save(e);
    }

    @Override
    public UUID createToken(UUID instanceId, String nodeId) {
        UUID id = UUID.randomUUID();
        WfToken t = new WfToken();
        t.id = id;
        t.instanceId = instanceId;
        t.nodeId = nodeId;
        t.active = true;
        t.createdAt = now();
        t.updatedAt = now();
        tokenRepo.save(t);
        return id;
    }

    @Override
    public void moveToken(UUID tokenId, String nodeId) {
        WfToken t = tokenRepo.findById(tokenId).orElseThrow();
        t.nodeId = nodeId;
        t.updatedAt = now();
        tokenRepo.save(t);
    }

    @Override
    public void consumeToken(UUID tokenId) {
        WfToken t = tokenRepo.findById(tokenId).orElseThrow();
        t.active = false;
        t.updatedAt = now();
        tokenRepo.save(t);
    }

    @Override
    public List<TokenView> activeTokens(UUID instanceId) {
        List<WfToken> list = tokenRepo.findByInstanceIdAndActive(instanceId, true);
        List<TokenView> out = new ArrayList<>();
        for (WfToken t : list) {
            out.add(new TokenView(t.id, t.nodeId));
        
        }return out;
    }

    @Override
    public int incrementJoin(UUID instanceId, String nodeId, int expectedIncoming) {
        WfJoin.PK pk = new WfJoin.PK(instanceId, nodeId);
        WfJoin j = joinRepo.findById(pk).orElse(null);
        if (j == null) {
            j = new WfJoin();
            j.instanceId = instanceId;
            j.nodeId = nodeId;
            j.arrivals = 1;
        } else {
            j.arrivals += 1;
        }
        joinRepo.save(j);
        return j.arrivals;
    }

    @Override
    public void resetJoin(UUID instanceId, String nodeId) {
        joinRepo.deleteById(new WfJoin.PK(instanceId, nodeId));
    }

    @Override
    public UUID createUserTask(UUID instanceId, UUID tokenId, String nodeId, String name) {
        UUID id = UUID.randomUUID();
        WfTask t = new WfTask();
        t.id = id;
        t.instanceId = instanceId;
        t.tokenId = tokenId;
        t.nodeId = nodeId;
        t.name = name;
        t.state = "OPEN";
        t.createdAt = now();
        taskRepo.save(t);
        return id;
    }

    @Override
    public void completeUserTask(UUID taskId) {
        WfTask t = taskRepo.findById(taskId).orElseThrow();
        t.state = "COMPLETED";
        t.completedAt = now();
        taskRepo.save(t);
    }

    @Override
    public boolean hasOpenTasks(UUID instanceId) {
        return !taskRepo.findByInstanceIdAndState(instanceId, "OPEN").isEmpty();
    }

    private static OffsetDateTime now() {
        return OffsetDateTime.now();
    }

    private String toJson(Map<String, Object> m) {
        try {
            return new ObjectMapper().writeValueAsString(m == null ? Map.of() : m);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> fromJson(String s) {
        try {
            return s == null || s.isBlank() ? new HashMap<>() : new ObjectMapper().readValue(s, Map.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
