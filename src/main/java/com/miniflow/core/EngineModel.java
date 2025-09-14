
// ---------------------------------------------------------------------------
// src/main/java/com/miniflow/core/EngineModel.java   (definition-only model)
// ---------------------------------------------------------------------------
package com.miniflow.core;

import java.util.*;

public class EngineModel {
  public enum NodeType { START, END, USER_TASK, SERVICE_TASK, EXCLUSIVE_GATEWAY, PARALLEL_GATEWAY }

  public static class ProcessDefinition {
    public final String id; public final String name;
    private final Map<String, Node> nodes = new LinkedHashMap<>();
    private String startId;
    public ProcessDefinition(String id, String name){ this.id=id; this.name=(name==null||name.isBlank())?id:name; }
    public void addNode(Node n){ nodes.put(n.id, n); if (n.type==NodeType.START) startId = n.id; }
    public Node getNode(String id){ return nodes.get(id); }
    public Collection<Node> getNodes(){ return nodes.values(); }
    public String startId(){ if (startId==null) throw new IllegalStateException("No startEvent"); return startId; }
  }

  public static abstract class Node {
    public final String id, name; public final NodeType type;
    public final List<SequenceFlow> outgoing = new ArrayList<>();
    public final List<SequenceFlow> incoming = new ArrayList<>();
    public final Map<String, String> props = new HashMap<>();

    protected Node(String id, String name, NodeType t){ this.id=id; this.name=(name==null||name.isBlank()?id:name); this.type=t; }
  }
  public static class StartEvent extends Node { public StartEvent(String id, String name){ super(id,name,NodeType.START);} }
  public static class EndEvent   extends Node { public EndEvent(String id, String name){ super(id,name,NodeType.END);} }
  public static class UserTask   extends Node { public UserTask(String id, String name){ super(id,name,NodeType.USER_TASK);} }
  public static class ServiceTask extends Node {
    public final String taskType;                // logical type (defaults to id)
    public final Map<String,String> props;       // extension properties (e.g., http.url, java.class)
    public ServiceTask(String id, String name, String taskType){ this(id,name,taskType, new HashMap<>()); }
    public ServiceTask(String id, String name, String taskType, Map<String,String> props){ super(id,name,NodeType.SERVICE_TASK); this.taskType=(taskType==null||taskType.isBlank()?id:taskType); this.props=props==null? new HashMap<>() : props; }
  }
  public static class ExclusiveGateway extends Node { public ExclusiveGateway(String id, String name){ super(id,name,NodeType.EXCLUSIVE_GATEWAY);} }
  public static class ParallelGateway  extends Node { public ParallelGateway(String id, String name){ super(id,name,NodeType.PARALLEL_GATEWAY);} }

  @FunctionalInterface public interface Condition { boolean eval(Map<String,Object> vars); }
  public static class SequenceFlow { public final String id, from, to;
    public final Condition condition;
    public SequenceFlow(String id,String from,String to,Condition c){ this.id=id; this.from=from; this.to=to; this.condition=c; } }

  public static class Builder {
    private final ProcessDefinition def; private final Map<String,Node> nodes = new HashMap<>();
    public Builder(String id, String name){ def = new ProcessDefinition(id,name); }
    public Builder start(String id){ add(new StartEvent(id, "Start")); return this; }
    public Builder end(String id){ add(new EndEvent(id, "End")); return this; }
    public Builder userTask(String id, String name){ add(new UserTask(id,name)); return this; }
    public Builder serviceTask(String id, String name, String type){ add(new ServiceTask(id,name,type)); return this; }
    public Builder serviceTask(String id, String name, String type, Map<String,String> props){ add(new ServiceTask(id,name,type, props)); return this; }
    public Builder exclusiveGateway(String id, String name){ add(new ExclusiveGateway(id,name)); return this; }
    public Builder parallelGateway(String id, String name){ add(new ParallelGateway(id,name)); return this; }
    private void add(Node n){ nodes.put(n.id,n); def.addNode(n); }
    public Builder flow(String from, String to){ return flow(from,to, vars->true); }
    public Builder flow(String from, String to, Condition c){ Node f=nodes.get(from), t=nodes.get(to); if(f==null||t==null) throw new IllegalArgumentException("Unknown nodes"); SequenceFlow sf=new SequenceFlow(UUID.randomUUID().toString(),from,to,c); f.outgoing.add(sf); t.incoming.add(sf); return this; }
    public ProcessDefinition build(){ def.startId(); return def; }
  }

  public static class Conditions { public static Condition equalsTo(String key,Object v){ return vars->java.util.Objects.equals(vars.get(key), v); } public static Condition alwaysTrue(){ return vars->true; } }
}
