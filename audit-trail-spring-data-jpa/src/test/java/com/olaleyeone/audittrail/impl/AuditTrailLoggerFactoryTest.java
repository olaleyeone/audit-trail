package com.olaleyeone.audittrail.impl;

import com.olalayeone.audittrailtest.EntityTest;
import com.olaleyeone.audittrail.entity.Task;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Provider;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AuditTrailLoggerFactoryTest extends EntityTest {

    @Autowired
    private Provider<AuditTrailLogger> auditTrailLoggerProvider;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Test
    void beforeCommit() {
        AuditTrailLogger auditTrailLogger = transactionTemplate.execute(status -> auditTrailLoggerProvider.get());
        Mockito.verify(auditTrailLogger, Mockito.times(1))
                .beforeCommit(Mockito.anyBoolean());
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Test
    void shouldCreateNewInstanceForEachTransaction() {
        AuditTrailLogger auditTrailLogger1 = transactionTemplate.execute(status -> auditTrailLoggerProvider.get());
        AuditTrailLogger auditTrailLogger2 = transactionTemplate.execute(status -> auditTrailLoggerProvider.get());
        assertNotSame(auditTrailLogger1, auditTrailLogger2);
    }

    @Transactional
    @Test
    void shouldUseOneInstancePerTransaction() {
        AuditTrailLogger auditTrailLogger1 = auditTrailLoggerProvider.get();
        AuditTrailLogger auditTrailLogger2 = transactionTemplate.execute(status -> auditTrailLoggerProvider.get());
        assertSame(auditTrailLogger1, auditTrailLogger2);
    }

    @Test
    void testCreateLogger2() {
        Task task = Mockito.mock(Task.class);
        AuditTrailLoggerFactory auditTrailLoggerFactory = new AuditTrailLoggerFactory() {
            @Override
            public Optional<Task> getTask() {
                return Optional.of(task);
            }
        };
        AuditTrailLogger auditTrailLogger = auditTrailLoggerFactory.createLogger(null);
        assertEquals(task, auditTrailLogger.getTask().get());
    }
}