package com.miniflow.persist.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "wf_join")
@IdClass(WfJoin.PK.class)
public class WfJoin {
    @Id
    public UUID instanceId;
    @Id
    public String nodeId;
    public int arrivals;

    public static class PK implements java.io.Serializable {
        public UUID instanceId;
        public String nodeId;

        public PK() {
        }

        public PK(UUID i, String n) {
            instanceId = i;
            nodeId = n;
        }
    }
}
