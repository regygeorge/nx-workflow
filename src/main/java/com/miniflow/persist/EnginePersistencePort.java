// ---------------------------------------------------------------------------
// src/main/java/com/miniflow/persist/EnginePersistencePort.java
// ---------------------------------------------------------------------------
package com.miniflow.persist;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.UnaryOperator;

public interface EnginePersistencePort {

    UUID createInstance(String processId, Map<String, Object> vars);

    void markInstanceCompleted(UUID instanceId);

    void updateVariables(UUID instanceId, UnaryOperator<Map<String, Object>> mutator);

    UUID createToken(UUID instanceId, String nodeId);

    void moveToken(UUID tokenId, String nodeId);

    void consumeToken(UUID tokenId);

    List<TokenView> activeTokens(UUID instanceId);

    int incrementJoin(UUID instanceId, String nodeId, int expectedIncoming);

    void resetJoin(UUID instanceId, String nodeId);

    UUID createUserTask(UUID instanceId, UUID tokenId, String nodeId, String name);

    void completeUserTask(UUID taskId);

    boolean hasOpenTasks(UUID instanceId);

    record TokenView(UUID tokenId, String nodeId) {

    }
}
