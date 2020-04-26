package com.olaleyeone.audittrail.impl;

import com.olaleyeone.audittrail.embeddable.Duration;
import com.olaleyeone.audittrail.entity.Task;
import com.olaleyeone.audittrail.entity.TaskActivity;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Data
@RequiredArgsConstructor
public class TaskContextImpl implements TaskContext {

    private final Task task;
    private final TaskActivity taskActivity;
    private final TaskContextHolder taskContextHolder;
    private final List<TaskActivity> taskActivities = new ArrayList<>();
    private final List<TaskTransactionContext> failedTaskTransactionContexts = new ArrayList<>();

    public TaskContextImpl(TaskActivity taskActivity, TaskContextHolder taskContextHolder) {
        this.taskActivity = taskActivity;
        this.task = taskActivity.getTask();
        this.taskContextHolder = taskContextHolder;
    }

    @Override
    public Task getTask() {
        return task;
    }

    @Override
    public Optional<TaskActivity> getTaskActivity() {
        return Optional.ofNullable(taskActivity);
    }

    @Override
    public <E> E doAndReturn(String name, Supplier<E> action) {
        return doAndReturn(name, null, action);
    }

    @Override
    public <E> E doAndReturn(String name, String description, Supplier<E> action) {
        LocalDateTime now = LocalDateTime.now();
        TaskActivity taskActivity = new TaskActivity();
        taskActivity.setTask(task);
        taskActivity.setParentActivity(this.taskActivity);
        taskActivity.setName(name);
        taskActivity.setDescription(description);
        taskActivity.setStatus(TaskActivity.Status.IN_PROGRESS);
        taskActivity.setPrecedence(taskActivities.size() + 1);

        return startActivity(taskActivity, action, now);
    }

    protected <E> E startActivity(TaskActivity taskActivity, Supplier<E> action, LocalDateTime now) {
        taskContextHolder.registerContext(new TaskContextImpl(taskActivity, taskContextHolder));

        E result;
        try {
            result = action.get();
            taskActivity.setStatus(TaskActivity.Status.SUCCESSFUL);
            return result;
        } catch (Exception e) {
            taskActivity.setStatus(TaskActivity.Status.FAILED);
            throw e;
        } finally {
            taskActivity.setDuration(Duration.builder()
                    .startedOn(now)
                    .nanoSeconds(now.until(LocalDateTime.now(), ChronoUnit.NANOS))
                    .build());
            taskActivities.add(taskActivity);
            this.resume();
        }
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
