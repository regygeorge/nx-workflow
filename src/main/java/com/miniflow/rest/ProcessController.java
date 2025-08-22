// ---------------------------------------------------------------------------
// src/main/java/com/miniflow/rest/ProcessController.java
// ---------------------------------------------------------------------------
package com.miniflow.rest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.miniflow.core.DbBackedEngine;
import com.miniflow.core.EngineModel.*;
import com.miniflow.core.EngineModel.Node;
import com.miniflow.core.EngineModel.NodeType;
import com.miniflow.core.EngineModel.ProcessDefinition;
import com.miniflow.core.EngineModel.ServiceTask;
import com.miniflow.parser.SimpleBpmnParser;
import com.miniflow.persist.entity.WfTask;
import com.miniflow.persist.repo.WfTaskRepo;
import java.nio.charset.StandardCharsets;
@RestController
@RequestMapping("/api")
public class ProcessController {

    private final DbBackedEngine engine;
    private final WfTaskRepo taskRepo;

    public ProcessController(DbBackedEngine engine, WfTaskRepo taskRepo) {
        this.engine = engine;
        this.taskRepo = taskRepo;
    }



    // ...
    @PostMapping(value="/deploy/bpmn", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiDtos.DeployResponse deploy(@RequestPart("file") MultipartFile file) throws IOException {
        String xml = new String(file.getBytes(), StandardCharsets.UTF_8);
        ProcessDefinition def = SimpleBpmnParser.parse(new java.io.ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        engine.deploy(def, xml); // <- persist wf_process row
        for (Node n : def.getNodes())
            if (n.type==NodeType.SERVICE_TASK){
                String t=((ServiceTask)n).taskType;
                engine.register(t, ctx -> { /* no-op default */});
            }
        return new ApiDtos.DeployResponse(def.id);
    }


    @GetMapping("/processes")
    public Set<String> processes() {
        return engine.deployedProcessIds();
    }

    @PostMapping("/processes/{processId}/start")
    public ApiDtos.StartResponse start(@PathVariable("processId") String processId, @RequestBody(required = false) Map<String, Object> vars) {
        DbBackedEngine.InstanceView v = engine.start(processId, vars == null ? Map.of() : vars);
        return new ApiDtos.StartResponse(v.id.toString(), v.processId, v.tokenAt(), v.completed, v.variables);
    }

    @GetMapping("/instances/{instanceId}")
    public ApiDtos.InstanceView instance(@PathVariable("instanceId") String instanceId) {
        DbBackedEngine.InstanceView v = engine.instance(UUID.fromString(instanceId)).orElseThrow();
        return new ApiDtos.InstanceView(v.id.toString(), v.processId, v.tokenAt(), v.completed, v.variables);
    }

    @GetMapping("/instances/{instanceId}/tasks")
    public List<ApiDtos.TaskView> tasks(@PathVariable("instanceId") String instanceId) {
        UUID iid = UUID.fromString(instanceId);
        List<WfTask> open = taskRepo.findByInstanceIdAndState(iid, "OPEN");
        return open.stream().map(t -> new ApiDtos.TaskView(
            t.id.toString(),
            t.instanceId.toString(),
            t.nodeId,
            t.name,
            t.dueDateTime != null ? t.dueDateTime.toString() : null
        )).collect(Collectors.toList());
    }

    @PostMapping("/tasks/{taskId}/complete")
    public ApiDtos.InstanceView complete(@PathVariable("taskId") String taskId, @RequestBody(required = false) Map<String, Object> updates) {
        DbBackedEngine.InstanceView v = engine.completeUserTask(UUID.fromString(taskId), updates == null ? Map.of() : updates);
        return new ApiDtos.InstanceView(v.id.toString(), v.processId, v.tokenAt(), v.completed, v.variables);
    }
}
