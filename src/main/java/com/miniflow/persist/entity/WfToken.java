package com.miniflow.persist.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name="wf_token")
public class WfToken { @Id
public UUID id; public UUID instanceId;
public String nodeId;
public boolean active;
public OffsetDateTime createdAt;
public OffsetDateTime updatedAt; }
