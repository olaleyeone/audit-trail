package com.olaleyeone.audittrail.embeddable;

import lombok.Data;

import javax.persistence.Embeddable;
import javax.persistence.Lob;

@Data
@Embeddable
public class WebRequest {

    private String sessionId;
    private String userId;
    private String ipAddress;
    @Lob
    private String userAgent;
    @Lob
    private String uri;
    private Integer statusCode;
}
