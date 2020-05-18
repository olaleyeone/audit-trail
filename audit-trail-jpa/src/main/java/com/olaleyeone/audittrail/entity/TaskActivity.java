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

    @ManyToOne(optional = false, cascade = CascadeType.PERSIST)
    private Task task;

    @ManyToOne(cascade = CascadeType.PERSIST)
    private TaskActivity parentActivity;

    @ManyToOne
    private TaskTransaction taskTransaction;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition="TEXT")
    private String description;

    @Column(nullable = false)
    private Integer precedence;

    @OneToOne(cascade = CascadeType.PERSIST)
    private CodeContext entryPoint;

    @Embedded
    private Duration duration;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskActivity.Status status;

    @Column(columnDefinition="TEXT")
    private String failureType;
    @Column(columnDefinition="TEXT")
    private String failureReason;

    @ManyToOne(cascade = CascadeType.PERSIST)
    private CodeContext failurePoint;

    public static enum Status {
        SUCCESSFUL, FAILED, IN_PROGRESS
    }
}
