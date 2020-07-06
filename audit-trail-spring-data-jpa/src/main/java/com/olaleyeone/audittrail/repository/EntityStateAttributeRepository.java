package com.olaleyeone.audittrail.repository;

import com.olaleyeone.audittrail.entity.EntityState;
import com.olaleyeone.audittrail.entity.EntityStateAttribute;
import com.olaleyeone.audittrail.entity.TaskTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface EntityStateAttributeRepository extends JpaRepository<EntityStateAttribute, Long> {

    @Query("SELECT h FROM EntityStateAttribute h WHERE h.entityState=?1 AND h.name=?2")
    Optional<EntityStateAttribute> getByEntityHistory(EntityState entityState, String name);

    @Query("SELECT COUNT(h) FROM EntityStateAttribute h WHERE h.entityState.taskTransaction=?1")
    int countByUnitOfWork(TaskTransaction taskTransaction);
}
