package com.olaleyeone.audittrail.impl;

import com.olalayeone.audittrailtest.EntityTest;
import com.olaleyeone.audittrail.api.EntityOperation;
import com.olaleyeone.audittrail.api.EntityStateLogger;
import com.olaleyeone.audittrail.entity.Task;
import com.olaleyeone.audittrail.entity.TaskActivity;
import com.olaleyeone.audittrail.entity.TaskTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

@Transactional
class TaskTransactionContextTest extends EntityTest {

    private TaskTransactionContext taskTransactionContext;

    private TaskTransactionLogger taskTransactionLogger;
    private EntityStateLogger entityStateLogger;

    private TaskContextImpl taskContext;

    @BeforeEach
    void setUp() {
        taskTransactionLogger = Mockito.mock(TaskTransactionLogger.class);
        entityStateLogger = Mockito.mock(EntityStateLogger.class);
        TaskActivity taskActivity = new TaskActivity();
        taskActivity.setTask(new Task());
        taskContext = Mockito.spy(new TaskContextImpl(taskActivity, null, null));
        Mockito.doNothing().when(taskContext).end();

        taskTransactionContext = new TaskTransactionContext(taskContext, taskTransactionLogger, entityStateLogger);
    }

    @Test
    void getEntityStateLogger() {
        assertSame(entityStateLogger, taskTransactionContext.getEntityStateLogger());
    }

    @Test
    void shouldIgnoreNoUpdate() {
        Mockito.doReturn(Collections.EMPTY_LIST).when(entityStateLogger).getOperations();
        taskTransactionContext.beforeCommit(false);
        Mockito.verify(taskTransactionLogger, Mockito.never()).saveTaskTransaction(Mockito.any(), Mockito.any());
    }

    @Test
    void shouldSaveUpdates() {
        Mockito.doReturn(Collections.singletonList(Mockito.mock(EntityOperation.class))).when(entityStateLogger).getOperations();
        taskTransactionContext.beforeCommit(false);
        Mockito.verify(taskTransactionLogger, Mockito.times(1))
                .saveTaskTransaction(taskTransactionContext, TaskTransaction.Status.COMMITTED);
    }

    @Test
    void shouldNotSaveErrorAfterCommit() {
        taskTransactionContext.afterCompletion(TransactionSynchronization.STATUS_COMMITTED);
        assertEquals(TaskTransaction.Status.COMMITTED, taskTransactionContext.getStatus());
        Mockito.verify(taskContext, Mockito.never()).registerFailedTransaction(taskTransactionContext);
        Mockito.verify(taskContext, Mockito.never()).end();
    }

    @Test
    void shouldNotSaveErrorWhenNoActivityWasDone() {
        taskTransactionContext.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
        assertEquals(TaskTransaction.Status.ROLLED_BACK, taskTransactionContext.getStatus());
        Mockito.verify(taskContext, Mockito.never()).registerFailedTransaction(taskTransactionContext);
        Mockito.verify(taskContext, Mockito.never()).end();
    }

    @Test
    void shouldSaveErrorForRollback() {
        taskTransactionContext.addActivity(new TaskActivity());
        taskTransactionContext.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
        assertEquals(TaskTransaction.Status.ROLLED_BACK, taskTransactionContext.getStatus());
        Mockito.verify(taskContext, Mockito.times(1)).registerFailedTransaction(taskTransactionContext);
        Mockito.verify(taskContext, Mockito.never()).end();
    }

    @Test
    void shouldSaveErrorForUnknown() {
        taskTransactionContext.addActivity(new TaskActivity());
        taskTransactionContext.afterCompletion(TransactionSynchronization.STATUS_UNKNOWN);
        assertEquals(TaskTransaction.Status.UNKNOWN, taskTransactionContext.getStatus());
        Mockito.verify(taskContext, Mockito.times(1)).registerFailedTransaction(taskTransactionContext);
        Mockito.verify(taskContext, Mockito.never()).end();
    }
}