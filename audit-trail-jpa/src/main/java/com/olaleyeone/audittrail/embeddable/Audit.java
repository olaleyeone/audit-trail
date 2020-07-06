package com.olaleyeone.audittrail.embeddable;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.OffsetDateTime;

@Data
@Embeddable
public class Audit {

    @Column(updatable = false, nullable = false)
    private OffsetDateTime createdOn;

    private String createdBy;

    private OffsetDateTime lastUpdatedOn;
    private String lastUpdatedBy;

    @PrePersist
    public void prePersist() {
        if (createdOn != null) {
            return;
        }
        createdOn = OffsetDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        lastUpdatedOn = OffsetDateTime.now();
    }
}
