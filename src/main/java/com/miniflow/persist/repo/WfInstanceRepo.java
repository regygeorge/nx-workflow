package com.miniflow.persist.repo;

import com.miniflow.persist.entity.WfInstance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WfInstanceRepo extends JpaRepository<WfInstance, UUID> {

    List<WfInstance> findByProcessId(String processId);
}
