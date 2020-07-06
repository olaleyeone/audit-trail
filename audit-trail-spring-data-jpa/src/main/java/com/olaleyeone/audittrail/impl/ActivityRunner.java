package com.olaleyeone.audittrail.impl;

import com.olaleyeone.audittrail.context.ActionWithResult;
import com.olaleyeone.audittrail.embeddable.Duration;
import com.olaleyeone.audittrail.entity.TaskActivity;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

public class ActivityRunner {

    private static final ActivityRunner INSTANCE = new ActivityRunner();

    public static <E> E startActivity(TaskContextImpl parentContext, TaskActivity taskActivity, ActionWithResult<E> action) throws Throwable {
        return INSTANCE._startActivity(parentContext, taskActivity, action);
    }

    private <E> E _startActivity(TaskContextImpl parentContext, TaskActivity taskActivity, ActionWithResult<E> action) throws Throwable {

        TaskContextImpl taskContext = new TaskContextImpl(taskActivity, parentContext.getTaskContextHolder(), parentContext.getTaskTransactionContextFactory());
        taskContext.start(parentContext);
        OffsetDateTime now = OffsetDateTime.now();
        taskActivity.setDuration(Duration.builder()
                .startedOn(now)
                .build());

        E result;
        try {
            result = action.get();
            taskActivity.setStatus(TaskActivity.Status.SUCCESSFUL);
            return result;
        } catch (Exception e) {
            CodeContextUtil.setFailurePoint(taskActivity, e);
            throw e;
        } finally {
            taskActivity.setDuration(Duration.builder()
                    .startedOn(now)
                    .nanoSecondsTaken(now.until(OffsetDateTime.now(), ChronoUnit.NANOS))
                    .build());
            taskContext.end();
        }
    }

}
