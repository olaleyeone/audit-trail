package com.olaleyeone.audittrail.impl;

import com.olalayeone.audittrailtest.DataFactory;
import com.olalayeone.audittrailtest.EntityTest;
import com.olaleyeone.audittrail.advice.EntityManagerAdvice;
import com.olaleyeone.audittrail.api.*;
import com.olaleyeone.audittrail.entity.*;
import com.olaleyeone.audittrail.repository.EntityStateAttributeRepository;
import com.olaleyeone.audittrail.repository.EntityStateRepository;
import com.olaleyeone.audittrail.repository.TaskTransactionRepository;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.metamodel.EntityType;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
class TaskTransactionLoggerTest extends EntityTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private TaskTransactionRepository taskTransactionRepository;

    @Autowired
    private EntityStateRepository entityStateRepository;

    @Autowired
    private EntityStateAttributeRepository entityStateAttributeRepository;

    @Autowired
    private EntityManagerAdvice entityManagerAdvice;

    @Autowired
    private DataFactory dataFactory;

    private TaskTransactionLogger taskTransactionLogger;

    private TaskTransactionContext taskTransactionContext;

    @BeforeEach
    void setUp() {
        taskTransactionLogger = new TaskTransactionLogger(entityManager);

        EntityStateLogger entityStateLogger = Mockito.mock(EntityStateLogger.class);
        List<EntityOperation> entityOperations = getEntityHistoryLogs();
        Mockito.doReturn(entityOperations).when(entityStateLogger).getOperations();

        taskTransactionContext = Mockito.mock(TaskTransactionContext.class);
        Mockito.doReturn(LocalDateTime.now()).when(taskTransactionContext).getStartTime();
        Mockito.doReturn(Collections.EMPTY_LIST).when(taskTransactionContext).getAuditTransactionActivities();
        Mockito.doReturn(entityStateLogger).when(taskTransactionContext).getEntityStateLogger();

        TaskActivity taskActivity = dataFactory.getTaskActivity();
        Mockito.doReturn(taskActivity).when(taskTransactionContext).getTaskActivity();
        Mockito.doReturn(taskActivity.getTask()).when(taskTransactionContext).getTask();
    }

    @AfterEach
    public void afterEach() throws Throwable {
        Mockito.verify(entityManagerAdvice, Mockito.never()).adviceEntityCreation(Mockito.any());
    }

    @Test
    void saveUnitOfWork() {

        taskTransactionLogger.saveUnitOfWork(taskTransactionContext, TaskTransaction.Status.COMMITTED);

        assertEquals(1, taskTransactionRepository.count());
        assertEquals(3, entityStateRepository.count());
        assertEquals(3, entityStateAttributeRepository.count());

        List<TaskTransaction> units = taskTransactionRepository.getAllByTask(taskTransactionContext.getTask());

        assertEquals(1, units.size());
        TaskTransaction taskTransaction = units.iterator().next();
        taskTransactionContext.getEntityStateLogger().getOperations().forEach(entityHistoryLog -> {
            EntityIdentifier entityIdentifier = entityHistoryLog.getEntityIdentifier();
            Optional<EntityState> optionalEntityHistory = entityStateRepository.getByUnitOfWork(taskTransaction, entityIdentifier.getEntityName(),
                    entityIdentifier.getPrimaryKey().toString());
            assertTrue(optionalEntityHistory.isPresent());
            EntityState entityState = optionalEntityHistory.get();
            entityHistoryLog.getAttributes().entrySet()
                    .forEach(entry -> assertTrue(entityStateAttributeRepository.getByEntityHistory(entityState, entry.getKey()).isPresent()));
        });
    }

    @Test
    void shouldSaveActivityLogs() {

        List<TaskActivity> taskActivities = Arrays.asList(dataFactory.getTaskActivity(false), dataFactory.getTaskActivity(false));
        Mockito.doReturn(taskActivities).when(taskTransactionContext).getAuditTransactionActivities();

        TaskTransaction taskTransaction = taskTransactionLogger.saveUnitOfWork(taskTransactionContext, TaskTransaction.Status.COMMITTED);
        assertEquals(TaskTransaction.Status.COMMITTED, taskTransaction.getStatus());
        taskActivities
                .forEach(taskActivity -> {
                    assertNotNull(taskActivity.getId());
                    assertEquals(taskTransaction, taskActivity.getTaskTransaction());
                    assertEquals(taskTransaction.getTaskActivity(), taskActivity.getParentActivity());
                    assertEquals(taskTransaction.getTask(), taskActivity.getTask());
                });
    }

    @Test
    void saveEntityHistory() {
        TaskTransaction taskTransaction = dataFactory.createTaskTransaction();
        OperationType operationType = OperationType.CREATE;
        EntityType<?> entityType = entityManager.getEntityManagerFactory().getMetamodel().entity(Task.class);
        EntityIdentifier entityIdentifier = new EntityIdentifierImpl(entityType, faker.number().randomDigit());
        EntityOperation historyLog = new EntityOperation(entityIdentifier, operationType);
        EntityState entityState = taskTransactionLogger.createEntityHistory(taskTransaction, historyLog);
        assertNotNull(entityState);
        assertNotNull(entityState.getId());
        assertEquals(taskTransaction, entityState.getTaskTransaction());
        assertEquals(operationType, entityState.getOperationType());

        assertEquals(entityType.getName(), entityState.getEntityName());
        assertEquals(entityIdentifier.getPrimaryKey().toString(), entityState.getEntityId());
    }

    @Test
    void saveEntityHistoryAttribute() {
        EntityState entityState = dataFactory.createEntityState();

        EntityAttributeData data = EntityAttributeData.builder()
                .value(new AuditDataImpl(faker.lordOfTheRings().character()))
                .previousValue(new AuditDataImpl(faker.lordOfTheRings().character()))
                .build();

        EntityStateAttribute attribute = taskTransactionLogger.createEntityHistoryAttribute(entityState, Pair.of(faker.funnyName().name(), data));
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
}