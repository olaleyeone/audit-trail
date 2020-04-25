package com.olaleyeone.audittrail.impl;

import org.springframework.beans.factory.FactoryBean;

public class TaskContextHolder implements FactoryBean<TaskContext> {

    private static final ThreadLocal<TaskContext> taskContextThreadLocal = new ThreadLocal<>();

    @Override
    public TaskContext getObject() {
        return taskContextThreadLocal.get();
    }

    @Override
    public Class<?> getObjectType() {
        return TaskContext.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    public void registerContext(TaskContext taskContext) {
        taskContextThreadLocal.set(taskContext);
    }
}
