package com.olaleyeone.audittrail.impl;

import com.olaleyeone.audittrail.api.EntityAttributeData;
import com.olaleyeone.audittrail.api.EntityOperation;
import com.olaleyeone.audittrail.entity.AuditTrailActivity;
import com.olaleyeone.audittrail.entity.AuditTrail;
import com.olaleyeone.audittrail.entity.EntityState;
import com.olaleyeone.audittrail.entity.EntityStateAttribute;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class AuditTrailLoggerDelegate {

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final EntityManager entityManager;
    private final TransactionTemplate transactionTemplate;

    public AuditTrail saveUnitOfWork(AuditTrailLogger auditTrailLogger, AuditTrail.Status status) {
        AuditTrail auditTrail = createUnitOfWork(auditTrailLogger, status);
        auditTrailLogger.getEntityStateLogger().getOperations().forEach(entityHistoryLog -> createEntityHistory(auditTrail, entityHistoryLog));
        auditTrailLogger.getAuditTrailActivities().forEach(activityLog -> {
            activityLog.setId(null);
            activityLog.setAuditTrail(auditTrail);
            entityManager.persist(activityLog);
        });
        return auditTrail;
    }

    AuditTrail createUnitOfWork(AuditTrailLogger auditTrailLogger, AuditTrail.Status status) {
        AuditTrail auditTrail = new AuditTrail();
        auditTrail.setStatus(status);
        List<AuditTrailActivity> auditTrailActivities = auditTrailLogger.getAuditTrailActivities();
        if (!auditTrailActivities.isEmpty()) {
            auditTrail.setName(auditTrailActivities.iterator().next().getName());
        }
        auditTrail.setStartedOn(auditTrailLogger.getStartTime());
        auditTrail.setEstimatedTimeTakenInNanos(auditTrailLogger.getStartTime().until(LocalDateTime.now(), ChronoUnit.NANOS));
        auditTrailLogger.getTask().ifPresent(task -> {
            auditTrail.setRequest(task);
            task.setEstimatedTimeTakenInNanos(task.getStartedOn().until(LocalDateTime.now(), ChronoUnit.NANOS));
            entityManager.merge(task);
        });
        entityManager.persist(auditTrail);
        return auditTrail;
    }

    EntityState createEntityHistory(AuditTrail auditTrail, EntityOperation entityOperation) {
        EntityState entityState = new EntityState();
        entityState.setAuditTrail(auditTrail);
        entityState.setOperationType(entityOperation.getOperationType());
        entityState.setEntityName(entityOperation.getEntityIdentifier().getEntityName());
        entityState.setEntityId(entityOperation.getEntityIdentifier().getPrimaryKey().toString());
        entityManager.persist(entityState);

        if (entityOperation.getAttributes() != null) {
            entityOperation.getAttributes().entrySet().forEach(entry -> createEntityHistoryAttribute(entityState, entry));
        }

        return entityState;
    }

    EntityStateAttribute createEntityHistoryAttribute(EntityState entityState, Map.Entry<String, EntityAttributeData> field) {
        EntityStateAttribute entityStateAttribute = new EntityStateAttribute();
        entityStateAttribute.setEntityState(entityState);
        entityStateAttribute.setName(field.getKey());
        EntityAttributeData historyData = field.getValue();
        entityStateAttribute.setModified(historyData.isModified());
        historyData.getPreviousValue().getTextValue().ifPresent(entityStateAttribute::setPreviousValue);
        historyData.getValue().getTextValue().ifPresent(entityStateAttribute::setValue);

        entityManager.persist(entityStateAttribute);
        return entityStateAttribute;
    }

    public void saveFailure(AuditTrailLogger auditTrailLogger, AuditTrail.Status status) {
        try {
            transactionTemplate.execute(txStatus -> {
                saveUnitOfWork(auditTrailLogger, status);
                return null;
            });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
