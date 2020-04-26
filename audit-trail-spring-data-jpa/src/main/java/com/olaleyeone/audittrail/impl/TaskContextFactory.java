package com.olaleyeone.audittrail.impl;

import com.olaleyeone.audittrail.entity.Task;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TaskContextFactory {

    private final TaskContextHolder taskContextHolder;
    private final TaskTransactionContextFactory taskTransactionContextFactory;

    public TaskContextImpl start(Task task) {
        TaskContextImpl taskContext = new TaskContextImpl(task, null, taskContextHolder, taskTransactionContextFactory);
        taskContextHolder.registerContext(taskContext);
        return taskContext;
    }

}
