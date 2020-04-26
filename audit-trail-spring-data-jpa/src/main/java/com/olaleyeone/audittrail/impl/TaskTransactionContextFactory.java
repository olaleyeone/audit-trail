package com.olaleyeone.audittrail.impl;

import com.olaleyeone.audittrail.entity.TaskActivity;
import com.olaleyeone.audittrail.error.NoTaskActivityException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Supplier;

public class TaskTransactionContextFactory implements FactoryBean<TaskTransactionContext> {

    @PersistenceContext
    private EntityManager entityManager;

    private final TaskContextHolder taskContextHolder;

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

    private Optional<TaskTransactionContext> getCurrentTaskTransactionLogger() {
        return TransactionSynchronizationManager.getSynchronizations()
                .stream()
                .filter(it -> it instanceof TaskTransactionContext)
                .findFirst()
                .map(it -> (TaskTransactionContext) it);
    }

    public TaskTransactionContext createTaskTransactionContext(TaskTransactionLogger taskTransactionLogger) {
        TaskContextImpl taskContext = taskContextHolder.getObject();
        TaskActivity parentTaskActivity = taskContext.getTaskActivity().orElseThrow(NoTaskActivityException::new);

        TaskTransactionContext taskTransactionContext = new TaskTransactionContext(taskContext, taskTransactionLogger);

        TaskContextImpl wrapperContext = new TaskContextImpl(parentTaskActivity, taskContextHolder) {

            @Override
            protected <E> E startActivity(TaskActivity taskActivity, Supplier<E> action, LocalDateTime now) {
                taskTransactionContext.addActivity(taskActivity);
                taskActivity.setPrecedence(taskContext.getTaskActivities().size() + 1);
                taskContext.getTaskActivities().add(taskActivity);
                return super.startActivity(taskActivity, action, now);
            }
        };
        taskContextHolder.registerContext(wrapperContext);

        return taskTransactionContext;
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
