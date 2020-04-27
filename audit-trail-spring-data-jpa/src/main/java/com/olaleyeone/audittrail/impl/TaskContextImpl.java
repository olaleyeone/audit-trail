package com.olaleyeone.audittrail.impl;

import com.olaleyeone.audittrail.context.Action;
import com.olaleyeone.audittrail.context.TaskContext;
import com.olaleyeone.audittrail.entity.CodeContext;
import com.olaleyeone.audittrail.entity.Task;
import com.olaleyeone.audittrail.entity.TaskActivity;
import com.olaleyeone.audittrail.error.NoTaskActivityException;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Getter(AccessLevel.NONE)
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

    @SneakyThrows
    @Override
    public <E> E execute(String name, Action<E> action) {
        TaskActivity taskActivity = createTaskActivity(name, null);
        return ActivityRunner.startActivity(this, taskActivity, action);
    }

    @SneakyThrows
    @Override
    public <E> E execute(String name, String description, Action<E> action) {
        TaskActivity taskActivity = createTaskActivity(name, description);
        return ActivityRunner.startActivity(this, taskActivity, action);
    }

    @Override
    public void setDescription(String description) {
        if (taskActivity != null) {
            taskActivity.setDescription(description);
            return;
        }
        task.setDescription(description);
    }

    public Task getTask() {
        return task;
    }

    public Optional<TaskActivity> getTaskActivity() {
        return Optional.ofNullable(taskActivity);
    }

    public List<TaskActivity> getTaskActivities() {
        return getActivityStream().collect(Collectors.toList());
    }

    public Stream<TaskActivity> getActivityStream() {
        Stream<TaskActivity> children = this.children.parallelStream()
                .flatMap(it -> it.getActivityStream());
        if (taskActivity == null) {
            return children;
        }
        return Stream.concat(Stream.of(taskActivity), children);
    }

    public Stream<TaskTransactionContext> getAllFailedTransactionStream() {
        Stream<TaskTransactionContext> children = this.children.parallelStream()
                .flatMap(it -> it.getAllFailedTransactionStream());
        return Stream.concat(failedTaskTransactionContexts.stream(), children);
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

    public void end() {
        taskContextHolder.registerContext(parent.orElse(null));
    }

    private void addChild(TaskContextImpl taskContext) {
        children.add(taskContext);
        TaskActivity taskActivity = taskContext.getTaskActivity().orElseThrow(NoTaskActivityException::new);
        taskActivity.setPrecedence(taskActivities.size() + 1);
        taskActivities.add(taskActivity);
    }

    private TaskActivity createTaskActivity(String name, String description) {
        TaskActivity taskActivity = new TaskActivity();
        taskActivity.setTask(task);
        taskActivity.setParentActivity(this.taskActivity);
        taskActivity.setName(name);
        taskActivity.setDescription(description);
        taskActivity.setStatus(TaskActivity.Status.IN_PROGRESS);

        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
        CodeContext entryPoint = CodeContextUtil.getEntryPoint(stackTraceElement);
        taskActivity.setEntryPoint(entryPoint);

        return taskActivity;
    }
}
