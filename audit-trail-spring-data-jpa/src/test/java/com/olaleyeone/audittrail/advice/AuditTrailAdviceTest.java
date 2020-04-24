package com.olaleyeone.audittrail.advice;

import com.ComponentTest;
import com.olaleyeone.audittrail.api.AuditData;
import com.olaleyeone.audittrail.api.EntityDataExtractor;
import com.olaleyeone.audittrail.api.EntityStateLogger;
import com.olaleyeone.audittrail.api.EntityIdentifier;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;

import javax.inject.Provider;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.inOrder;

class AuditTrailAdviceTest extends ComponentTest {

    private AuditTrailAdvice auditTrailAdvice;

    @Mock
    private EntityDataExtractor entityDataExtractor;

    @Mock
    private EntityStateLogger entityStateLogger;

    private EntityIdentifier entityIdentifier;

    @BeforeEach
    public void setUp() {
        auditTrailAdvice = new AuditTrailAdvice(entityDataExtractor, new Provider<EntityStateLogger>() {

            @Override
            public EntityStateLogger get() {
                return entityStateLogger;
            }
        });
        entityIdentifier = new EntityIdentifier(Object.class, faker.funnyName().name(), faker.number().randomDigit());
    }

    @Test
    void adviceEntityCreation() throws Throwable {
        ProceedingJoinPoint proceedingJoinPoint = Mockito.mock(ProceedingJoinPoint.class);
        Object[] args = new Object[]{new Object()};
        Mockito.doReturn(args).when(proceedingJoinPoint).getArgs();

        Mockito.doReturn(entityIdentifier).when(entityDataExtractor).getIdentifier(Mockito.any());

        Map<String, AuditData> data = Mockito.mock(Map.class);
        Mockito.doReturn(data).when(entityDataExtractor).extractAttributes(Mockito.any());

        auditTrailAdvice.adviceEntityCreation(proceedingJoinPoint);

        Mockito.verify(proceedingJoinPoint, Mockito.times(1))
                .proceed(args);
        Mockito.verify(entityDataExtractor, Mockito.times(1))
                .getIdentifier(args[0]);
        Mockito.verify(entityDataExtractor, Mockito.times(1))
                .extractAttributes(args[0]);
        Mockito.verify(entityStateLogger, Mockito.times(1))
                .registerNewEntity(entityIdentifier);
        Mockito.verify(entityStateLogger, Mockito.times(1))
                .setCurrentState(entityIdentifier, data);
    }

    @Test
    void adviceEntityUpdate() throws Throwable {
        ProceedingJoinPoint proceedingJoinPoint = Mockito.mock(ProceedingJoinPoint.class);
        Object[] args = new Object[]{new Object()};
        Object expectedResult = new Object();
        Mockito.doReturn(args).when(proceedingJoinPoint).getArgs();
        Mockito.doReturn(expectedResult).when(proceedingJoinPoint).proceed(args);

        Mockito.doReturn(entityIdentifier).when(entityDataExtractor).getIdentifier(Mockito.any());

        Map<String, AuditData> data = Mockito.mock(Map.class);
        Mockito.doReturn(data).when(entityDataExtractor).extractAttributes(Mockito.any());

        Object actualResult = auditTrailAdvice.adviceEntityUpdate(proceedingJoinPoint);
        assertSame(expectedResult, actualResult);

        Mockito.verify(proceedingJoinPoint, Mockito.times(1))
                .proceed(args);
        Mockito.verify(entityDataExtractor, Mockito.times(1))
                .getIdentifier(args[0]);
        Mockito.verify(entityDataExtractor, Mockito.times(1))
                .extractAttributes(args[0]);
        Mockito.verify(entityStateLogger, Mockito.times(1))
                .setCurrentState(entityIdentifier, data);

        Mockito.verify(entityStateLogger, Mockito.never())
                .registerNewEntity(Mockito.any());
    }

