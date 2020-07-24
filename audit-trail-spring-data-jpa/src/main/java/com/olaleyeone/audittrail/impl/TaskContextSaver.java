package com.olaleyeone.audittrail.impl;

import com.olaleyeone.audittrail.entity.Task;
import com.olaleyeone.audittrail.entity.TaskActivity;
import com.olaleyeone.audittrail.repository.TaskActivityRepository;
import com.olaleyeone.audittrail.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class TaskContextSaver {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final TaskActivityRepository taskActivityRepository;
    private final TaskRepository taskRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void save(TaskContextImpl taskContext) {
        saveTask(taskContext.getTask());

        saveFailedTransactions(taskContext);

        saveActivities(taskContext);
    }

    protected void saveTask(Task task) {
        if (task.getId() == null) {
            persistTask(task);
        } else {
            Optional<Task> savedTask = taskRepository.findById(task.getId());
            if (savedTask.isPresent()) {
                if (task.getWebRequest() != null) {
                    entityManager.merge(task.getWebRequest());
                }
                if (task.getFailure() != null && savedTask.get().getFailure() == null) {
                    entityManager.persist(task.getFailure());
                }
                entityManager.merge(task);
            } else {
                persistTask(task);
            }
        }
    }

    private void persistTask(Task task) {
        task.setId(null);
        if (task.getWebRequest() != null) {
            task.getWebRequest().setId(null);
            entityManager.persist(task.getWebRequest());
        }
        if (task.getFailure() != null) {
            task.getFailure().setId(null);
            entityManager.persist(task.getFailure());
        }
        entityManager.persist(task);
    }

    protected void saveFailedTransactions(TaskContextImpl taskContext) {//List<Long> generatedIds
        taskContext.getAllFailedTransactionStream().sequential().forEach(taskTransactionContext -> {
//            List<Long> activityIds = taskTransactionContext.getTaskActivities().stream().map(TaskActivity::getId)
//                    .collect(Collectors.toList());

//            if (!Collections.disjoint(generatedIds, activityIds)) {
//                return;
//            }
            taskTransactionContext.saveAfterFailure();
        });
    }

    protected void saveActivities(TaskContextImpl taskContext) {
        List<Long> generatedIds = taskContext.getActivityStream()
                .map(TaskActivity::getId)
                .filter(it -> it != null)
                .collect(Collectors.toList());
        Map<Long, TaskActivity> savedActivities = generatedIds.isEmpty()
                ? Collections.EMPTY_MAP
                : taskActivityRepository.findAllById(generatedIds)
                .parallelStream()
                .collect(Collectors.toMap(TaskActivity::getId, it -> it));

        taskContext.getTaskActivity().ifPresent(taskActivity -> saveTaskActivity(taskActivity, savedActivities));
        taskContext.getChildren().forEach(childContext -> saveChildContext(childContext, savedActivities));
    }

    private void saveChildContext(TaskContextImpl activityContext, Map<Long, TaskActivity> savedActivities) {
        TaskActivity taskActivity = activityContext.getTaskActivity().get();
        saveTaskActivity(taskActivity, savedActivities);
        activityContext.getChildren().forEach(childContext -> saveChildContext(childContext, savedActivities));
    }

    protected void saveTaskActivity(TaskActivity taskActivity, Map<Long, TaskActivity> savedActivities) {
        if (taskActivity.getTaskTransaction() != null) {
            return;
        }
        if (taskActivity.getId() == null) {
            persistTaskActivity(taskActivity);
        } else {
            TaskActivity savedCopy = savedActivities.get(taskActivity.getId());
            if (savedCopy == null) {
                taskActivity.setId(null);
                persistTaskActivity(taskActivity);
            } else if (savedCopy.getDuration().getNanoSecondsTaken() == null) {
                entityManager.merge(taskActivity);
            }
        }
    }

    private void persistTaskActivity(TaskActivity it) {
        entityManager.persist(it.getEntryPoint());
        if (it.getFailure() != null) {
            entityManager.persist(it.getFailure());
        }
        entityManager.persist(it);
    }

}
