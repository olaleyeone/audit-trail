package com.olaleyeone.audittrail.impl;

import com.olaleyeone.audittrail.advice.CodeLocationUtil;
import com.olaleyeone.audittrail.context.TaskContext;
import com.olaleyeone.audittrail.embeddable.Duration;
import com.olaleyeone.audittrail.entity.CodeInstruction;
import com.olaleyeone.audittrail.entity.Task;
import com.olaleyeone.audittrail.entity.TaskActivity;
import com.olaleyeone.audittrail.error.NoTaskActivityException;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Task task;
    private final TaskActivity taskActivity;
    private final TaskContextHolder taskContextHolder;
    private final TaskTransactionContextFactory taskTransactionContextFactory;

    @Setter(AccessLevel.NONE)
    private Optional<TaskContextImpl> parent;

    @Getter(value = AccessLevel.NONE)
    private final List<TaskActivity> taskActivities = new ArrayList<>();

    private final List<TaskContextImpl> children = new ArrayList<>();
    private final List<TaskTransactionContext> failedTaskTransactionContexts = new ArrayList<>();

    public TaskContextImpl(
            TaskActivity taskActivity,
            TaskContextHolder taskContextHolder,
            TaskTransactionContextFactory taskTransactionContextFactory) {
        this.taskActivity = taskActivity;
        this.task = taskActivity.getTask();
        this.taskContextHolder = taskContextHolder;
        this.taskTransactionContextFactory = taskTransactionContextFactory;
    }

    public Task getTask() {
        return task;
    }

    public Optional<TaskActivity> getTaskActivity() {
        return Optional.ofNullable(taskActivity);
    }

    @Override
    public void setDescription(String description) {
        taskActivity.setDescription(description);
    }

    @Override
    public <E> E execute(String name, Supplier<E> action) {
        TaskActivity taskActivity = createTaskActivity(name, null);
        return startActivity(taskActivity, action);
    }

    @Override
    public <E> E execute(String name, String description, Supplier<E> action) {
        TaskActivity taskActivity = createTaskActivity(name, description);
        return startActivity(taskActivity, action);
    }

    protected <E> E startActivity(TaskActivity taskActivity, Supplier<E> action) {

        TaskContextImpl taskContext = new TaskContextImpl(taskActivity, taskContextHolder, taskTransactionContextFactory);
        taskContext.start(this);
        LocalDateTime now = LocalDateTime.now();

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
            taskContext.end();
        }
    }

    public List<TaskActivity> getTaskActivities() {
        return Collections.unmodifiableList(taskActivities);
    }

    public void registerFailedTransaction(TaskTransactionContext taskTransactionContext) {
        failedTaskTransactionContexts.add(taskTransactionContext);
    }

    public void start(TaskContextImpl parent) {
        this.parent = Optional.ofNullable(parent);
        if (parent != null) {
            parent.addChild(this);
        }
        taskContextHolder.registerContext(this);
        taskTransactionContextFactory.joinAvailableTransaction(taskActivity);
    }

    private void addChild(TaskContextImpl taskContext) {
        children.add(taskContext);
        TaskActivity taskActivity = taskContext.getTaskActivity().orElseThrow(NoTaskActivityException::new);
        taskActivity.setPrecedence(taskActivities.size() + 1);
        taskActivities.add(taskActivity);
    }

    public void end() {
        taskContextHolder.registerContext(parent.orElse(null));
    }

    private TaskActivity createTaskActivity(String name, String description) {
        TaskActivity taskActivity = new TaskActivity();
        taskActivity.setTask(task);
        taskActivity.setParentActivity(this.taskActivity);
        taskActivity.setName(name);
        taskActivity.setDescription(description);
        taskActivity.setStatus(TaskActivity.Status.IN_PROGRESS);

        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[2];
        CodeInstruction entryPoint = CodeLocationUtil.getEntryPoint(stackTraceElement);
        taskActivity.setEntryPoint(entryPoint);

        return taskActivity;
    }
}