    @Test
    void adviceEntityUpdateShouldSetLoadedState() throws Throwable {
        ProceedingJoinPoint proceedingJoinPoint = Mockito.mock(ProceedingJoinPoint.class);
        Object[] args = new Object[]{new Object()};
        Mockito.doReturn(args).when(proceedingJoinPoint).getArgs();

        Mockito.doReturn(entityIdentifier).when(entityDataExtractor).getIdentifier(Mockito.any());

        Object loadedEntity = new Object();
        Mockito.doReturn(loadedEntity).when(entityDataExtractor).getEntityBeforeOperation(Mockito.any());

        Map<String, AuditData> data = Mockito.mock(Map.class);
        Mockito.doReturn(data).when(entityDataExtractor).extractAttributes(Mockito.any());

        Mockito.doReturn(false).when(entityStateLogger).isPreviousStateLoaded(Mockito.any());
        Mockito.doReturn(false).when(entityStateLogger).isNew(Mockito.any());

        InOrder inOrder = inOrder(proceedingJoinPoint, entityDataExtractor, entityStateLogger);

        auditTrailAdvice.adviceEntityUpdate(proceedingJoinPoint);

        inOrder.verify(entityStateLogger, Mockito.times(1))
                .isNew(entityIdentifier);
        inOrder.verify(entityStateLogger, Mockito.times(1))
                .isPreviousStateLoaded(entityIdentifier);
        inOrder.verify(entityDataExtractor, Mockito.times(1))
                .getEntityBeforeOperation(entityIdentifier);
        inOrder.verify(entityStateLogger, Mockito.times(1))
                .setPreviousState(entityIdentifier, data);
        inOrder.verify(entityStateLogger, Mockito.times(1))
                .setCurrentState(entityIdentifier, data);
        inOrder.verify(proceedingJoinPoint, Mockito.times(1))
                .proceed(args);

    }

    @Test
    void adviceEntityUpdateShouldNotSetLoadedStateForNew() throws Throwable {
        ProceedingJoinPoint proceedingJoinPoint = Mockito.mock(ProceedingJoinPoint.class);
        Object[] args = new Object[]{new Object()};
        Mockito.doReturn(args).when(proceedingJoinPoint).getArgs();

        Mockito.doReturn(entityIdentifier).when(entityDataExtractor).getIdentifier(Mockito.any());

        Mockito.doReturn(true).when(entityStateLogger).isNew(Mockito.any());

        auditTrailAdvice.adviceEntityUpdate(proceedingJoinPoint);

        Mockito.verify(entityStateLogger, Mockito.never())
                .setPreviousState(Mockito.any(), Mockito.any());
    }

    @Test
    void adviceEntityUpdateShouldNotSetLoadedStateMoreThanOnce() throws Throwable {
        ProceedingJoinPoint proceedingJoinPoint = Mockito.mock(ProceedingJoinPoint.class);
        Object[] args = new Object[]{new Object()};
        Mockito.doReturn(args).when(proceedingJoinPoint).getArgs();

        Mockito.doReturn(entityIdentifier).when(entityDataExtractor).getIdentifier(Mockito.any());

        Mockito.doReturn(false).when(entityStateLogger).isNew(Mockito.any());
        Mockito.doReturn(true).when(entityStateLogger).isPreviousStateLoaded(Mockito.any());

        auditTrailAdvice.adviceEntityUpdate(proceedingJoinPoint);

        Mockito.verify(entityStateLogger, Mockito.never())
                .setPreviousState(Mockito.any(), Mockito.any());
    }

    @Test
    void adviceEntityDelete() throws Throwable {
        ProceedingJoinPoint proceedingJoinPoint = Mockito.mock(ProceedingJoinPoint.class);
        Object[] args = new Object[]{new Object()};
        Mockito.doReturn(args).when(proceedingJoinPoint).getArgs();

        Mockito.doReturn(entityIdentifier).when(entityDataExtractor).getIdentifier(Mockito.any());

        auditTrailAdvice.adviceEntityDelete(proceedingJoinPoint);

        Mockito.verify(proceedingJoinPoint, Mockito.times(1))
                .proceed(args);
        Mockito.verify(entityDataExtractor, Mockito.times(1))
                .getIdentifier(args[0]);
        Mockito.verify(entityStateLogger, Mockito.times(1))
                .registerDeletedEntity(entityIdentifier);

        Mockito.verify(entityDataExtractor, Mockito.never())
                .extractAttributes(Mockito.any());
        Mockito.verify(entityStateLogger, Mockito.never())
                .registerNewEntity(Mockito.any());
        Mockito.verify(entityStateLogger, Mockito.never())
                .setCurrentState(Mockito.any(), Mockito.any());
    }
}