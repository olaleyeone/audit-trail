package com.olaleyeone.audittrail.repository;

import com.olaleyeone.audittrail.entity.TaskActivity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskActivityRepository extends JpaRepository<TaskActivity, Long> {
}
