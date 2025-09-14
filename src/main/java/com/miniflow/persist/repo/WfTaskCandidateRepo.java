package com.miniflow.persist.repo;

import com.miniflow.persist.entity.WfTaskCandidate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

import com.miniflow.persist.entity.WfTaskCandidate;
import java.util.Collection;
import java.util.UUID;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface WfTaskCandidateRepo extends JpaRepository<WfTaskCandidate, WfTaskCandidate.PK> {

    @Query("""
    select count(c) from WfTaskCandidate c
    where c.taskId = :taskId
      and (
            (c.type = 'U' and c.candidate = :user)
         or (c.type = 'G' and c.candidate in :groups)
      )
  """)
    long countMembership(@Param("taskId") UUID taskId,
                         @Param("user") String user,
                         @Param("groups") Collection<String> groups);


    List<WfTaskCandidate> findByTaskId(UUID taskId);
}

