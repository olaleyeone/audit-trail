package com.olaleyeone.audittrail.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
public class Failure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String failureType;
    @Column(columnDefinition = "TEXT")
    private String failureReason;

    @ManyToOne(cascade = CascadeType.PERSIST, optional = false)
    private CodeContext codeContext;
}
