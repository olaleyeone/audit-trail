/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.olaleyeone.audittrail.advice;

import com.olaleyeone.audittrail.api.EntityDataExtractor;
import com.olaleyeone.audittrail.api.EntityIdentifier;
import com.olaleyeone.audittrail.api.EntityStateLogger;
import com.olaleyeone.audittrail.impl.TaskTransactionContext;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Provider;

@RequiredArgsConstructor
@Aspect
public class EntityManagerAdvice implements EntityManagerPointcut {

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final EntityDataExtractor entityDataExtractor;

    private final Provider<TaskTransactionContext> transactionContextProvider;

    @Around("persist()")
    public Object adviceEntityCreation(ProceedingJoinPoint jp) throws Throwable {
        Object result = jp.proceed(jp.getArgs());
        Object entity = jp.getArgs()[0];

        EntityStateLogger entityStateLogger = transactionContextProvider.get().getEntityStateLogger();
        EntityIdentifier entityIdentifier = entityDataExtractor.getIdentifier(entity);
        entityStateLogger.registerNewEntity(entityIdentifier);
        entityStateLogger.setCurrentState(entityIdentifier, entityDataExtractor.extractAttributes(entity));

        return result;
    }

    @Around("merge()")
    public Object adviceEntityUpdate(ProceedingJoinPoint jp) throws Throwable {
        Object entity = jp.getArgs()[0];

        EntityIdentifier entityIdentifier = entityDataExtractor.getIdentifier(entity);
        EntityStateLogger entityStateLogger = transactionContextProvider.get().getEntityStateLogger();
        if (!entityStateLogger.isNew(entityIdentifier) && !entityStateLogger.isPreviousStateLoaded(entityIdentifier)) {
            Object loadedEntity = entityDataExtractor.getEntityBeforeOperation(entityIdentifier);
            entityStateLogger.setPreviousState(entityIdentifier, entityDataExtractor.extractAttributes(loadedEntity));
        }

        entityStateLogger.setCurrentState(entityIdentifier, entityDataExtractor.extractAttributes(entity));

        return jp.proceed(jp.getArgs());
    }

    @Around("remove()")
    public Object adviceEntityDelete(ProceedingJoinPoint jp) throws Throwable {
        Object result = jp.proceed(jp.getArgs());
        Object entity = jp.getArgs()[0];

        EntityStateLogger entityStateLogger = transactionContextProvider.get().getEntityStateLogger();
        entityStateLogger.registerDeletedEntity(entityDataExtractor.getIdentifier(entity));

        return result;
    }

}
