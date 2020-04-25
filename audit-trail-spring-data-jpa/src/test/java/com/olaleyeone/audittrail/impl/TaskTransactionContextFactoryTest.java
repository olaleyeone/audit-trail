package com.olaleyeone.audittrail.impl;

import com.olalayeone.audittrailtest.EntityTest;
import com.olaleyeone.audittrail.entity.Task;
import com.olaleyeone.audittrail.entity.TaskActivity;
import com.olaleyeone.audittrail.error.NoTaskActivityException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Provider;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TaskTransactionContextFactoryTest extends EntityTest {

    @Autowired
    private Provider<TaskTransactionContext> auditTrailLoggerProvider;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private TaskContext taskContext;

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Test
    void beforeCommit() {
        TaskTransactionContext taskTransactionContext = transactionTemplate.execute(status -> auditTrailLoggerProvider.get());
        Mockito.verify(taskTransactionContext, Mockito.times(1))
                .beforeCommit(Mockito.anyBoolean());
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Test
    void shouldCreateNewInstanceForEachTransaction() {
        TaskTransactionContext taskTransactionContext1 = transactionTemplate.execute(status -> auditTrailLoggerProvider.get());
        TaskTransactionContext taskTransactionContext2 = transactionTemplate.execute(status -> auditTrailLoggerProvider.get());
        assertNotSame(taskTransactionContext1, taskTransactionContext2);
    }

    @Transactional
    @Test
    void shouldUseOneInstancePerTransaction() {
        TaskTransactionContext taskTransactionContext1 = auditTrailLoggerProvider.get();
        TaskTransactionContext taskTransactionContext2 = transactionTemplate.execute(status -> auditTrailLoggerProvider.get());
        assertSame(taskTransactionContext1, taskTransactionContext2);
    }

    @Test
    void shouldRequireTaskActivity() {
        Mockito.doReturn(Optional.empty()).when(taskContext).getTaskActivity();
        TaskTransactionContextFactory taskTransactionContextFactory = new TaskTransactionContextFactory();
        applicationContext.getAutowireCapableBeanFactory().autowireBean(taskTransactionContextFactory);
        assertThrows(NoTaskActivityException.class, () -> taskTransactionContextFactory.createTaskTransactionContext(null));
    }

    @Test
    void testCreateLogger() {
        Task task = new Task();
        TaskActivity taskActivity = new TaskActivity();
        taskActivity.setTask(new Task());

        Mockito.doReturn(task).when(taskContext).getTask();
        Mockito.doReturn(Optional.of(taskActivity)).when(taskContext).getTaskActivity();

        TaskTransactionContextFactory taskTransactionContextFactory = new TaskTransactionContextFactory();
        applicationContext.getAutowireCapableBeanFactory().autowireBean(taskTransactionContextFactory);

        TaskTransactionContext taskTransactionContext = taskTransactionContextFactory.createTaskTransactionContext(null);

        assertSame(taskActivity, taskTransactionContext.getTaskActivity());
        assertSame(taskActivity.getTask(), taskTransactionContext.getTask());
        assertNotSame(task, taskTransactionContext.getTask());
    }
}