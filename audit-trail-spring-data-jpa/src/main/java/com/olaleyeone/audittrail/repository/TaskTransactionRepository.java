package com.olaleyeone.audittrail.repository;

import com.olaleyeone.audittrail.entity.TaskTransaction;
import com.olaleyeone.audittrail.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskTransactionRepository extends JpaRepository<TaskTransaction, Long> {

    List<TaskTransaction> getAllByTask(Task task);
}
