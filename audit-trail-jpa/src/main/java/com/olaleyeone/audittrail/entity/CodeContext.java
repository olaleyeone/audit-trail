package com.olaleyeone.audittrail.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class CodeContext {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition="TEXT")
    private String className;

    @Column(nullable = false, columnDefinition="TEXT")
    private String methodName;

    @Column(columnDefinition="TEXT")
    private String methodSignature;
    private Integer lineNumber;
}
