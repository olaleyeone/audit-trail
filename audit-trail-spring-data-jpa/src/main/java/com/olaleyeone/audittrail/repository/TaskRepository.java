package com.olaleyeone.audittrail.repository;

import com.olaleyeone.audittrail.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {

}
