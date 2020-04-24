package com.olaleyeone.audittrail.impl;

import com.ComponentTest;
import com.olaleyeone.audittrail.entity.AuditTrailActivity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class AuditTrailActivityLoggerImplTest extends ComponentTest {

    private ActivityLoggerImpl activityLogger;

    @BeforeEach
    public void setUp(){
        activityLogger = new ActivityLoggerImpl(new ArrayList<>());
    }

    @Test
    void log() {
        activityLogger.log(faker.funnyName().name(), faker.backToTheFuture().quote());
        AuditTrailActivity auditTrailActivity = activityLogger.getAuditTrailActivities().iterator().next();
        assertEquals(getClass().getName(), auditTrailActivity.getClassName());
    }

    @Test
    void testLog() {
        activityLogger.log(faker.funnyName().name(), faker.backToTheFuture().quote());
        AuditTrailActivity auditTrailActivity = activityLogger.getAuditTrailActivities().iterator().next();
        assertEquals(getClass().getName(), auditTrailActivity.getClassName());
    }
}