    package com.miniflow.persist.entity;

    import jakarta.persistence.*;
    import java.io.Serializable;
    import java.time.OffsetDateTime;
    import java.util.Objects;
    import java.util.UUID;

    import org.hibernate.annotations.JdbcTypeCode;
    import org.hibernate.annotations.UpdateTimestamp;
    import org.hibernate.type.SqlTypes;


    import jakarta.persistence.*;
    import org.hibernate.annotations.UpdateTimestamp;

    import java.io.Serializable;
    import java.time.OffsetDateTime;
    import java.util.Objects;
    import java.util.UUID;

    @Entity
    @Table(name = "wf_variable")
    @IdClass(WfVariable.PK.class)
    public class WfVariable {

        @Id
        @Column(name = "instance_id", nullable = false)
        public UUID instanceId;

        @Id
        @Column(name = "\"key\"", nullable = false, length = 200) // quoted "key"
        public String key;

        @Column(name = "value_text", columnDefinition = "text", nullable = false)
        public String valueText;  // JSON string

        // Read-only generated jsonb column (optional to expose)
        @Column(name = "value_jsonb", columnDefinition = "jsonb", insertable = false, updatable = false)
        public String valueJsonb;

        @UpdateTimestamp
        @Column(name = "updated_at", nullable = false)
        public OffsetDateTime updatedAt;

        // ---- Composite PK ----
        public static class PK implements Serializable {
            public UUID instanceId;
            public String key;

            public PK() {}
            public PK(UUID instanceId, String key) {
                this.instanceId = instanceId;
                this.key = key;
            }
            @Override public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof PK pk)) return false;
                return Objects.equals(instanceId, pk.instanceId) && Objects.equals(key, pk.key);
            }
            @Override public int hashCode() { return Objects.hash(instanceId, key); }
        }
    }
