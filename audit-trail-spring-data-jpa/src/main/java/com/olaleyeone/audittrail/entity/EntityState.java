package com.olaleyeone.audittrail.entity;

import com.olaleyeone.audittrail.api.OperationType;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class EntityState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private AuditTrail auditTrail;

    @Column(nullable = false)
    private String entityName;
    @Column(nullable = false)
    private String entityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OperationType operationType;
}
