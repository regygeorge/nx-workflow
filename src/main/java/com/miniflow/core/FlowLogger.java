package com.miniflow.core;



import com.miniflow.core.EngineModel.*;

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
}