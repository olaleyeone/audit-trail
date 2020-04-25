package com.olaleyeone.audittrail.impl;

import com.olaleyeone.audittrail.entity.Task;
import com.olaleyeone.audittrail.entity.TaskActivity;
import com.olaleyeone.audittrail.error.NoTaskActivityException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Optional;

@RequiredArgsConstructor
public class TaskTransactionContextFactory implements FactoryBean<TaskTransactionContext> {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private Provider<TaskContext> taskContextProvider;

    private TaskTransactionLogger taskTransactionLogger;

    @PostConstruct
    public void init() {
        taskTransactionLogger = new TaskTransactionLogger(entityManager, transactionTemplate);
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

    private Optional<TaskTransactionContext> getCurrentTaskTransactionLogger() {
        return TransactionSynchronizationManager.getSynchronizations()
                .stream()
                .filter(it -> it instanceof TaskTransactionContext)
                .findFirst()
                .map(it -> (TaskTransactionContext) it);
    }

    public TaskTransactionContext createTaskTransactionContext(TaskTransactionLogger taskTransactionLogger) {
        TaskContext taskContext = taskContextProvider.get();
        TaskActivity taskActivity = taskContext.getTaskActivity().orElseThrow(NoTaskActivityException::new);
        return new TaskTransactionContext(taskTransactionLogger) {

            @Override
            public Task getTask() {
                return taskActivity.getTask();
            }

            @Override
            public TaskActivity getTaskActivity() {
                return taskActivity;
            }
        };
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
