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
public class TaskTransactionContext implements TransactionSynchronization {

    @Getter(AccessLevel.NONE)
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final LocalDateTime startTime = LocalDateTime.now();
    private final List<TaskActivity> auditTransactionActivities = new ArrayList<>();

    private final TaskContextImpl taskContext;
    private final TaskTransactionLogger taskTransactionLogger;
    private final EntityStateLogger entityStateLogger;

    private TaskTransaction.Status status;

    public TaskTransactionContext(TaskContextImpl taskContext, TaskTransactionLogger taskTransactionLogger) {
        this(taskContext, taskTransactionLogger, new EntityStateLoggerImpl());
    }

    public TaskTransactionContext(TaskContextImpl taskContext, TaskTransactionLogger taskTransactionLogger, EntityStateLogger entityStateLogger) {
        this.taskContext = taskContext;
        this.taskTransactionLogger = taskTransactionLogger;
        this.entityStateLogger = entityStateLogger;
    }

    public void addActivity(TaskActivity taskActivity) {
        this.auditTransactionActivities.add(taskActivity);
    }

    public EntityStateLogger getEntityStateLogger() {
        return entityStateLogger;
    }

    @Override
    public void beforeCommit(boolean readOnly) {
        List<EntityOperation> logs = entityStateLogger.getOperations();
        if (logs.isEmpty()) {
            logger.warn("No work done in transaction");
            return;
        }
        taskTransactionLogger.saveTaskTransaction(this, TaskTransaction.Status.COMMITTED);
    }

    @Override
    public void afterCompletion(int status) {
        if (status == TransactionSynchronization.STATUS_COMMITTED) {
            this.status = TaskTransaction.Status.COMMITTED;
        } else {
            this.status = status == TransactionSynchronization.STATUS_ROLLED_BACK
                    ? TaskTransaction.Status.ROLLED_BACK
                    : TaskTransaction.Status.UNKNOWN;
            if (!auditTransactionActivities.isEmpty()) {
                taskContext.registerFailedTransaction(this);
            }
        }
        taskContext.end();
    }

    public Task getTask() {
        return taskContext.getTask();
    }

    public TaskActivity getTaskActivity() {
        return taskContext.getTaskActivity().get();
    }
}
