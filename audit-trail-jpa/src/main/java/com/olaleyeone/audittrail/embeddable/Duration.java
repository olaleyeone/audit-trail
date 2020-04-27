package com.olaleyeone.audittrail.embeddable;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Embeddable
public class Duration {

    @Column(nullable = false)
    private LocalDateTime startedOn;

    @Column
    private Long nanoSecondsTaken;
}
