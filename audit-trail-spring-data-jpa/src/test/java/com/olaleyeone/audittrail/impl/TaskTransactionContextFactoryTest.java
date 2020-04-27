package com.olaleyeone.audittrail.impl;

import com.olalayeone.audittrailtest.EntityTest;
import com.olaleyeone.audittrail.entity.Task;
import com.olaleyeone.audittrail.entity.TaskActivity;
import com.olaleyeone.audittrail.error.NoTaskActivityException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Provider;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TaskTransactionContextFactoryTest extends EntityTest {

    @Autowired
    private Provider<TaskTransactionContext> taskTransactionContextProvider;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private TaskContextImpl taskContext;

    private TaskContextHolder taskContextHolder;
    private TaskTransactionContextFactory taskTransactionContextFactory;
    private TaskActivity taskActivity;
    private TaskTransactionLogger taskTransactionLogger;

    @BeforeEach
    public void setUp() {
        taskContextHolder = new TaskContextHolder();
        TaskTransactionContextFactory taskTransactionContextFactory = new TaskTransactionContextFactory(taskContextHolder);
        applicationContext.getAutowireCapableBeanFactory().autowireBean(taskTransactionContextFactory);
        taskTransactionContextFactory.init();
        taskTransactionLogger = taskTransactionContextFactory.getTaskTransactionLogger();
        this.taskTransactionContextFactory = Mockito.spy(taskTransactionContextFactory);

        taskActivity = new TaskActivity();
        taskActivity.setTask(new Task());

        Mockito.doReturn(taskActivity.getTask()).when(taskContext).getTask();
        Mockito.doReturn(Optional.of(taskActivity)).when(taskContext).getTaskActivity();
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Test
    void beforeCommit() {
        TaskTransactionContext taskTransactionContext = transactionTemplate.execute(status -> taskTransactionContextProvider.get());
        Mockito.verify(taskTransactionContext, Mockito.times(1))
                .beforeCommit(Mockito.anyBoolean());
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Test
    void shouldCreateNewInstanceForEachTransaction() {
        TaskTransactionContext taskTransactionContext1 = transactionTemplate.execute(status -> taskTransactionContextProvider.get());
        TaskTransactionContext taskTransactionContext2 = transactionTemplate.execute(status -> taskTransactionContextProvider.get());
        assertNotSame(taskTransactionContext1, taskTransactionContext2);
    }

    @Transactional
    @Test
    void shouldUseOneInstancePerTransaction() {
        TaskTransactionContext taskTransactionContext1 = taskTransactionContextProvider.get();
        TaskTransactionContext taskTransactionContext2 = transactionTemplate.execute(status -> taskTransactionContextProvider.get());
        assertSame(taskTransactionContext1, taskTransactionContext2);
    }

    @Transactional
    @Test
    void initializeInTransaction() {
        assertTrue(TransactionSynchronizationManager.getSynchronizations().isEmpty());
        taskTransactionContextFactory.initialize();
        assertFalse(TransactionSynchronizationManager.getSynchronizations().isEmpty());
    }

    @Test
    void initializeOutsideTransactionShouldBeSilent() {
        taskTransactionContextFactory.initialize();
    }

    @Test
    void shouldRequireTaskActivity() {
        taskContextHolder.registerContext(taskContext);
        Mockito.doReturn(Optional.empty()).when(taskContext).getTaskActivity();
        assertThrows(NoTaskActivityException.class, () -> taskTransactionContextFactory.createTaskTransactionContext(taskTransactionLogger));
    }

    @Test
    void testCreateTaskTransactionContext() {
        taskContextHolder.registerContext(taskContext);

        TaskTransactionContext taskTransactionContext = taskTransactionContextFactory.createTaskTransactionContext(taskTransactionLogger);

        assertSame(taskContext.getTaskActivity().get(), taskTransactionContext.getTaskActivity());
        assertSame(taskContext.getTask(), taskTransactionContext.getTask());
    }

    @Test
    public void joinAvailableTransaction() {
        TaskContextImpl taskContext = new TaskContextImpl(taskActivity, taskContextHolder, taskTransactionContextFactory);
        taskContext.start(null);
        transactionTemplate.execute(status -> {
            TaskTransactionContext taskTransactionContext = taskTransactionContextFactory.getObject();
            taskContext.execute(faker.lordOfTheRings().location(), () -> null);

            assertEquals(2, taskContext.getTaskActivities().size());
            assertEquals(1, taskTransactionContext.getTaskActivities().size());
            return null;
        });
    }
}