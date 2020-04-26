package com.olaleyeone.audittrail.embeddable;

import lombok.Data;

import javax.persistence.Embeddable;

@Data
@Embeddable
public class TaskActivityEntryPoint {

    private String signature;
    private String className;
    private String methodName;
    private Integer lineNumber;
}
