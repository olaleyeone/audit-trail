package com.olaleyeone.audittrail.advice;

import com.ComponentTest;
import com.olalayeone.audittrailtest.DataFactory;
import com.olaleyeone.audittrail.Audited;
import com.olaleyeone.audittrail.api.AuditData;
import com.olaleyeone.audittrail.api.EntityDataExtractor;
import com.olaleyeone.audittrail.api.EntityIdentifier;
import com.olaleyeone.audittrail.api.EntityStateLogger;
import com.olaleyeone.audittrail.embeddable.Audit;
import com.olaleyeone.audittrail.entity.TaskActivity;
import com.olaleyeone.audittrail.entity.WebRequest;
import com.olaleyeone.audittrail.impl.TaskContextImpl;
import com.olaleyeone.audittrail.impl.TaskTransactionContext;
import com.olaleyeone.audittrail.impl.TaskTransactionLogger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.inOrder;

class EntityManagerAdviceTest extends ComponentTest {

    @Mock
    private EntityDataExtractor entityDataExtractor;

    @Mock
    private EntityStateLogger entityStateLogger;

    @Mock
    private ProceedingJoinPoint proceedingJoinPoint;

    private EntityManagerAdvice entityManagerAdvice;

    private EntityIdentifier entityIdentifier;

    @Mock
    private TaskContextImpl taskContext;

    private TaskActivity taskActivity;

    private WebRequest webRequest;

    @BeforeEach
    public void setUp() {
        DataFactory dataFactory = new DataFactory();
        taskActivity = dataFactory.getTaskActivity(false);
        Mockito.doReturn(Optional.of(taskActivity)).when(taskContext).getTaskActivity();
        entityManagerAdvice = new EntityManagerAdvice(
                entityDataExtractor,
                () -> new TaskTransactionContext(taskContext, new TaskTransactionLogger(null), entityStateLogger));
        entityIdentifier = new EntityIdentifier(Object.class, faker.funnyName().name(), faker.number().randomDigit());

        webRequest = new WebRequest();
        webRequest.setUserId(faker.number().digit());
    }

    @Test
    void adviceEntityCreation() throws Throwable {
        Object[] args = new Object[]{new Object()};
        Mockito.doReturn(args).when(proceedingJoinPoint).getArgs();

        Mockito.doReturn(entityIdentifier).when(entityDataExtractor).getIdentifier(Mockito.any());

        Map<String, AuditData> data = Mockito.mock(Map.class);
        Mockito.doReturn(data).when(entityDataExtractor).extractAttributes(Mockito.any());

        entityManagerAdvice.adviceEntityCreation(proceedingJoinPoint);

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
        Object[] args = new Object[]{new Object()};
        Object expectedResult = new Object();
        Mockito.doReturn(args).when(proceedingJoinPoint).getArgs();
        Mockito.doReturn(expectedResult).when(proceedingJoinPoint).proceed(args);

        Mockito.doReturn(entityIdentifier).when(entityDataExtractor).getIdentifier(Mockito.any());

        Map<String, AuditData> data = Mockito.mock(Map.class);
        Mockito.doReturn(data).when(entityDataExtractor).extractAttributes(Mockito.any());

        Object actualResult = entityManagerAdvice.adviceEntityUpdate(proceedingJoinPoint);
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

        entityManagerAdvice.adviceEntityUpdate(proceedingJoinPoint);

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
        Object[] args = new Object[]{new Object()};
        Mockito.doReturn(args).when(proceedingJoinPoint).getArgs();

        Mockito.doReturn(entityIdentifier).when(entityDataExtractor).getIdentifier(Mockito.any());

        Mockito.doReturn(true).when(entityStateLogger).isNew(Mockito.any());

        entityManagerAdvice.adviceEntityUpdate(proceedingJoinPoint);

        Mockito.verify(entityStateLogger, Mockito.never())
                .setPreviousState(Mockito.any(), Mockito.any());
    }

    @Test
    void adviceEntityUpdateShouldNotSetLoadedStateMoreThanOnce() throws Throwable {
        createAudit();

        Mockito.doReturn(entityIdentifier).when(entityDataExtractor).getIdentifier(Mockito.any());
        Mockito.doReturn(false).when(entityStateLogger).isNew(Mockito.any());
        Mockito.doReturn(true).when(entityStateLogger).isPreviousStateLoaded(Mockito.any());

        entityManagerAdvice.adviceEntityUpdate(proceedingJoinPoint);

        Mockito.verify(entityStateLogger, Mockito.never())
                .setPreviousState(Mockito.any(), Mockito.any());
    }

    @Test
    void adviceEntityUpdateShouldSetCreatedByForNew() throws Throwable {
        Audit audit = createAudit();

        taskActivity.getTask().setWebRequest(webRequest);

        Mockito.doReturn(entityIdentifier).when(entityDataExtractor).getIdentifier(Mockito.any());

        entityManagerAdvice.adviceEntityCreation(proceedingJoinPoint);

        Mockito.verify(audit, Mockito.times(1))
                .setCreatedBy(webRequest.getUserId());
    }

    @Test
    void adviceEntityUpdateShouldSetUpdatedByForUpdate() throws Throwable {
        Audit audit = createAudit();

        taskActivity.getTask().setWebRequest(webRequest);

        Mockito.doReturn(entityIdentifier).when(entityDataExtractor).getIdentifier(Mockito.any());
        Mockito.doReturn(false).when(entityStateLogger).isNew(Mockito.any());

        entityManagerAdvice.adviceEntityUpdate(proceedingJoinPoint);

        Mockito.verify(audit, Mockito.times(1))
                .setLastUpdatedBy(webRequest.getUserId());
    }

    private Audit createAudit() {
        Audit audit = Mockito.mock(Audit.class);
        Object[] args = new Object[]{(Audited) () -> audit};
        Mockito.doReturn(args).when(proceedingJoinPoint).getArgs();
        return audit;
    }

    @Test
    void adviceEntityDelete() throws Throwable {
        Object[] args = new Object[]{new Object()};
        Mockito.doReturn(args).when(proceedingJoinPoint).getArgs();

        Mockito.doReturn(entityIdentifier).when(entityDataExtractor).getIdentifier(Mockito.any());

        entityManagerAdvice.adviceEntityDelete(proceedingJoinPoint);

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