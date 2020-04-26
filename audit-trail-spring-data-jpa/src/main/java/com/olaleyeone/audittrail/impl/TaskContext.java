package com.olaleyeone.audittrail.impl;

import com.olaleyeone.audittrail.entity.Task;
import com.olaleyeone.audittrail.entity.TaskActivity;

import java.util.Optional;
import java.util.function.Supplier;

public interface TaskContext {

    Task getTask();

    Optional<TaskActivity> getTaskActivity();

//    void logActivity(TaskActivity taskActivity);

    <E> E doAndReturn(String name, String description, Supplier<E> action);

    <E> E doAndReturn(String name, Supplier<E> action);

    void setDescription(String description);
}
