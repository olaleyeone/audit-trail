package com.olaleyeone.audittrail.impl;

import com.olalayeone.audittrailtest.EntityTest;
import com.olaleyeone.audittrail.advice.AuditTrailAdvice;
import com.olaleyeone.audittrail.api.*;
import com.olaleyeone.audittrail.entity.AuditTrail;
import com.olaleyeone.audittrail.entity.EntityState;
import com.olaleyeone.audittrail.entity.EntityStateAttribute;
import com.olaleyeone.audittrail.entity.Task;
import com.olaleyeone.audittrail.repository.AuditTrailRepository;
import com.olaleyeone.audittrail.repository.EntityStateAttributeRepository;
import com.olaleyeone.audittrail.repository.EntityStateRepository;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.metamodel.EntityType;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
class AuditTrailLoggerDelegateTest extends EntityTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private AuditTrailRepository auditTrailRepository;

    @Autowired
    private EntityStateRepository entityStateRepository;

    @Autowired
    private EntityStateAttributeRepository entityStateAttributeRepository;

    @Autowired
    private AuditTrailAdvice auditTrailAdvice;

    private AuditTrailLoggerDelegate auditTrailLoggerDelegate;

    private AuditTrailLogger auditTrailLogger;
    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {
        transactionTemplate = Mockito.spy(applicationContext.getBean(TransactionTemplate.class));
        auditTrailLoggerDelegate = new AuditTrailLoggerDelegate(entityManager, transactionTemplate);


        EntityStateLogger entityStateLogger = Mockito.mock(EntityStateLogger.class);
        List<EntityOperation> entityOperations = getEntityHistoryLogs();
        Mockito.doReturn(entityOperations).when(entityStateLogger).getOperations();

        auditTrailLogger = Mockito.mock(AuditTrailLogger.class);
        Mockito.doReturn(LocalDateTime.now()).when(auditTrailLogger).getStartTime();
        Mockito.doReturn(Collections.EMPTY_LIST).when(auditTrailLogger).getAuditTrailActivities();
        Mockito.doReturn(entityStateLogger).when(auditTrailLogger).getEntityStateLogger();

        Task task = new Task();
        task.setStartedOn(LocalDateTime.now());
        task.setName(faker.funnyName().name());
        task.setType(faker.app().name());
        entityManager.persist(task);
        Mockito.doReturn(Optional.of(task)).when(auditTrailLogger).getTask();
    }

    @AfterEach
    public void afterEach() throws Throwable {
        Mockito.verify(auditTrailAdvice, Mockito.never()).adviceEntityCreation(Mockito.any());
    }

    @Test
    void saveUnitOfWork() {

        auditTrailLoggerDelegate.saveUnitOfWork(auditTrailLogger, AuditTrail.Status.SUCCESSFUL);

        assertEquals(1, auditTrailRepository.count());
        assertEquals(3, entityStateRepository.count());
        assertEquals(3, entityStateAttributeRepository.count());

        List<AuditTrail> units = auditTrailRepository.getAllByRequest(auditTrailLogger.getTask().get());

        assertEquals(1, units.size());
        AuditTrail auditTrail = units.iterator().next();
        auditTrailLogger.getEntityStateLogger().getOperations().forEach(entityHistoryLog -> {
            EntityIdentifier entityIdentifier = entityHistoryLog.getEntityIdentifier();
            Optional<EntityState> optionalEntityHistory = entityStateRepository.getByUnitOfWork(auditTrail, entityIdentifier.getEntityName(),
                    entityIdentifier.getPrimaryKey().toString());
            assertTrue(optionalEntityHistory.isPresent());
            EntityState entityState = optionalEntityHistory.get();
            entityHistoryLog.getAttributes().entrySet()
                    .forEach(entry -> assertTrue(entityStateAttributeRepository.getByEntityHistory(entityState, entry.getKey()).isPresent()));
        });
    }

    @Test
    void shouldSaveActivityLogs() {
        ActivityLoggerImpl activityLogger = new ActivityLoggerImpl(new ArrayList<>());
        activityLogger.log(faker.funnyName().name(), faker.backToTheFuture().quote());
        activityLogger.log(faker.funnyName().name(), faker.backToTheFuture().quote());
        Mockito.doReturn(activityLogger.getAuditTrailActivities()).when(auditTrailLogger).getAuditTrailActivities();
        AuditTrail auditTrail = auditTrailLoggerDelegate.saveUnitOfWork(auditTrailLogger, AuditTrail.Status.SUCCESSFUL);
        assertEquals(AuditTrail.Status.SUCCESSFUL, auditTrail.getStatus());
        activityLogger.getAuditTrailActivities()
                .forEach(activityLog -> {
                    assertNotNull(activityLog.getId());
                    assertEquals(auditTrail, activityLog.getAuditTrail());
                });
    }

    @Test
    void saveErrorInNewTransaction() {
        Mockito.doCallRealMethod().when(transactionTemplate).execute(Mockito.any());
        auditTrailLoggerDelegate.saveFailure(auditTrailLogger, AuditTrail.Status.SUCCESSFUL);
        Mockito.verify(transactionTemplate, Mockito.times(1))
                .execute(Mockito.any());
        Mockito.verify(auditTrailLogger, Mockito.times(1))
                .getEntityStateLogger();
        Mockito.verify(auditTrailLogger, Mockito.atLeast(1))
                .getAuditTrailActivities();
        Mockito.verify(auditTrailLogger, Mockito.atLeast(1))
                .getStartTime();
    }

    @Test
    void shouldNotPropagateExceptionWhenSavingError() {
        Mockito.doThrow(IllegalArgumentException.class).when(transactionTemplate).execute(Mockito.any());
        auditTrailLoggerDelegate.saveFailure(null, AuditTrail.Status.SUCCESSFUL);
        Mockito.verify(transactionTemplate, Mockito.times(1))
                .execute(Mockito.any());
    }

    @Test
    void saveEntityHistory() {
        AuditTrail auditTrail = createAuditTrail();
        OperationType operationType = OperationType.CREATE;
        EntityType<?> entityType = entityManager.getEntityManagerFactory().getMetamodel().entity(Task.class);
        EntityIdentifier entityIdentifier = new EntityIdentifierImpl(entityType, faker.number().randomDigit());
        EntityOperation historyLog = new EntityOperation(entityIdentifier, operationType);
        EntityState entityState = auditTrailLoggerDelegate.createEntityHistory(auditTrail, historyLog);
        assertNotNull(entityState);
        assertNotNull(entityState.getId());
        assertEquals(auditTrail, entityState.getAuditTrail());
        assertEquals(operationType, entityState.getOperationType());

        assertEquals(entityType.getName(), entityState.getEntityName());
        assertEquals(entityIdentifier.getPrimaryKey().toString(), entityState.getEntityId());
    }

    @Test
    void saveEntityHistoryAttribute() {
        EntityState entityState = createEntityHistory();

        EntityAttributeData data = EntityAttributeData.builder()
                .value(new AuditDataImpl(faker.lordOfTheRings().character()))
                .previousValue(new AuditDataImpl(faker.lordOfTheRings().character()))
                .build();

        EntityStateAttribute attribute = auditTrailLoggerDelegate.createEntityHistoryAttribute(entityState, Pair.of(faker.funnyName().name(), data));
        assertNotNull(attribute);
        assertNotNull(attribute.getId());
        assertEquals(entityState, attribute.getEntityState());
        assertEquals(data.isModified(), attribute.isModified());
        assertEquals(data.getPreviousValue().getTextValue().get(), attribute.getPreviousValue());
        assertEquals(data.getValue().getTextValue().get(), attribute.getValue());
    }

    private List<EntityOperation> getEntityHistoryLogs() {
        EntityType<?> entityType = entityManager.getEntityManagerFactory().getMetamodel().getEntities().iterator().next();
        List<EntityOperation> entityOperations = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            EntityOperation historyLog = new EntityOperation(new EntityIdentifierImpl(entityType, i), OperationType.CREATE);
            Map<String, EntityAttributeData> dataMap = new HashMap<>();
            for (int j = 0; j < i; j++) {
                EntityAttributeData data = EntityAttributeData.builder()
                        .value(new AuditDataImpl(faker.lordOfTheRings().character()))
                        .previousValue(new AuditDataImpl(faker.lordOfTheRings().character()))
                        .build();
                dataMap.put(faker.funnyName().name(), data);
            }
            historyLog.setAttributes(dataMap);
            entityOperations.add(historyLog);
        }
        return entityOperations;
    }

    private AuditTrail createAuditTrail() {
        AuditTrail auditTrail = new AuditTrail();
        auditTrail.setStartedOn(auditTrailLogger.getStartTime());
        auditTrail.setStatus(AuditTrail.Status.SUCCESSFUL);
        auditTrail.setName(faker.funnyName().name());
        auditTrail.setEstimatedTimeTakenInNanos(faker.number().randomNumber());
        entityManager.persist(auditTrail);
        return auditTrail;
    }

    private EntityState createEntityHistory() {
        EntityState entityState = new EntityState();
        entityState.setAuditTrail(createAuditTrail());
        entityState.setOperationType(OperationType.CREATE);
        entityState.setEntityName(faker.funnyName().name());
        entityState.setEntityId(faker.number().digit());
        entityManager.persist(entityState);
        return entityState;
    }
}