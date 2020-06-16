package com.olaleyeone.audittrail.entity;

import com.olaleyeone.audittrail.embeddable.Duration;
import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class TaskTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Task task;

    @ManyToOne(optional = false)
    private TaskActivity taskActivity;

    @Embedded
    private Duration duration;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    public static enum Status {
        COMMITTED, ROLLED_BACK, UNKNOWN
    }
}
