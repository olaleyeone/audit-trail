package com.olaleyeone.audittrail.impl;

import com.olaleyeone.audittrail.entity.TaskActivity;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Optional;

public class TaskTransactionContextFactory implements FactoryBean<TaskTransactionContext> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @PersistenceContext
    private EntityManager entityManager;

    private final TaskContextHolder taskContextHolder;

    @Getter
    private TaskTransactionLogger taskTransactionLogger;

    public TaskTransactionContextFactory(TaskContextHolder taskContextHolder) {
        this.taskContextHolder = taskContextHolder;
    }

    @PostConstruct
    public void init() {
        taskTransactionLogger = new TaskTransactionLogger(entityManager);
    }

    @Override
    public TaskTransactionContext getObject() {
        return getCurrentTaskTransactionLogger()
                .orElseGet(() -> {
                    TaskTransactionContext taskTransactionContext = createTaskTransactionContext(taskTransactionLogger);
                    TransactionSynchronizationManager.registerSynchronization(taskTransactionContext);
                    return taskTransactionContext;
                });
    }

    public void initialize() {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        getObject();
    }

    public void joinAvailableTransaction(TaskActivity taskActivity) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        getObject().addActivity(taskActivity);
    }

    private Optional<TaskTransactionContext> getCurrentTaskTransactionLogger() {
        return TransactionSynchronizationManager.getSynchronizations()
                .stream()
                .filter(it -> it instanceof TaskTransactionContext)
                .findFirst()
                .map(it -> (TaskTransactionContext) it);
    }

    protected TaskTransactionContext createTaskTransactionContext(TaskTransactionLogger taskTransactionLogger) {
        return new TaskTransactionContext(taskContextHolder.getObject(), taskTransactionLogger);
    }

    @Override
    public Class<?> getObjectType() {
        return TaskTransactionContext.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
