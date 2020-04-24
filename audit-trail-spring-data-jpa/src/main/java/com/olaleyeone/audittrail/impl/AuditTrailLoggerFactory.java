package com.olaleyeone.audittrail.impl;

import com.olaleyeone.audittrail.entity.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Optional;

@RequiredArgsConstructor
public class AuditTrailLoggerFactory implements FactoryBean<AuditTrailLogger> {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private AuditTrailLoggerDelegate auditTrailLoggerDelegate;

    @PostConstruct
    public void init() {
        auditTrailLoggerDelegate = new AuditTrailLoggerDelegate(entityManager, transactionTemplate);
    }

    @Override
    public AuditTrailLogger getObject() {
        return (AuditTrailLogger) TransactionSynchronizationManager.getSynchronizations()
                .stream()
                .filter(it -> it instanceof AuditTrailLogger)
                .findFirst()
                .orElseGet(() -> {
                    AuditTrailLogger auditTrailLogger = createLogger(auditTrailLoggerDelegate);
                    TransactionSynchronizationManager.registerSynchronization(auditTrailLogger);
                    return auditTrailLogger;
                });
    }

    public AuditTrailLogger createLogger(AuditTrailLoggerDelegate auditTrailLoggerDelegate) {
        return new AuditTrailLogger(auditTrailLoggerDelegate) {
            @Override
            public Optional<Task> getTask() {
                return AuditTrailLoggerFactory.this.getTask();
            }
        };
    }

    public Optional<Task> getTask() {
        return Optional.empty();
    }

    @Override
    public Class<?> getObjectType() {
        return AuditTrailLogger.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
