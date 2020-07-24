package com.olaleyeone.audittrail.entity;

import com.olaleyeone.audittrail.embeddable.Duration;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;

@Entity

@Getter
@Setter
public class TaskActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Task task;

    @ManyToOne
    private TaskActivity parentActivity;

    @ManyToOne
    private TaskTransaction taskTransaction;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer precedence;

    @OneToOne
    @JoinColumn(nullable = false)
    private CodeContext entryPoint;

    @Embedded
    private Duration duration;

    @OneToOne
    private Failure failure;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskActivity.Status status;

    public static enum Status {
        SUCCESSFUL, FAILED, IN_PROGRESS
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskActivity that = (TaskActivity) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
