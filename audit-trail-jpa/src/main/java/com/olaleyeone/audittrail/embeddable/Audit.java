package com.olaleyeone.audittrail.embeddable;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.PrePersist;
import java.time.LocalDateTime;

@Data
@Embeddable
public class Audit {

    @Column(updatable = false, nullable = false)
    private LocalDateTime createdOn;
    private String createdBy;

    private LocalDateTime lastUpdatedOn;
    private String lastUpdatedBy;

    @PrePersist
    public void prePersist() {
        if (createdOn != null) {
            return;
        }
        createdOn = LocalDateTime.now();
    }
}
