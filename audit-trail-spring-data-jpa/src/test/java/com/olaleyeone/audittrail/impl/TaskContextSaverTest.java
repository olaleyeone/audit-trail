package com.olaleyeone.audittrail.impl;

import com.olalayeone.audittrailtest.DataFactory;
import com.olalayeone.audittrailtest.EntityTest;
import com.olaleyeone.audittrail.api.EntityIdentifier;
import com.olaleyeone.audittrail.entity.Task;
import com.olaleyeone.audittrail.entity.TaskActivity;
import com.olaleyeone.audittrail.entity.WebRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskContextSaverTest extends EntityTest {

    private TaskTransactionContextFactory taskTransactionContextFactory;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private DataFactory dataFactory;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private TaskContextSaver taskContextSaver;

    private TaskContextHolder taskContextHolder;

    @BeforeEach
    void setUp() {
        taskContextHolder = new TaskContextHolder();
        taskContextSaver = applicationContext.getAutowireCapableBeanFactory().createBean(TaskContextSaver.class);
        taskTransactionContextFactory = new TaskTransactionContextFactory(taskContextHolder);
        applicationContext.getAutowireCapableBeanFactory().autowireBean(taskTransactionContextFactory);
        taskTransactionContextFactory.init();
    }

    @Transactional
    @Test
    void saveTask() {
        Task task = dataFactory.getTask(false);
        taskContextSaver.saveTask(task);
        assertNotNull(task.getId());
    }

    @Transactional
    @Test
    void saveTaskInLimbo() {
        WebRequest webRequest = new WebRequest();
        webRequest.setUri(faker.internet().url());
        webRequest.setId(1L);
        Task task = dataFactory.getTask(false);
        task.setId(20L);
        task.setWebRequest(webRequest);

        taskContextSaver.saveTask(task);
        assertNotNull(task.getId());
        assertNotNull(task.getWebRequest().getId());
    }

    @Transactional
    @Test
    void saveTaskAlreadySaved() {
        Task task = dataFactory.getTask(true);
        entityManager.detach(task);
        long nanoSeconds = faker.number().randomNumber();
        task.getDuration().setNanoSecondsTaken(nanoSeconds);

        Task dbValue = entityManager.find(Task.class, task.getId());
        assertNull(dbValue.getDuration().getNanoSecondsTaken());

        taskContextSaver.saveTask(task);
        entityManager.flush();
        entityManager.refresh(dbValue);
        assertNotNull(dbValue.getId());
    }

    @Transactional
    @Test
    void saveTaskActivity() {
        TaskActivity taskActivity = dataFactory.getTaskActivity(false);
        taskContextSaver.saveTaskActivity(taskActivity, Collections.EMPTY_MAP);
        assertNotNull(taskActivity.getId());
    }

    @Transactional
    @Test
    void saveTaskActivityInLimbo() {
        TaskActivity taskActivity = dataFactory.getTaskActivity(false);
        taskActivity.setId(20L);
        taskContextSaver.saveTaskActivity(taskActivity, Collections.EMPTY_MAP);
        assertNotNull(taskActivity.getId());
    }

    @Transactional
    @Test
    void saveTaskActivityPartiallySaved() {
        TaskActivity taskActivity = dataFactory.getTaskActivity(true);
        entityManager.detach(taskActivity);
        TaskActivity dvValue = entityManager.find(TaskActivity.class, taskActivity.getId());

        long nanoSeconds = faker.number().randomNumber();
        taskActivity.getDuration().setNanoSecondsTaken(nanoSeconds);
        assertNull(dvValue.getDuration().getNanoSecondsTaken());

        taskContextSaver.saveTaskActivity(taskActivity, Collections.singletonMap(taskActivity.getId(),
                dvValue));
        entityManager.flush();
        entityManager.refresh(dvValue);
        assertEquals(nanoSeconds, dvValue.getDuration().getNanoSecondsTaken());
    }

    @Transactional
    @Test
    void saveTaskActivityFullySaved() {
        TaskActivity taskActivity = dataFactory.getTaskActivity(true);
        entityManager.detach(taskActivity);
        TaskActivity dvValue = entityManager.find(TaskActivity.class, taskActivity.getId());

        long nanoSeconds = faker.number().randomNumber();
        taskActivity.getDuration().setNanoSecondsTaken(nanoSeconds);

        dvValue.getDuration().setNanoSecondsTaken(nanoSeconds + 1);

        taskContextSaver.saveTaskActivity(taskActivity, Collections.singletonMap(taskActivity.getId(),
                dvValue));
        entityManager.flush();
        entityManager.refresh(dvValue);
        assertEquals(nanoSeconds + 1, dvValue.getDuration().getNanoSecondsTaken());
    }

    @Test
    void saveTaskWithChildren() {
        TaskActivity taskActivity = dataFactory.getTaskActivity(false);
        TaskContextImpl taskContext = new TaskContextImpl(taskActivity, taskContextHolder, taskTransactionContextFactory);
        taskContext.start(null);

        List<TaskActivity> taskActivities = taskContext.executeAndReturn("1", () -> {
            TaskContextImpl taskContext1 = taskContextHolder.getObject();
            TaskActivity taskActivity1 = taskContext1.getTaskActivity().get();
            TaskActivity taskActivity2 = taskContext1.executeAndReturn("1A", () -> taskContextHolder.getObject().getTaskActivity().get());
            return Arrays.asList(taskActivity1, taskActivity2);
        });

        transactionTemplate.execute(status -> {
            taskContextSaver.save(taskContext);
            assertNotNull(taskActivity.getId());
            assertEquals(2, taskActivities.size());
            taskActivities.forEach(it -> assertNotNull(it.getId()));
            status.setRollbackOnly();
            return null;
        });
    }

    @Transactional
    @Test
    void saveTransactionalTaskWithChildren() {
        TaskActivity taskActivity = dataFactory.getTaskActivity(true);
        Task task = taskActivity.getTask();
        TaskContextImpl rootContext = new TaskContextImpl(task, null, taskContextHolder, taskTransactionContextFactory);
        TaskContextImpl taskContext = new TaskContextImpl(taskActivity, taskContextHolder, taskTransactionContextFactory);
        taskContext.start(rootContext);
        TaskTransactionContext taskTransactionContext = taskTransactionContextFactory.getObject();
        assertEquals(1, taskTransactionContext.getTaskActivities().size());
        taskTransactionContext.getEntityStateLogger().registerDeletedEntity(new EntityIdentifier(Object.class, Object.class.getSimpleName(), 1));
        taskTransactionContext.beforeCommit(false);
        assertNotNull(taskActivity.getTaskTransaction());

        taskContextSaver.save(rootContext);
        assertNotNull(task.getId());
    }

    @Test
    void saveWithFailedTransaction() {
        TaskActivity taskActivity = dataFactory.getTaskActivity(false);
        TaskContextImpl taskContext = new TaskContextImpl(taskActivity, taskContextHolder, taskTransactionContextFactory);
        taskContext.start(null);

        TaskActivity taskActivity1 = transactionTemplate.execute(status -> {
            status.setRollbackOnly();
            TaskTransactionContext taskTransactionContext = taskTransactionContextFactory.getObject();
            EntityIdentifier entityIdentifier = new EntityIdentifier(String.class, String.class.getSimpleName(), 1);
            taskTransactionContext.getEntityStateLogger().registerDeletedEntity(entityIdentifier);
            return taskContext.executeAndReturn("1", () -> taskContextHolder.getObject().getTaskActivity().get());
        });
        transactionTemplate.execute(status -> {
            taskContextSaver.save(taskContext);
            assertNotNull(taskActivity.getId());
            assertNotNull(taskActivity1.getId());
            assertNotNull(taskActivity1.getTaskTransaction());
            status.setRollbackOnly();
            return null;
        });
    }
}