package com.olaleyeone.audittrail.impl;

import com.olaleyeone.audittrail.api.ActivityLogger;
import com.olaleyeone.audittrail.entity.AuditTrailActivity;
import lombok.Data;

import java.util.List;

@Data
public final class ActivityLoggerImpl implements ActivityLogger {

    private final List<AuditTrailActivity> auditTrailActivities;

//    @Override
//    public void log(String name) {
//        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[2];
//        log(name, null, stackTraceElement);
//    }

    @Override
    public void log(String name, String description) {
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[2];
        log(name, description, stackTraceElement);
    }

    private void log(String name, String description, StackTraceElement stackTraceElement) {
        AuditTrailActivity auditTrailActivity = new AuditTrailActivity();
        auditTrailActivity.setName(name);
        auditTrailActivity.setDescription(description);
        auditTrailActivity.setClassName(stackTraceElement.getClassName());
        auditTrailActivity.setMethodName(stackTraceElement.getMethodName());
        auditTrailActivity.setLineNumber(stackTraceElement.getLineNumber());
        auditTrailActivity.setPrecedence(auditTrailActivities.size() + 1);
        auditTrailActivities.add(auditTrailActivity);
    }
}
