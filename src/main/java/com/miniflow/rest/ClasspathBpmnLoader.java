// ---------------------------------------------------------------------------
// src/main/java/com/miniflow/rest/ClasspathBpmnLoader.java
// ---------------------------------------------------------------------------
package com.miniflow.rest;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import com.miniflow.core.DbBackedEngine;
import com.miniflow.core.EngineModel.*;
import com.miniflow.core.EngineModel.Node;
import com.miniflow.core.EngineModel.NodeType;
import com.miniflow.core.EngineModel.ProcessDefinition;
import com.miniflow.core.EngineModel.ServiceTask;
import com.miniflow.parser.SimpleBpmnParser;

@Component
public class ClasspathBpmnLoader implements CommandLineRunner {

    private final DbBackedEngine engine;

    public ClasspathBpmnLoader(DbBackedEngine engine) {
        this.engine = engine;
    }

    @Override
    public void run(String... args) throws Exception {
        PathMatchingResourcePatternResolver r = new PathMatchingResourcePatternResolver();
        Resource[] resources = r.getResources("classpath*:**/*.bpmn");
        for (Resource res : resources) {
            try (InputStream in = res.getInputStream()) {
                byte[] bytes = in.readAllBytes();
                String xml = new String(bytes, StandardCharsets.UTF_8);
                ProcessDefinition def = SimpleBpmnParser.parse(new java.io.ByteArrayInputStream(bytes));
                engine.deploy(def, xml); // <- persist wf_process row
                for (Node n : def.getNodes())
                    if (n.type==NodeType.SERVICE_TASK){
                        String t=((ServiceTask)n).taskType;
                        engine.register(t, ctx -> { /* no-op */});
                    }
                System.out.println("[MiniFlow] Deployed BPMN: "+res.getFilename()+" -> "+def.id);
            }
        }
    }
}
