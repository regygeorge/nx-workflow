// ---------------------------------------------------------------------------
// src/main/java/com/miniflow/core/DbBackedEngine.java
// ---------------------------------------------------------------------------
package com.miniflow.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miniflow.core.EngineModel.*;
import com.miniflow.persist.EnginePersistencePort;
import com.miniflow.persist.entity.WfInstance;
import com.miniflow.persist.entity.WfProcess;
import com.miniflow.persist.entity.WfTask;
import com.miniflow.persist.repo.WfInstanceRepo;
import com.miniflow.persist.repo.WfProcessRepo;
import com.miniflow.persist.repo.WfTaskRepo;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
public class DbBackedEngine {

    private final Map<String, ProcessDefinition> deployed = new ConcurrentHashMap<>();
    private final EnginePersistencePort db;
    private final WfInstanceRepo instanceRepo;
    private final WfTaskRepo taskRepo;
    private final RestTemplate rest = new RestTemplate(); // for HTTP system tasks


  // fields
  private final WfProcessRepo processRepo;

  // ctor
  public DbBackedEngine(EnginePersistencePort db,
                        WfInstanceRepo instanceRepo,
                        WfTaskRepo taskRepo,
                        WfProcessRepo processRepo) {
    this.db = db; this.instanceRepo = instanceRepo; this.taskRepo = taskRepo; this.processRepo = processRepo;
  }

  // keep the old method for convenience
  public void deploy(ProcessDefinition def){
    deploy(def, null);
  }

  // NEW: persist to wf_process
  public void deploy(ProcessDefinition def, String bpmnXml){
    Objects.requireNonNull(def);
    WfProcess p = new WfProcess();
    p.processId = def.id;
    p.name = (def.name == null || def.name.isBlank()) ? def.id : def.name;
    p.bpmnXml = bpmnXml;
    p.deployedAt = OffsetDateTime.now();
    processRepo.save(p);           // <- ensures FK parent row exists
    deployed.put(def.id, def);     // still keep in-memory cache
    FlowLogger.describe(def);
    }
    // Service task handlers registry (Java class callbacks)
    @FunctionalInterface
    public interface ServiceTaskHandler {

        void execute(ExecutionContext ctx) throws Exception;
    }

    public static final class ServiceRegistry {

        private final Map<String, ServiceTaskHandler> map = new ConcurrentHashMap<>();

        public void register(String type, ServiceTaskHandler h) {
            map.put(type, h);
        }

        public ServiceTaskHandler get(String type) {
            return map.get(type);
        }
    }
    private final ServiceRegistry registry = new ServiceRegistry();

    public void register(String taskType, ServiceTaskHandler handler) {
        registry.register(taskType, handler);
    }



    public Set<String> deployedProcessIds() {
        return deployed.keySet();
    }

    public static final class InstanceView {

        public final UUID id;
        public final String processId;
        public final String businessKey;
        public final boolean completed;
        public final Map<String, Object> variables;
        public final List<String> activeNodes;

        public InstanceView(UUID id, String processId, String businessKey, boolean completed, Map<String, Object> variables, List<String> activeNodes) {
            this.id = id;
            this.processId = processId;
            this.businessKey = businessKey;
            this.completed = completed;
            this.variables = variables;
            this.activeNodes = activeNodes;
        }

        public String tokenAt() {
            return activeNodes.isEmpty() ? "" : activeNodes.get(0);
        }
    }

    @Transactional
    public InstanceView start(String processId, Map<String, Object> vars) {
        return start(processId, null, vars);
    }
    
    @Transactional
    public InstanceView start(String processId, String businessKey, Map<String, Object> vars) {
        ProcessDefinition def = requireProcess(processId);
        UUID iid = db.createInstance(processId, businessKey, vars == null ? Map.of() : vars);
        db.createToken(iid, def.startId());
        runUntilWait(iid, def);
        FlowLogger.describe(def);
        return snapshot(iid);
    }

    /**
     * Sets the due date for a task
     * @param taskId The ID of the task
     * @param dueDateTime The due date and time
     */
    @Transactional
    public void setTaskDueDate(UUID taskId, OffsetDateTime dueDateTime) {
        WfTask task = taskRepo.findById(taskId).orElseThrow();
        task.dueDateTime = dueDateTime;
        taskRepo.save(task);
    }

