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
    private OffsetDateTime createdAt;

    private String createdBy;

    private OffsetDateTime lastUpdatedAt;
    private String lastUpdatedBy;

    @PrePersist
    public void prePersist() {
        if (createdAt != null) {
            return;
        }
        createdAt = OffsetDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        lastUpdatedAt = OffsetDateTime.now();
    }
}
