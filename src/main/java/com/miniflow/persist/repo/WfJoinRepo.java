package com.miniflow.persist.repo;

import com.miniflow.persist.entity.WfJoin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WfJoinRepo extends JpaRepository<WfJoin, WfJoin.PK> {
}
