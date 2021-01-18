package com.olaleyeone.audittrail.embeddable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Embeddable
public class Duration {

    @Column(nullable = false)
    private OffsetDateTime startedAt;

    @Column
    private Long nanoSecondsTaken;
}
