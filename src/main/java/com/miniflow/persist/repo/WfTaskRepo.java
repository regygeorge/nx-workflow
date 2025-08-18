package com.miniflow.persist.repo;

import com.miniflow.persist.entity.WfTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WfTaskRepo extends JpaRepository<WfTask, UUID> {

    List<WfTask> findByInstanceIdAndState(UUID instanceId, String state);

    List<WfTask> findByInstanceId(UUID instanceId);
}
