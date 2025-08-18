package com.miniflow.persist.entity;

import jakarta.persistence.*;

import java.util.UUID;


import jakarta.persistence.*;
import java.io.Serializable;
import java.util.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity @Table(name="wf_variable") @IdClass(WfVariable.PK.class)
public class WfVariable {
    @Id public UUID instanceId;
    @Id public String key;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    public Object value; // or Map<String,Object>

    public static class PK implements Serializable {
        public UUID instanceId; public String key;
        public PK(){}
        public PK(UUID i,String k){instanceId=i;key=k;}
    }
}
