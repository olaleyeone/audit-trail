package com.olaleyeone.audittrail.repository;

import com.olaleyeone.audittrail.entity.TaskTransaction;
import com.olaleyeone.audittrail.entity.EntityState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface EntityStateRepository extends JpaRepository<EntityState, Long> {

    @Query("SELECT h FROM EntityState h WHERE h.taskTransaction=?1 AND h.entityName=?2 AND h.entityId=?3")
    Optional<EntityState> getByUnitOfWork(TaskTransaction taskTransaction, String name, String id);

    @Query("SELECT COUNT(h) FROM EntityState h WHERE h.taskTransaction=?1")
    int countByUnitOfWork(TaskTransaction taskTransaction);
}
