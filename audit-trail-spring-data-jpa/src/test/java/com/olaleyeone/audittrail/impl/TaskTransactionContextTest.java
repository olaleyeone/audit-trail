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
import java.util.Optional;

@Transactional
class TaskTransactionContextTest extends EntityTest {

    private TaskTransactionContext taskTransactionContext;

    private TaskTransactionLogger taskTransactionLogger;
    private EntityStateLogger entityStateLogger;

    private TaskContext taskContext;

    @BeforeEach
    void setUp() {
        taskTransactionLogger = Mockito.mock(TaskTransactionLogger.class);
        entityStateLogger = Mockito.mock(EntityStateLogger.class);
        Task task = new Task();
        TaskActivity taskActivity = new TaskActivity();
        taskContext = new TaskContext() {
            @Override
            public Task getTask() {
                return task;
            }

            @Override
            public Optional<TaskActivity> getTaskActivity() {
                return Optional.of(taskActivity);
            }

            @Override
            public void logActivity(TaskActivity taskActivity) {

            }
        };

        taskTransactionContext = new TaskTransactionContext(taskTransactionLogger, entityStateLogger) {

            @Override
            public Task getTask() {
                return task;
            }

            @Override
            public TaskActivity getTaskActivity() {
                return taskActivity;
            }
        };

//        taskTransactionContext.getActivityLogger().log(faker.funnyName().name(), faker.backToTheFuture().quote());
    }

    @Test
    void shouldIgnoreNoUpdate() {
        Mockito.doReturn(Collections.EMPTY_LIST).when(entityStateLogger).getOperations();
        taskTransactionContext.beforeCommit(false);
        Mockito.verify(taskTransactionLogger, Mockito.never()).saveUnitOfWork(Mockito.any(), Mockito.any());
    }

    @Test
    void shouldSaveUpdates() {
        Mockito.doReturn(Collections.singletonList(Mockito.mock(EntityOperation.class))).when(entityStateLogger).getOperations();
        taskTransactionContext.beforeCommit(false);
        Mockito.verify(taskTransactionLogger, Mockito.times(1)).saveUnitOfWork(taskTransactionContext, TaskTransaction.Status.SUCCESSFUL);
    }

    @Test
    void shouldNotSaveErrorAfterCommit() {
        taskTransactionContext.afterCompletion(TransactionSynchronization.STATUS_COMMITTED);
        Mockito.verify(taskTransactionLogger, Mockito.never()).saveFailure(Mockito.any(), Mockito.any());
    }

    @Test
    void shouldNotSaveErrorWhenNoActivityWasDone() {
        TaskTransactionContext taskTransactionContext = getTaskTransactionContext(false);
        taskTransactionContext.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
        Mockito.verify(taskTransactionLogger, Mockito.never()).saveFailure(Mockito.any(), Mockito.any());
    }

    @Test
    void shouldSaveErrorForRollback() {
        taskTransactionContext.logActivity(new TaskActivity());
        taskTransactionContext.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
        Mockito.verify(taskTransactionLogger, Mockito.times(1))
                .saveFailure(taskTransactionContext, TaskTransaction.Status.ROLLED_BACK);
    }

    @Test
    void shouldSaveErrorForUnknown() {
        taskTransactionContext.logActivity(new TaskActivity());
        taskTransactionContext.afterCompletion(TransactionSynchronization.STATUS_UNKNOWN);
        Mockito.verify(taskTransactionLogger, Mockito.times(1))
                .saveFailure(taskTransactionContext, TaskTransaction.Status.UNKNOWN);
    }

//    @Test
//    void shouldRequireActivityLogBeforeCommitByDefault() {
//        TaskTransactionContext taskTransactionContext = new TaskTransactionContext(null) {
//            @Override
//            public Task getTask() {
//                return taskContext.getTask();
//            }
//
//            @Override
//            public Optional<TaskActivity> getTaskActivity() {
//                return taskContext.getTaskActivity();
//            }
//        };
//        assertFalse(taskTransactionContext.canCommitWithoutActivityLog());
//    }

//    @Test
//    void shouldRequireActivityLogBeforeCommit() {
//        TaskTransactionContext taskTransactionContext = getTaskTransactionContext(false);
//        assertThrows(NoActivityLogException.class, () -> taskTransactionContext.beforeCommit(false));
//    }

//    @Test
//    void shouldRequireActivityLogBeforeStateUpdate() {
//        TaskTransactionContext taskTransactionContext = getTaskTransactionContext(false);
//        assertThrows(NoActivityLogException.class, () -> taskTransactionContext.getEntityStateLogger());
//    }

    @Test
    void shouldNotRequireActivityLog_IfCanCommitWithoutActivityLog() {
        TaskTransactionContext taskTransactionContext = getTaskTransactionContext(true);
        taskTransactionContext.beforeCommit(false);
    }

    @Test
    void shouldNotRequireActivityLogBeforeStateUpdate_IfCanCommitWithoutActivityLog() {
        TaskTransactionContext taskTransactionContext = getTaskTransactionContext(true);
        taskTransactionContext.getEntityStateLogger();
    }

    private TaskTransactionContext getTaskTransactionContext(boolean canCommitWithoutActivityLog) {
        return new TaskTransactionContext(taskTransactionLogger) {

            @Override
            public Task getTask() {
                return taskContext.getTask();
            }

            @Override
            public TaskActivity getTaskActivity() {
                return taskContext.getTaskActivity().get();
            }
        };
    }
}