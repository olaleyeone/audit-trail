package com.olaleyeone.audittrail.impl;

import com.olaleyeone.audittrail.entity.Task;
import com.olaleyeone.audittrail.entity.TaskActivity;

import java.util.Optional;

public interface TaskContext {

    Task getTask();

    Optional<TaskActivity> getTaskActivity();

    void logActivity(TaskActivity taskActivity);
}
