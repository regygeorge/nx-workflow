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

    public String processId;
    public String businessKey;
    public String status;

    // <— map JSONB properly
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    public Map<String, Object> variables = new HashMap<>();

    public OffsetDateTime createdAt;
    public OffsetDateTime updatedAt;
}
