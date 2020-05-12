package com.olaleyeone.audittrail.impl;

import com.olaleyeone.audittrail.api.EntityAttributeData;
import com.olaleyeone.audittrail.api.EntityOperation;
import com.olaleyeone.audittrail.embeddable.Duration;
import com.olaleyeone.audittrail.entity.EntityState;
import com.olaleyeone.audittrail.entity.EntityStateAttribute;
import com.olaleyeone.audittrail.entity.TaskActivity;
import com.olaleyeone.audittrail.entity.TaskTransaction;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.Map;

@RequiredArgsConstructor
public class TaskTransactionLogger {

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final EntityManager entityManager;

    public TaskTransaction saveTaskTransaction(TaskTransactionContext taskTransactionContext) {

        TaskTransaction taskTransaction = taskTransactionContext.getTaskTransaction();
        entityManager.persist(taskTransaction);

        taskTransactionContext.getEntityStateLogger().getOperations()
                .forEach(entityHistoryLog -> createEntityHistory(taskTransaction, entityHistoryLog));

        taskTransactionContext.getTaskActivities().forEach(activityInTransaction -> {
            entityManager.persist(activityInTransaction);
        });
        return taskTransaction;
    }

    TaskTransaction createTaskTransaction(TaskTransactionContext taskTransactionContext, LocalDateTime startTime) {
        TaskTransaction taskTransaction = new TaskTransaction();
//        taskTransaction.setStatus(status);

        TaskActivity taskActivity = taskTransactionContext.getTaskActivity();

        taskTransaction.setTask(taskActivity.getTask());
        taskTransaction.setTaskActivity(taskActivity);

        taskTransaction.setDuration(Duration.builder()
                .startedOn(startTime)
//                .nanoSeconds(taskTransactionContext.getStartTime().until(LocalDateTime.now(), ChronoUnit.NANOS))
                .build());

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

        entityStateAttribute.setHasPreviousValue(historyData.getPreviousValue().getData().isPresent());
        entityStateAttribute.setHasNewValue(historyData.getValue().getData().isPresent());
        
        historyData.getPreviousValue().getTextValue().ifPresent(entityStateAttribute::setPreviousValue);
        historyData.getValue().getTextValue().ifPresent(entityStateAttribute::setNewValue);

        entityManager.persist(entityStateAttribute);
        return entityStateAttribute;
    }
}
