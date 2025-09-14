package com.miniflow.persist.repo;

import com.miniflow.persist.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
import java.util.UUID;

public interface WfVariableRepo extends JpaRepository<WfVariable, WfVariable.PK> {

    List<WfVariable> findByInstanceId(UUID instanceId);
    Optional<WfVariable> findByInstanceIdAndKey(UUID instanceId, String key);
}
