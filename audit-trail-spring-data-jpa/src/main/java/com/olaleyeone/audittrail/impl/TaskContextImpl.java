package com.olaleyeone.audittrail.impl;

import com.olaleyeone.audittrail.advice.CodeLocationUtil;
import com.olaleyeone.audittrail.context.TaskContext;
import com.olaleyeone.audittrail.embeddable.Duration;
import com.olaleyeone.audittrail.entity.CodeInstruction;
import com.olaleyeone.audittrail.entity.Task;
import com.olaleyeone.audittrail.entity.TaskActivity;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Data
@RequiredArgsConstructor
public class TaskContextImpl implements TaskContext {

    private final Task task;
    private final TaskActivity taskActivity;
    private final TaskContextHolder taskContextHolder;

    @Getter(value = AccessLevel.NONE)
    private final List<TaskActivity> taskActivities = new ArrayList<>();
    private final List<TaskTransactionContext> failedTaskTransactionContexts = new ArrayList<>();

    public TaskContextImpl(TaskActivity taskActivity, TaskContextHolder taskContextHolder) {
        this.taskActivity = taskActivity;
        this.task = taskActivity.getTask();
        this.taskContextHolder = taskContextHolder;
    }

    public Task getTask() {
        return task;
    }

    public Optional<TaskActivity> getTaskActivity() {
        return Optional.ofNullable(taskActivity);
    }

    @Override
    public <E> E execute(String name, Supplier<E> action) {
        LocalDateTime now = LocalDateTime.now();
        TaskActivity taskActivity = getTaskActivity(name, null);

        return startActivity(taskActivity, action, now);
    }

    @Override
    public <E> E execute(String name, String description, Supplier<E> action) {
        LocalDateTime now = LocalDateTime.now();
        TaskActivity taskActivity = getTaskActivity(name, description);

        return startActivity(taskActivity, action, now);
    }

    private TaskActivity getTaskActivity(String name, String description) {
        TaskActivity taskActivity = new TaskActivity();
        taskActivity.setTask(task);
        taskActivity.setParentActivity(this.taskActivity);
        taskActivity.setName(name);
        taskActivity.setDescription(description);
        taskActivity.setStatus(TaskActivity.Status.IN_PROGRESS);

        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[2];
        CodeInstruction entryPoint = CodeLocationUtil.getEntryPoint(stackTraceElement);
        taskActivity.setEntryPoint(entryPoint);

        addActivity(taskActivity);
        return taskActivity;
    }

    protected <E> E startActivity(TaskActivity taskActivity, Supplier<E> action, LocalDateTime now) {
        taskContextHolder.registerContext(new TaskContextImpl(taskActivity, taskContextHolder));

        E result;
        try {
            result = action.get();
            taskActivity.setStatus(TaskActivity.Status.SUCCESSFUL);
            return result;
        } catch (Exception e) {
            CodeLocationUtil.setFailurePoint(taskActivity, e);
            throw e;
        } finally {
            taskActivity.setDuration(Duration.builder()
                    .startedOn(now)
                    .nanoSeconds(now.until(LocalDateTime.now(), ChronoUnit.NANOS))
                    .build());
            this.resume();
        }
    }

    public void addActivity(TaskActivity taskActivity) {
        taskActivity.setPrecedence(taskActivities.size() + 1);
        taskActivities.add(taskActivity);
    }

    public List<TaskActivity> getTaskActivities() {
        return Collections.unmodifiableList(taskActivities);
    }

    @Override
    public void setDescription(String description) {
        taskActivity.setDescription(description);
    }

    public void registerFailedTransaction(TaskTransactionContext taskTransactionContext) {
        failedTaskTransactionContexts.add(taskTransactionContext);
    }

    public void resume() {
        taskContextHolder.registerContext(this);
    }
}
