package com.olaleyeone.audittrail.impl;

import com.olalayeone.audittrailtest.EntityTest;
import com.olaleyeone.audittrail.api.EntityOperation;
import com.olaleyeone.audittrail.api.EntityStateLogger;
import com.olaleyeone.audittrail.entity.Task;
import com.olaleyeone.audittrail.entity.AuditTrail;
import com.olaleyeone.audittrail.error.NoActivityLogException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
class AuditTrailLoggerTest extends EntityTest {

    private AuditTrailLogger auditTrailLogger;

    private AuditTrailLoggerDelegate auditTrailLoggerDelegate;
    private EntityStateLogger entityStateLogger;

    private Task task;

    @BeforeEach
    void setUp() {
        auditTrailLoggerDelegate = Mockito.mock(AuditTrailLoggerDelegate.class);
        entityStateLogger = Mockito.mock(EntityStateLogger.class);
        task = new Task();

        auditTrailLogger = new AuditTrailLogger(auditTrailLoggerDelegate, entityStateLogger) {

            @Override
            public Optional<Task> getTask() {
                return Optional.of(task);
            }
        };
        auditTrailLogger.getActivityLogger().log(faker.funnyName().name(), faker.backToTheFuture().quote());
    }

    @Test
    void shouldIgnoreNoUpdate() {
        Mockito.doReturn(Collections.EMPTY_LIST).when(entityStateLogger).getOperations();
        auditTrailLogger.beforeCommit(false);
        Mockito.verify(auditTrailLoggerDelegate, Mockito.never()).saveUnitOfWork(Mockito.any(), Mockito.any());
    }

    @Test
    void shouldSaveUpdates() {
        Mockito.doReturn(Collections.singletonList(Mockito.mock(EntityOperation.class))).when(entityStateLogger).getOperations();
        auditTrailLogger.beforeCommit(false);
        Mockito.verify(auditTrailLoggerDelegate, Mockito.times(1)).saveUnitOfWork(auditTrailLogger, AuditTrail.Status.SUCCESSFUL);
    }

    @Test
    void shouldNotSaveErrorAfterCommit() {
        auditTrailLogger.afterCompletion(TransactionSynchronization.STATUS_COMMITTED);
        Mockito.verify(auditTrailLoggerDelegate, Mockito.never()).saveFailure(Mockito.any(), Mockito.any());
    }

    @Test
    void shouldNotSaveErrorWhenNoActivityWasDone() {
        AuditTrailLogger auditTrailLogger = getUnitOfWorkLogger(false);
        auditTrailLogger.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
        Mockito.verify(auditTrailLoggerDelegate, Mockito.never()).saveFailure(Mockito.any(), Mockito.any());
    }

    @Test
    void shouldSaveErrorForRollback() {
        auditTrailLogger.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
        Mockito.verify(auditTrailLoggerDelegate, Mockito.times(1))
                .saveFailure(auditTrailLogger, AuditTrail.Status.ROLLED_BACK);
    }

    @Test
    void shouldSaveErrorForUnknown() {
        auditTrailLogger.afterCompletion(TransactionSynchronization.STATUS_UNKNOWN);
        Mockito.verify(auditTrailLoggerDelegate, Mockito.times(1))
                .saveFailure(auditTrailLogger, AuditTrail.Status.UNKNOWN);
    }

    @Test
    void shouldRequireActivityLogBeforeCommitByDefault() {
        AuditTrailLogger auditTrailLogger = new AuditTrailLogger(null) {
            @Override
            public Optional<Task> getTask() {
                return Optional.empty();
            }
        };
        assertFalse(auditTrailLogger.canCommitWithoutActivityLog());
    }

    @Test
    void shouldRequireActivityLogBeforeCommit() {
        AuditTrailLogger auditTrailLogger = getUnitOfWorkLogger(false);
        assertThrows(NoActivityLogException.class, () -> auditTrailLogger.beforeCommit(false));
    }

    @Test
    void shouldRequireActivityLogBeforeStateUpdate() {
        AuditTrailLogger auditTrailLogger = getUnitOfWorkLogger(false);
        assertThrows(NoActivityLogException.class, () -> auditTrailLogger.getEntityStateLogger());
    }

    @Test
    void shouldNotRequireActivityLog_IfCanCommitWithoutActivityLog() {
        AuditTrailLogger auditTrailLogger = getUnitOfWorkLogger(true);
        auditTrailLogger.beforeCommit(false);
    }

    @Test
    void shouldNotRequireActivityLogBeforeStateUpdate_IfCanCommitWithoutActivityLog() {
        AuditTrailLogger auditTrailLogger = getUnitOfWorkLogger(true);
        auditTrailLogger.getEntityStateLogger();
    }

    private AuditTrailLogger getUnitOfWorkLogger(boolean canCommitWithoutActivityLog) {
        return new AuditTrailLogger(auditTrailLoggerDelegate) {

            @Override
            public Optional<Task> getTask() {
                return Optional.of(task);
            }

            @Override
            public boolean canCommitWithoutActivityLog() {
                return canCommitWithoutActivityLog;
            }
        };
    }
}