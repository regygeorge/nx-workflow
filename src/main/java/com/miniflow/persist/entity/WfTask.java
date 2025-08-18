package com.miniflow.persist.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name="wf_task")
public class WfTask { @Id
public UUID id; public UUID instanceId;
public UUID tokenId; public String nodeId;
public String name; public String state;
public OffsetDateTime createdAt;
public OffsetDateTime completedAt; }
