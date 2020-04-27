package com.olaleyeone.audittrail.advice;

import org.aspectj.lang.annotation.Pointcut;

public interface ActivityPointCut {

    @Pointcut("@annotation(com.olaleyeone.audittrail.api.Activity)")
    default void activityMethod() {
        //not to be implemented
    }
}
