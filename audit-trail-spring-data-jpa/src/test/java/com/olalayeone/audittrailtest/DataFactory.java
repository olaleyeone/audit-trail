package com.olalayeone.audittrailtest;

import com.github.javafaker.Faker;
import com.olaleyeone.audittrail.api.OperationType;
import com.olaleyeone.audittrail.embeddable.Duration;
import com.olaleyeone.audittrail.entity.EntityState;
import com.olaleyeone.audittrail.entity.Task;
import com.olaleyeone.audittrail.entity.TaskActivity;
import com.olaleyeone.audittrail.entity.TaskTransaction;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.Random;

@Component
public class DataFactory {

    protected final Faker faker = Faker.instance(new Random());

    @PersistenceContext
    private EntityManager entityManager;

    public Task getTask(boolean persist) {
        Task task = new Task();
        task.setDuration(new Duration(LocalDateTime.now(), null));
        task.setName(faker.funnyName().name());
        task.setType(faker.app().name());
        if (persist) {
            entityManager.persist(task);
        }
        return task;
    }

    public TaskActivity getTaskActivity(boolean persist) {
        TaskActivity taskActivity = new TaskActivity();
        taskActivity.setTask(getTask(persist));
        taskActivity.setName(faker.lordOfTheRings().character());
        taskActivity.setPrecedence(1);
        taskActivity.setDuration(new Duration(LocalDateTime.now(), null));
        taskActivity.setStatus(TaskActivity.Status.IN_PROGRESS);
        if (persist) {
            entityManager.persist(taskActivity);
        }
        return taskActivity;
    }

    public TaskTransaction createTaskTransaction(boolean persist) {
        TaskTransaction taskTransaction = new TaskTransaction();
        TaskActivity taskActivity = getTaskActivity(persist);
        taskTransaction.setTaskActivity(taskActivity);
        taskTransaction.setTask(taskActivity.getTask());

        taskTransaction.setDuration(Duration.builder()
                .startedOn(LocalDateTime.now())
                .nanoSeconds(faker.number().randomNumber())
                .build());

        taskTransaction.setStatus(TaskTransaction.Status.COMMITTED);
        if (persist) {
            entityManager.persist(taskTransaction);
        }
        return taskTransaction;
    }

    public EntityState createEntityState(boolean persist) {
        EntityState entityState = new EntityState();
        entityState.setTaskTransaction(createTaskTransaction(persist));
        entityState.setOperationType(OperationType.CREATE);
        entityState.setEntityName(faker.funnyName().name());
        entityState.setEntityId(faker.number().digit());
        if (persist) {
            entityManager.persist(entityState);
        }
        return entityState;
    }
}
