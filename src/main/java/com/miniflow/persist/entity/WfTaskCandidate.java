package com.miniflow.persist.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;


import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "wf_task_candidate")
@IdClass(WfTaskCandidate.PK.class)
public class WfTaskCandidate {

    @Id
    @Column(name = "task_id", nullable = false)
    public UUID taskId;

    @Id
    @Column(name = "type", nullable = false, length = 1) // 'U' or 'G'
    public String type;

    @Id
    @Column(name = "candidate", nullable = false)
    public String candidate;

    // --- PK ---
    public static class PK implements Serializable {
        public UUID taskId; public String type; public String candidate;
        public PK() {}
        public PK(UUID taskId, String type, String candidate) {
            this.taskId = taskId; this.type = type; this.candidate = candidate;
        }
        @Override public boolean equals(Object o){
            if (this == o) return true;
            if (!(o instanceof PK pk)) return false;
            return Objects.equals(taskId, pk.taskId)
                    && Objects.equals(type, pk.type)
                    && Objects.equals(candidate, pk.candidate);
        }
        @Override public int hashCode(){ return Objects.hash(taskId, type, candidate); }
    }
}
