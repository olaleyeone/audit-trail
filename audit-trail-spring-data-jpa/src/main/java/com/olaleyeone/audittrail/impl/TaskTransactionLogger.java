package com.olaleyeone.audittrail.impl;

import com.olaleyeone.audittrail.api.EntityAttributeData;
import com.olaleyeone.audittrail.api.EntityOperation;
import com.olaleyeone.audittrail.embeddable.Duration;
import com.olaleyeone.audittrail.entity.EntityState;
import com.olaleyeone.audittrail.entity.EntityStateAttribute;
import com.olaleyeone.audittrail.entity.TaskTransaction;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@RequiredArgsConstructor
public class TaskTransactionLogger {

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final EntityManager entityManager;
    private final TransactionTemplate transactionTemplate;

    public TaskTransaction saveUnitOfWork(TaskTransactionContext taskTransactionContext, TaskTransaction.Status status) {
        TaskTransaction taskTransaction = createTaskTransaction(taskTransactionContext, status);
        taskTransactionContext.getEntityStateLogger().getOperations().forEach(entityHistoryLog -> createEntityHistory(taskTransaction, entityHistoryLog));

        taskTransactionContext.getAuditTransactionActivities().forEach(taskActivity -> {
            taskActivity.setId(null);
            taskActivity.setTask(taskTransaction.getTask());
            taskActivity.setParentActivity(taskTransaction.getTaskActivity());
            taskActivity.setTaskTransaction(taskTransaction);
            entityManager.persist(taskActivity);
        });
        return taskTransaction;
    }

    TaskTransaction createTaskTransaction(TaskTransactionContext taskTransactionContext, TaskTransaction.Status status) {
        TaskTransaction taskTransaction = new TaskTransaction();
        taskTransaction.setStatus(status);

        taskTransaction.setTaskActivity(taskTransactionContext.getTaskActivity());

        taskTransaction.setDuration(Duration.builder()
                .startedOn(taskTransactionContext.getStartTime())
                .nanoSeconds(taskTransactionContext.getStartTime().until(LocalDateTime.now(), ChronoUnit.NANOS))
                .build());

        taskTransaction.setTask(taskTransactionContext.getTask());

        entityManager.persist(taskTransaction);
        return taskTransaction;
    }

    EntityState createEntityHistory(TaskTransaction taskTransaction, EntityOperation entityOperation) {
        EntityState entityState = new EntityState();
        entityState.setTaskTransaction(taskTransaction);
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

    public void saveFailure(TaskTransactionContext taskTransactionContext, TaskTransaction.Status status) {
        try {
            transactionTemplate.execute(txStatus -> {
                saveUnitOfWork(taskTransactionContext, status);
                return null;
            });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
