package com.olaleyeone.audittrail.impl;

import com.olaleyeone.audittrail.entity.Task;
import com.olaleyeone.audittrail.entity.TaskActivity;
import com.olaleyeone.audittrail.repository.TaskActivityRepository;
import com.olaleyeone.audittrail.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class TaskContextSaver {

    private final TaskActivityRepository taskActivityRepository;
    private final TaskRepository taskRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void save(TaskContextImpl taskContext) {
        saveTask(taskContext.getTask());
        saveActivities(taskContext);
    }

    protected void saveTask(Task task) {
        if (task.getId() == null) {
            entityManager.persist(task);
        } else {
            Optional<Task> savedTask = taskRepository.findById(task.getId());
            if (savedTask.isPresent()) {
                entityManager.merge(task);
            } else {
                task.setId(null);
                entityManager.persist(task);
            }
        }
    }

    protected void saveActivities(TaskContextImpl taskContext) {
        List<Long> generatedIds = taskContext.getActivityStream()
                .map(TaskActivity::getId)
                .collect(Collectors.toList());
        Map<Long, TaskActivity> savedActivities = taskActivityRepository.findAllById(generatedIds)
                .parallelStream()
                .collect(Collectors.toMap(TaskActivity::getId, it -> it));

        taskContext.getTaskActivity().ifPresent(taskActivity -> saveTaskActivity(taskActivity, savedActivities));

        taskContext.getChildren().forEach(childContext -> saveChildContext(childContext, savedActivities));
    }

    private void saveChildContext(TaskContextImpl activityContext, Map<Long, TaskActivity> savedActivities) {
        TaskActivity taskActivity = activityContext.getTaskActivity().get();
        if (taskActivity.getTaskTransaction() != null) {
            return;
        }
        saveTaskActivity(taskActivity, savedActivities);
        activityContext.getChildren().forEach(childContext -> saveChildContext(childContext, savedActivities));
    }

    protected void saveTaskActivity(TaskActivity taskActivity, Map<Long, TaskActivity> savedActivities) {
        if (taskActivity.getId() == null) {
            entityManager.persist(taskActivity);
        } else {
            TaskActivity savedCopy = savedActivities.get(taskActivity.getId());
            if (savedCopy == null) {
                taskActivity.setId(null);
                entityManager.persist(taskActivity);
            } else if (savedCopy.getDuration().getNanoSeconds() == null) {
                entityManager.merge(taskActivity);
            }
        }
    }

}
