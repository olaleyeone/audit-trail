package com.olaleyeone.audittrail.entity;

import lombok.Data;

import javax.persistence.Embeddable;

@Data
@Embeddable
public class WebRequest {

    private String sessionId;
    private String userId;
    private String ipAddress;
    private String userAgent;
    private String uri;
    private Integer statusCode;
}
