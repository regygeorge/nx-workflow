package com.miniflow.persist.repo;

import com.miniflow.dto.TaskSummaryDTO;
import com.miniflow.dto.TaskSummaryView;
import com.miniflow.persist.entity.WfTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


import org.springframework.stereotype.Repository;

@Repository
public interface WfTaskRepo extends JpaRepository<WfTask, UUID> {

    // -------- Reads (derived) --------
    List<WfTask> findByAssigneeAndState(String assignee, String state);
    List<WfTask> findByInstanceIdAndState(UUID instanceId, String state);
    Optional<WfTask> findByIdAndState(UUID id, String state);
    List<WfTask> findByInstanceId(UUID instanceId);

    // -------- Updates (native SQL) --------
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
      update wf_task
         set assignee   = :assignee,
             state      = 'ASSIGNED',
             updated_at = now()
       where id    = :taskId
         and state = 'OPEN'
      """, nativeQuery = true)
    int claim(@Param("taskId") UUID taskId, @Param("assignee") String assignee);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
      update wf_task
         set assignee   = null,
             state      = 'OPEN',
             updated_at = now()
       where id    = :taskId
         and state = 'ASSIGNED'
      """, nativeQuery = true)
    int unclaim(@Param("taskId") UUID taskId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
      update wf_task
         set state      = 'COMPLETED',
             updated_at = now()
       where id = :taskId
         and state in ('OPEN','ASSIGNED')
      """, nativeQuery = true)
    int markCompleted(@Param("taskId") UUID taskId);

    // Optional: visible OPEN tasks by user or groups (make sure you mapped wf_task_candidate)
    @Query(value = """
      select distinct t.*
        from wf_task t
        left join wf_task_candidate c on c.task_id = t.id
       where t.state = 'OPEN'
         and (
               t.assignee = :user
            or (c.type = 'U' and c.candidate = :user)
            or (c.type = 'G' and c.candidate = any(:groups))
         )
      """, nativeQuery = true)
    List<WfTask> findOpenForUserOrGroups(@Param("user") String user,
                                         @Param("groups") String[] groups);



    @Query("""
        select
          t.id          as taskId,
          t.instanceId  as instanceId,
          i.processId   as processId,
          p.name        as processName,
          t.nodeId      as nodeId,
          t.name        as stepName,
          t.assignee    as assignee,
          t.createdAt   as createdAt,
          t.dueDateTime as dueDateTime,
          t.completedAt as completedAt,
          t.state       as state,
          i.businessKey as businessKey,
          i.variables   as variables,
          t.formKey     as formKey
        from WfTask t, WfInstance i, WfProcess p
        where t.instanceId = i.id
          and i.processId  = p.processId
          and (:assignee is null or lower(t.assignee) = lower(:assignee))
          and (:state    is null or t.state = :state)
        """)
    Page<TaskSummaryView> findByAssignee(
            @Param("assignee") String assignee,
            @Param("state") String state,
            Pageable pageable
    );


    @Query("""
        select new com.miniflow.dto.TaskSummaryDTO(
            t.id, t.instanceId,
            i.processId, p.name,
            t.nodeId, t.name,
            t.assignee, t.createdAt, t.dueDateTime, t.completedAt,
            t.state,
            i.businessKey, i.variables,
            t.formKey
        )
        from WfTask t, WfInstance i, WfProcess p
        where t.instanceId = i.id
          and i.processId  = p.processId
          and (:assignee is null or lower(t.assignee) = lower(:assignee))
          and (:state    is null or t.state = :state)
        """)
    Page<TaskSummaryDTO> findTaskSummariesByAssignee(
            @Param("assignee") String assignee,
            @Param("state") String state,
            Pageable pageable
    );






        // List tasks you can claim (OPEN + unassigned + user in candidates)
        @Query("""
    select new com.miniflow.dto.TaskSummaryDTO(
      t.id, t.instanceId,
      i.processId, p.name,
      t.nodeId, t.name,
      t.assignee, t.createdAt, t.dueDateTime, t.completedAt,
      t.state, i.businessKey, i.variables,
      t.formKey
    )
    from WfTask t, WfInstance i, WfProcess p
    where t.instanceId = i.id
      and i.processId  = p.processId
      and t.state = 'OPEN'
      and t.assignee is null
      and exists (
         select 1 from WfTaskCandidate c
         where c.taskId = t.id
           and (
                 (c.type = 'U' and c.candidate = :user)
              or (c.type = 'G' and c.candidate in :groups)
           )
      )
  """)
        Page<TaskSummaryDTO> findClaimables(@Param("user") String user,
                                            @Param("groups") Collection<String> groups,
                                            Pageable pageable);

        // Atomic claim (no subselects here → race-safe against double-claim)
        @Modifying
        @Query("""
    update WfTask t
       set t.assignee = :user
     where t.id = :taskId
       and t.state = 'OPEN'
       and t.assignee is null
  """)
        int claimIfUnassigned(@Param("taskId") UUID taskId, @Param("user") String user);

        // For returning a summary after claim
        @Query("""
    select new com.miniflow.dto.TaskSummaryDTO(
      t.id, t.instanceId,
      i.processId, p.name,
      t.nodeId, t.name,
      t.assignee, t.createdAt, t.dueDateTime, t.completedAt,
      t.state, i.businessKey, i.variables,
      t.formKey
    )
    from WfTask t, WfInstance i, WfProcess p
    where t.id = :taskId and t.instanceId = i.id and i.processId = p.processId
  """)
        Optional<TaskSummaryDTO> findSummary(@Param("taskId") UUID taskId);

}



