package com.olaleyeone.audittrail.impl;

import com.olaleyeone.audittrail.api.ActivityLogger;
import com.olaleyeone.audittrail.api.EntityOperation;
import com.olaleyeone.audittrail.api.EntityStateLogger;
import com.olaleyeone.audittrail.entity.AuditTrailActivity;
import com.olaleyeone.audittrail.entity.Task;
import com.olaleyeone.audittrail.entity.AuditTrail;
import com.olaleyeone.audittrail.error.NoActivityLogException;
import lombok.AccessLevel;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronization;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
public abstract class AuditTrailLogger implements TransactionSynchronization {

    @Getter(AccessLevel.NONE)
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AuditTrailLoggerDelegate auditTrailLoggerDelegate;
    private final EntityStateLogger entityStateLogger;
    private final ActivityLogger activityLogger;

    private final List<AuditTrailActivity> auditTrailActivities = new ArrayList<>();
    private final LocalDateTime startTime = LocalDateTime.now();

    public AuditTrailLogger(AuditTrailLoggerDelegate auditTrailLoggerDelegate) {
        this(auditTrailLoggerDelegate, new EntityStateLoggerImpl());
    }

    public AuditTrailLogger(AuditTrailLoggerDelegate auditTrailLoggerDelegate, EntityStateLogger entityStateLogger) {
        this.auditTrailLoggerDelegate = auditTrailLoggerDelegate;
        this.entityStateLogger = entityStateLogger;
        this.activityLogger = createActivityLogger(auditTrailActivities);
    }

    @Override
    public void beforeCommit(boolean readOnly) {
        checkHasActivityLog();
        List<EntityOperation> logs = entityStateLogger.getOperations();
        if (logs.isEmpty()) {
            logger.warn("No work done");
            return;
        }
        auditTrailLoggerDelegate.saveUnitOfWork(this, AuditTrail.Status.SUCCESSFUL);
    }

    public abstract Optional<Task> getTask();

    public ActivityLogger createActivityLogger(List<AuditTrailActivity> auditTrailActivities) {
        return new ActivityLoggerImpl(auditTrailActivities);
    }

    public boolean canCommitWithoutActivityLog() {
        return false;
    }

    public EntityStateLogger getEntityStateLogger() {
        checkHasActivityLog();
        return entityStateLogger;
    }

    @Override
    public void afterCompletion(int status) {
        if (status == TransactionSynchronization.STATUS_COMMITTED || auditTrailActivities.isEmpty()) {
            return;
        }
        auditTrailLoggerDelegate.saveFailure(this, status == TransactionSynchronization.STATUS_ROLLED_BACK
                ? AuditTrail.Status.ROLLED_BACK
                : AuditTrail.Status.UNKNOWN);
    }

    private void checkHasActivityLog() {
        if (!auditTrailActivities.isEmpty()) {
            return;
        }
        if (!canCommitWithoutActivityLog()) {
            throw new NoActivityLogException();
        }
        logger.warn("No activity log");
    }
}
