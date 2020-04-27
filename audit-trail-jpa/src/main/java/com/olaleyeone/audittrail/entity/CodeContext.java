package com.olaleyeone.audittrail.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class CodeContext {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(nullable = false)
    private String className;

    @Lob
    @Column(nullable = false)
    private String methodName;

    @Lob
    private String methodSignature;
    private Integer lineNumber;
}
