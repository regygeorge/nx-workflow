package com.miniflow.persist.repo;

import com.miniflow.persist.entity.WfToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WfTokenRepo extends JpaRepository<WfToken, UUID> {

    List<WfToken> findByInstanceIdAndActive(UUID instanceId, boolean active);
}
