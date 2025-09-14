// ---------------------------------------------------------------------------
// src/main/java/com/miniflow/rest/ProcessController.java
// ---------------------------------------------------------------------------
package com.miniflow.rest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.miniflow.dto.TaskSummaryDTO;
import com.miniflow.dto.TaskSummaryView;
import com.miniflow.service.TaskQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.miniflow.core.DbBackedEngine;
import com.miniflow.core.EngineModel.Node;
import com.miniflow.core.EngineModel.NodeType;
import com.miniflow.core.EngineModel.ProcessDefinition;
import com.miniflow.core.EngineModel.ServiceTask;
import com.miniflow.parser.SimpleBpmnParser;
import com.miniflow.persist.entity.WfTask;
import com.miniflow.persist.repo.WfTaskRepo;

@RestController
@RequestMapping("/api")
public class ProcessController {


    private final TaskQueryService svc;
    private final DbBackedEngine engine;
    private final WfTaskRepo taskRepo;

    public ProcessController(TaskQueryService svc, DbBackedEngine engine, WfTaskRepo taskRepo) {
        this.svc = svc;
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
        return new ApiDtos.StartResponse(v.id.toString(), v.processId, v.businessKey, v.tokenAt(), v.completed, v.variables);
    }
    
    @PostMapping("/processes/{processId}/start-with-business-key")
    public ApiDtos.StartResponse startWithBusinessKey(
            @PathVariable("processId") String processId,
            @RequestParam("businessKey") String businessKey,
            @RequestBody(required = false) Map<String, Object> vars) {
        DbBackedEngine.InstanceView v = engine.start(processId, businessKey, vars == null ? Map.of() : vars);
        return new ApiDtos.StartResponse(v.id.toString(), v.processId, v.businessKey, v.tokenAt(), v.completed, v.variables);
    }

    @GetMapping("/instances/{instanceId}")
    public ApiDtos.InstanceView instance(@PathVariable("instanceId") String instanceId) {
        DbBackedEngine.InstanceView v = engine.instance(UUID.fromString(instanceId)).orElseThrow();
        return new ApiDtos.InstanceView(v.id.toString(), v.processId, v.businessKey, v.tokenAt(), v.completed, v.variables);
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
        return new ApiDtos.InstanceView(v.id.toString(), v.processId, v.businessKey, v.tokenAt(), v.completed, v.variables);
    }
    
    /**
     * Set the due date for a task
     * @param taskId The ID of the task
     * @param dueDateRequest The request containing the due date
     */
    @PostMapping("/tasks/{taskId}/due-date")
    public void setTaskDueDate(@PathVariable("taskId") String taskId, @RequestBody Map<String, String> dueDateRequest) {
        if (!dueDateRequest.containsKey("dueDateTime")) {
            throw new IllegalArgumentException("dueDateTime is required");
        }
        
        String dueDateTimeStr = dueDateRequest.get("dueDateTime");
        OffsetDateTime dueDateTime = OffsetDateTime.parse(dueDateTimeStr);
        engine.setTaskDueDate(UUID.fromString(taskId), dueDateTime);
    }

    @GetMapping("/tasks")
    public List<ApiDtos.TaskView> listTasks(
            @RequestParam(required=false) String assignee,
            @RequestParam(required=false) String candidateUser,
            @RequestParam(required=false) String groups // comma list: "doctors,nurses"
    ){
        if (assignee != null && !assignee.isBlank()) {
            return taskRepo.findByAssigneeAndState(assignee, "OPEN")
                    .stream().map(ApiDtos::from).toList();
        }
        if (candidateUser != null) {
            List<String> g = (groups==null||groups.isBlank()) ? List.of()
                    : Arrays.stream(groups.split(",")).map(String::trim).toList();
            return taskRepo.findOpenForUserOrGroups(candidateUser, (String[]) g.toArray())
                    .stream().map(ApiDtos::from).toList();
        }
        // default: all OPEN tasks
        return taskRepo.findAll().stream()
                .filter(t -> "OPEN".equals(t.state))
                .map(ApiDtos::from).toList();
    }


    @GetMapping("/tasks/assignee-view")
    public Page<TaskSummaryView> listByAssignee(
            @RequestParam String assignee,
            @RequestParam(required = false) String state,
            @PageableDefault(size = 20, sort = "dueDateTime", direction = Sort.Direction.ASC)
            Pageable pageable) {
        return taskRepo.findByAssignee(assignee, state, pageable);
    }

    @GetMapping("/tasks/assignee")
    public Page<TaskSummaryDTO> listByAssigneeAndGroup(
            @RequestParam String assignee,
            @RequestParam(required = false) String state,
            @PageableDefault(size = 20, sort = "dueDateTime", direction = Sort.Direction.ASC)
            Pageable pageable) {
        return taskRepo.findTaskSummariesByAssignee(assignee, state, pageable);
    }

}
