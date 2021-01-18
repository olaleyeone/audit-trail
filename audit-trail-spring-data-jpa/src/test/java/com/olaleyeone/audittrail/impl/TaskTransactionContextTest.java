package com.olaleyeone.audittrail.impl;

import com.olalayeone.audittrailtest.EntityTest;
import com.olaleyeone.audittrail.api.EntityOperation;
import com.olaleyeone.audittrail.api.EntityStateLogger;
import com.olaleyeone.audittrail.embeddable.Duration;
import com.olaleyeone.audittrail.entity.Task;
import com.olaleyeone.audittrail.entity.TaskActivity;
import com.olaleyeone.audittrail.entity.TaskTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;

import java.time.OffsetDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

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

        TaskTransaction taskTransaction = new TaskTransaction();
        taskTransaction.setStatus(TaskTransaction.Status.COMMITTED);
        taskTransaction.setDuration(Duration.builder()
                .startedAt(OffsetDateTime.now())
                .build());
        Mockito.doReturn(taskTransaction).when(taskTransactionLogger).createTaskTransaction(Mockito.any(), Mockito.any());

        taskTransactionContext = new TaskTransactionContext(taskContext, taskTransactionLogger, entityStateLogger);
        assertNotNull(taskTransactionContext.getTaskTransaction());
    }

    @Test
    void getEntityStateLogger() {
        assertSame(entityStateLogger, taskTransactionContext.getEntityStateLogger());
    }

    @Test
    void shouldNotIgnoreNoUpdate() {
        Mockito.doReturn(Collections.EMPTY_LIST).when(entityStateLogger).getOperations();
        taskTransactionContext.beforeCommit(false);
        Mockito.verify(taskTransactionLogger, Mockito.times(1)).saveTaskTransaction(Mockito.any());
    }

    @Test
    void shouldSaveUpdates() {
        Mockito.doReturn(Collections.singletonList(Mockito.mock(EntityOperation.class))).when(entityStateLogger).getOperations();
        taskTransactionContext.beforeCommit(false);
        Mockito.verify(taskTransactionLogger, Mockito.times(1))
                .saveTaskTransaction(taskTransactionContext);
    }

    @Test
    void shouldNotSaveErrorAfterCommit() {
        taskTransactionContext.afterCompletion(TransactionSynchronization.STATUS_COMMITTED);
        assertEquals(TaskTransaction.Status.COMMITTED, taskTransactionContext.getTaskTransaction().getStatus());
        Mockito.verify(taskContext, Mockito.never()).registerFailedTransaction(taskTransactionContext);
        Mockito.verify(taskContext, Mockito.never()).end();

        assertThrows(IllegalStateException.class, () -> taskTransactionContext.saveAfterFailure());
    }

    @Test
    void shouldNotSaveErrorWhenNoActivityWasDone() {
        taskTransactionContext.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
        assertEquals(TaskTransaction.Status.ROLLED_BACK, taskTransactionContext.getTaskTransaction().getStatus());
        Mockito.verify(taskContext, Mockito.never()).registerFailedTransaction(taskTransactionContext);
        Mockito.verify(taskContext, Mockito.never()).end();
    }

    @Test
    void shouldSaveErrorForRollback() {
        taskTransactionContext.addActivity(new TaskActivity());
        taskTransactionContext.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
        assertEquals(TaskTransaction.Status.ROLLED_BACK, taskTransactionContext.getTaskTransaction().getStatus());
        Mockito.verify(taskContext, Mockito.times(1)).registerFailedTransaction(taskTransactionContext);
        Mockito.verify(taskContext, Mockito.never()).end();
    }

    @Test
    void shouldSaveErrorForUnknown() {
        taskTransactionContext.addActivity(new TaskActivity());
        taskTransactionContext.afterCompletion(TransactionSynchronization.STATUS_UNKNOWN);
        assertEquals(TaskTransaction.Status.UNKNOWN, taskTransactionContext.getTaskTransaction().getStatus());
        Mockito.verify(taskContext, Mockito.times(1)).registerFailedTransaction(taskTransactionContext);
        Mockito.verify(taskContext, Mockito.never()).end();
    }
}