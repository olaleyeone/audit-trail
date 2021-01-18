package com.olaleyeone.audittrail.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
public class Failure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String type;
    @Column(columnDefinition = "TEXT")
    private String reason;

    @ManyToOne(cascade = CascadeType.PERSIST)
    private CodeContext codeContext;

    private OffsetDateTime createdAt;

    @PrePersist
    public void beforeInsert() {
        createdAt = OffsetDateTime.now();
    }
}
