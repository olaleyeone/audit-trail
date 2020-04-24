package com.olaleyeone.audittrail.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
public class Task {

    public static final String BACKGROUND_JOB = "BACKGROUND_JOB";
    public static final String WEB_REQUEST = "WEB_REQUEST";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String type;
    private String description;

    @Embedded
    private WebRequest webRequest;

    @Column(nullable = false)
    private LocalDateTime startedOn;

    private Long estimatedTimeTakenInNanos;
}
