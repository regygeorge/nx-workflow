package com.miniflow.persist.repo;

import com.miniflow.persist.entity.WfProcess;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WfProcessRepo extends JpaRepository<WfProcess, String> {
}
