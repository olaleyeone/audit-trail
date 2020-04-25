package com.olaleyeone.audittrail.impl;

import com.olaleyeone.audittrail.api.EntityOperation;
import com.olaleyeone.audittrail.api.EntityStateLogger;
import com.olaleyeone.audittrail.entity.Task;
import com.olaleyeone.audittrail.entity.TaskActivity;
import com.olaleyeone.audittrail.entity.TaskTransaction;
import lombok.AccessLevel;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronization;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class TaskTransactionContext implements TransactionSynchronization {

    @Getter(AccessLevel.NONE)
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final LocalDateTime startTime = LocalDateTime.now();
    private final List<TaskActivity> auditTransactionActivities = new ArrayList<>();

    private final TaskTransactionLogger taskTransactionLogger;
    private final EntityStateLogger entityStateLogger;

//    @Getter(lazy = true)
//    private final ActivityLogger activityLogger = new ActivityLoggerImpl(auditTransactionActivities);


    public TaskTransactionContext(TaskTransactionLogger taskTransactionLogger) {
        this(taskTransactionLogger, new EntityStateLoggerImpl());
    }

    public TaskTransactionContext(TaskTransactionLogger taskTransactionLogger, EntityStateLogger entityStateLogger) {
        this.taskTransactionLogger = taskTransactionLogger;
        this.entityStateLogger = entityStateLogger;
    }

//    public ActivityLogger createActivityLogger(List<TaskActivity> auditTrailActivities) {
//        return new ActivityLoggerImpl(auditTrailActivities);
//    }

    public abstract Task getTask();

    public abstract TaskActivity getTaskActivity();

    public void logActivity(TaskActivity taskActivity) {
        this.auditTransactionActivities.add(taskActivity);
    }

//    public boolean canCommitWithoutActivityLog() {
//        return false;
//    }

    public EntityStateLogger getEntityStateLogger() {
//        checkHasActivityLog();
        return entityStateLogger;
    }

    @Override
    public void beforeCommit(boolean readOnly) {
//        checkHasActivityLog();
        List<EntityOperation> logs = entityStateLogger.getOperations();
        if (logs.isEmpty()) {
            logger.warn("No work done in transaction");
            return;
        }
        taskTransactionLogger.saveUnitOfWork(this, TaskTransaction.Status.SUCCESSFUL);
    }

    @Override
    public void afterCompletion(int status) {
        if (status == TransactionSynchronization.STATUS_COMMITTED || auditTransactionActivities.isEmpty()) {
            return;
        }
        taskTransactionLogger.saveFailure(this, status == TransactionSynchronization.STATUS_ROLLED_BACK
                ? TaskTransaction.Status.ROLLED_BACK
                : TaskTransaction.Status.UNKNOWN);
    }

//    private void checkHasActivityLog() {
//        if (!auditTransactionActivities.isEmpty()) {
//            return;
//        }
//        if (!canCommitWithoutActivityLog()) {
//            throw new NoActivityLogException();
//        }
//        logger.warn("No activity log");
//    }
}
