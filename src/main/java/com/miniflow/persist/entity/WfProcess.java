package com.miniflow.persist.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "wf_process")
public class WfProcess {

    @Id
    @Column(name = "process_id")
    public String processId;
    public String name;
    @Column(columnDefinition = "text")
    public String bpmnXml;
    public OffsetDateTime deployedAt;
}
