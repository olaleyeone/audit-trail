package com.olaleyeone.audittrail.entity;

import com.olaleyeone.audittrail.embeddable.Duration;
import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class TaskActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Task task;

    @ManyToOne
    private TaskActivity parentActivity;

    @ManyToOne
    private TaskTransaction taskTransaction;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private Integer precedence;

    @OneToOne(cascade = CascadeType.PERSIST)
    private CodeInstruction entryPoint;

    @Embedded
    private Duration duration;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskActivity.Status status;

    private String failureType;
    private String failureReason;
    @ManyToOne(cascade = CascadeType.PERSIST)
    private CodeInstruction failurePoint;

    public static enum Status {
        SUCCESSFUL, FAILED, IN_PROGRESS
    }
}
