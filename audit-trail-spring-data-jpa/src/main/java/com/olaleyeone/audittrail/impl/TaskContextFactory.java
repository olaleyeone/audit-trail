package com.olaleyeone.audittrail.impl;

import com.olaleyeone.audittrail.entity.Task;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TaskContextFactory {

    private final TaskContextHolder taskContextHolder;

    public TaskContextImpl start(Task task) {
        TaskContextImpl taskContext = new TaskContextImpl(task, null, taskContextHolder);
        taskContextHolder.registerContext(taskContext);
        return taskContext;
    }

}
