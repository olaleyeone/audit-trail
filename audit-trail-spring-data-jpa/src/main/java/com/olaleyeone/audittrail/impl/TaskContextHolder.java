package com.olaleyeone.audittrail.impl;

import org.springframework.beans.factory.FactoryBean;

public class TaskContextHolder implements FactoryBean<TaskContextImpl> {

    private static final ThreadLocal<TaskContextImpl> taskContextThreadLocal = new ThreadLocal<>();

    @Override
    public TaskContextImpl getObject() {
        return taskContextThreadLocal.get();
    }

    @Override
    public Class<?> getObjectType() {
        return TaskContextImpl.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    public void registerContext(TaskContextImpl taskContext) {
        taskContextThreadLocal.set(taskContext);
    }
}
