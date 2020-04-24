package com.olaleyeone.audittrail.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
public class AuditTrail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    private String description;

    @ManyToOne
    private Task request;

    @Column(nullable = false)
    private Long estimatedTimeTakenInNanos;

    @Column(nullable = false)
    private LocalDateTime startedOn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    public static enum Status {
        SUCCESSFUL, ROLLED_BACK, UNKNOWN
    }
}