    @Transactional
    public InstanceView completeUserTask(UUID taskId, Map<String, Object> updates) {
        WfTask t = taskRepo.findById(taskId).orElseThrow();
        UUID iid = t.instanceId;
        if (updates != null && !updates.isEmpty()) {
            db.updateVariables(iid, cur -> {
                if (cur == null) {
                    cur = new HashMap<>();
                
                }cur.putAll(updates);
                return cur;
            });
        
        }ProcessDefinition def = requireProcess(instanceRepo.findById(iid).orElseThrow().processId);
        Node userNode = def.getNode(t.nodeId);


      Map<String,Object> vars = loadVars(iid);
      List<SequenceFlow> outs = matchingOutgoings(vars, userNode);

      db.completeUserTask(taskId);

      if (outs.size() == 1) {
        db.createToken(iid, outs.get(0).to);
      } else {
        // implicit AND-fork
        for (SequenceFlow f : outs) db.createToken(iid, f.to);
      }
      runUntilWait(iid, def);

        return snapshot(iid);
    }

    public Optional<InstanceView> instance(UUID iid) {
        return instanceRepo.findById(iid).map(e -> snapshot(e.id));
    }

    @Transactional
    protected void runUntilWait(UUID iid, ProcessDefinition def) {
        Map<String, Object> vars = new HashMap<>(loadVars(iid));
        while (true) {
            List<EnginePersistencePort.TokenView> tokens = db.activeTokens(iid);
            if (tokens.isEmpty()) {
                break;
            
            }boolean progressed = false;
            for (var tv : tokens) {
                Node node = def.getNode(tv.nodeId());
                switch (node.type) {
                    case START -> {
                        SequenceFlow out = requireSingleOutgoing(node);
                        db.moveToken(tv.tokenId(), out.to);
                        progressed = true;
                    }
                    case SERVICE_TASK -> {
                        ServiceTask st = (ServiceTask) node; // 1) Java handler by type
                        ServiceTaskHandler h = registry.get(st.taskType);
                        if (h != null) {
                            try {
                                h.execute(new ExecutionContext(iid, vars, st.props));
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        } else { // 2) Built-in HTTP or Java class via props
                            executeBuiltIn(st.props, new ExecutionContext(iid, vars, st.props));
                        }
                        db.updateVariables(iid, cur -> {
                            if (cur == null) {
                                cur = new HashMap<>();
                            
                            }cur.putAll(vars);
                            return cur;
                        });
                        SequenceFlow out = chooseOutgoing(vars, node);
                        db.moveToken(tv.tokenId(), out.to);
                        progressed = true;
                    }
                    case USER_TASK -> {
                        db.createUserTask(iid, tv.tokenId(), node.id, node.name);
                        db.consumeToken(tv.tokenId());
                    }
                    case EXCLUSIVE_GATEWAY -> {
                        SequenceFlow out = chooseOutgoing(vars, node);
                        db.moveToken(tv.tokenId(), out.to);
                        progressed = true;
                    }
                    case PARALLEL_GATEWAY -> {
                        boolean fork = node.outgoing.size() > 1 && node.incoming.size() <= 1;
                        boolean join = node.incoming.size() > 1 && node.outgoing.size() == 1;
                        if (fork) {
                            db.consumeToken(tv.tokenId());
                            for (SequenceFlow f : node.outgoing) {
                                db.createToken(iid, f.to);
                            
                            }progressed = true;
                        } else if (join) {
                            int arrived = db.incrementJoin(iid, node.id, node.incoming.size());
                            if (arrived >= node.incoming.size()) {
                                db.resetJoin(iid, node.id);
                                SequenceFlow out = requireSingleOutgoing(node);
                                db.moveToken(tv.tokenId(), out.to);
                                progressed = true;
                            } else {
                                db.consumeToken(tv.tokenId());
                            }
                        } else {
                            SequenceFlow out = chooseOutgoing(vars, node);
                            db.moveToken(tv.tokenId(), out.to);
                            progressed = true;
                        }
                    }
                    case END -> {
                        db.consumeToken(tv.tokenId());
                        progressed = true;
                    }
                }
            }
            if (!progressed) {
                break;
        
            }}
        boolean finished = db.activeTokens(iid).isEmpty() && !db.hasOpenTasks(iid);
        if (finished) {
            db.markInstanceCompleted(iid);
    
        }}

    private void executeBuiltIn(Map<String, String> props, ExecutionContext ctx) {
        String type = props.getOrDefault("type", "");
        if ("http".equalsIgnoreCase(type) || props.containsKey("http.url")) {
            callHttp(props, ctx);
        } else if (props.containsKey("java.class")) {
            callJava(props.get("java.class"), ctx);
        } // else: no-op
    }

    private void callHttp(Map<String, String> props, ExecutionContext ctx) {
        String url = props.getOrDefault("http.url", "");
        if (url.isBlank()) {
            return;
        
        }String method = props.getOrDefault("http.method", "POST");
        String bodyTpl = props.getOrDefault("http.body", "{}");
        try {
            Object body = new ObjectMapper().readValue(bodyTpl, Map.class); // can include placeholders if you extend
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Object> entity = new HttpEntity<>(body, headers);
            rest.exchange(url, HttpMethod.valueOf(method.toUpperCase()), entity, String.class);
        } catch (Exception e) {
            throw new RuntimeException("HTTP serviceTask failed: " + e.getMessage(), e);
        }
    }

    private void callJava(String className, ExecutionContext ctx) {
        try {
            Class<?> cl = Class.forName(className);
            Object o = cl.getDeclaredConstructor().newInstance();
            if (o instanceof JavaDelegate jd) {
                jd.execute(ctx);
            } else {
                throw new IllegalStateException("Class does not implement JavaDelegate: " + className);
            }
        } catch (Exception e) {
            throw new RuntimeException("Java class serviceTask failed: " + e.getMessage(), e);
        }
    }

    public interface JavaDelegate {

        void execute(ExecutionContext ctx) throws Exception;
    }

    private ProcessDefinition requireProcess(String processId) {
        ProcessDefinition d = deployed.get(processId);
        if (d == null) {
            throw new IllegalArgumentException("Process not deployed: " + processId);
        
        }return d;
    }

  // src/main/java/com/miniflow/core/DbBackedEngine.java
  private Map<String,Object> loadVars(UUID iid){
    var e = instanceRepo.findById(iid).orElseThrow();
    return e.variables == null ? new HashMap<>() : new HashMap<>(e.variables);
  }


  private static EngineModel.SequenceFlow requireSingleOutgoing(Node node) {
        if (node.outgoing.size() != 1) {
            throw new IllegalStateException("Expected exactly 1 outgoing from " + node.id);
        
        }return node.outgoing.get(0);
    }

    private static EngineModel.SequenceFlow chooseOutgoing(Map<String, Object> vars, Node node) {
        if (node.outgoing.isEmpty()) {
            throw new IllegalStateException("No outgoing from " + node.id);
        
        }for (EngineModel.SequenceFlow f : node.outgoing) {
            if (f.condition == null || f.condition.eval(vars)) {
                return f;
        
            }}
        throw new IllegalStateException("No matching condition from " + node.id);
    }

    public static final class ExecutionContext {

        private final UUID instanceId;
        private final Map<String, Object> vars;
        private final Map<String, String> props;

        public ExecutionContext(UUID instanceId, Map<String, Object> vars, Map<String, String> props) {
            this.instanceId = instanceId;
            this.vars = vars;
            this.props = props;
        }

        public UUID instanceId() {
            return instanceId;
        }

        public Map<String, Object> vars() {
            return vars;
        }

        public Map<String, String> props() {
            return props;
        }
    }

    public InstanceView snapshot(UUID iid) {
        WfInstance e = instanceRepo.findById(iid).orElseThrow();
        Map<String, Object> v = loadVars(iid);
        List<String> active = db.activeTokens(iid).stream().map(EnginePersistencePort.TokenView::nodeId).collect(Collectors.toList());
        boolean completed = "COMPLETED".equalsIgnoreCase(e.status);
        return new InstanceView(e.id, e.processId, e.businessKey, completed, v, active);
    }

  // DbBackedEngine.java
  private List<EngineModel.SequenceFlow> matchingOutgoings(Map<String,Object> vars, EngineModel.Node node) {
    List<EngineModel.SequenceFlow> outs = new ArrayList<>();
    for (EngineModel.SequenceFlow f : node.outgoing) {
      if (f.condition == null || f.condition.eval(vars)) outs.add(f);
    }
    if (outs.isEmpty()) throw new IllegalStateException("No matching condition from " + node.id);
    return outs;
  }

}
