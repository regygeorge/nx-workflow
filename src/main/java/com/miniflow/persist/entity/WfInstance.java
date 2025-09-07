package com.miniflow.persist.entity;
// src/main/java/com/miniflow/persist/entity/WfInstance.java


import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity @Table(name="wf_instance")
public class WfInstance {
    @Id public UUID id;

    @Column(name = "process_id")
    public String processId;
    @Column(name = "business_key")
    public String businessKey;
    public String status;

    // <— map JSONB properly
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    public Map<String, Object> variables = new HashMap<>();

    @Column(name = "created_at")
    public OffsetDateTime createdAt;
    
    @Column(name = "updated_at")
    public OffsetDateTime updatedAt;
}
