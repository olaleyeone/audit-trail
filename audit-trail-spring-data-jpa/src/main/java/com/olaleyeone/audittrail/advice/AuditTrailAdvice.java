/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.olaleyeone.audittrail.advice;

import com.olaleyeone.audittrail.api.EntityDataExtractor;
import com.olaleyeone.audittrail.api.EntityStateLogger;
import com.olaleyeone.audittrail.api.EntityIdentifier;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Provider;

@RequiredArgsConstructor
@Aspect
public class AuditTrailAdvice {

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final EntityDataExtractor entityDataExtractor;

    private final Provider<EntityStateLogger> entityUpdateLogProvider;

    @Pointcut("execution(* javax.persistence.EntityManager.persist(Object))")
    public void onSave() {
    }

    @Pointcut("execution(* javax.persistence.EntityManager.merge(Object))")
    public void onUpdate() {
    }

    @Pointcut("execution(* javax.persistence.EntityManager.remove(Object))")
    public void onRemove() {
    }

    @Around("onSave()")
    public Object adviceEntityCreation(ProceedingJoinPoint jp) throws Throwable {
        Object result = jp.proceed(jp.getArgs());
        Object entity = jp.getArgs()[0];

        EntityStateLogger entityStateLogger = entityUpdateLogProvider.get();
        EntityIdentifier entityIdentifier = entityDataExtractor.getIdentifier(entity);
        entityStateLogger.registerNewEntity(entityIdentifier);
        entityStateLogger.setCurrentState(entityIdentifier, entityDataExtractor.extractAttributes(entity));

        return result;
    }

    @Around("onUpdate()")
    public Object adviceEntityUpdate(ProceedingJoinPoint jp) throws Throwable {
        Object entity = jp.getArgs()[0];

        EntityIdentifier entityIdentifier = entityDataExtractor.getIdentifier(entity);
        EntityStateLogger entityStateLogger = entityUpdateLogProvider.get();
        if (!entityStateLogger.isNew(entityIdentifier) && !entityStateLogger.isPreviousStateLoaded(entityIdentifier)) {
            Object loadedEntity = entityDataExtractor.getEntityBeforeOperation(entityIdentifier);
            entityStateLogger.setPreviousState(entityIdentifier, entityDataExtractor.extractAttributes(loadedEntity));
        }

        entityStateLogger.setCurrentState(entityIdentifier, entityDataExtractor.extractAttributes(entity));

        return jp.proceed(jp.getArgs());
    }

    @Around("onRemove()")
    public Object adviceEntityDelete(ProceedingJoinPoint jp) throws Throwable {
        Object result = jp.proceed(jp.getArgs());
        Object entity = jp.getArgs()[0];

        EntityStateLogger entityStateLogger = entityUpdateLogProvider.get();
        entityStateLogger.registerDeletedEntity(entityDataExtractor.getIdentifier(entity));

        return result;
    }
}
