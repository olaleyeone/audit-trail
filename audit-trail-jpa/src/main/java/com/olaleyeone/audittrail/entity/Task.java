package com.olaleyeone.audittrail.entity;

import com.olaleyeone.audittrail.embeddable.Duration;
import lombok.Data;

import javax.persistence.*;

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

    @Lob
    private String description;

    @OneToOne(cascade = {CascadeType.PERSIST})
    @JoinColumn(updatable = false)
    private WebRequest webRequest;

    @Embedded
    private Duration duration;
}
