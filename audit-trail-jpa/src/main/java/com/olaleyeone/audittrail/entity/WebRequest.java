package com.olaleyeone.audittrail.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class WebRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String clientId;
    private String userId;
    private String sessionId;
    private String ipAddress;
    @Lob
    private String userAgent;
    @Lob
    private String uri;
    private Integer statusCode;
}
