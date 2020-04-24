package com.olaleyeone.audittrail.impl;

import com.olaleyeone.audittrail.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class EntityStateLoggerImplTest {

    private EntityStateLoggerImpl entityStateLogger;

    @BeforeEach
    void setUp() {
        entityStateLogger = new EntityStateLoggerImpl();
    }

    @Test
    void registerNewEntity() {
        EntityIdentifier entityIdentifier1 = new EntityIdentifier(String.class, String.class.getSimpleName(), 1);
        EntityIdentifier entityIdentifier2 = new EntityIdentifier(String.class, String.class.getSimpleName(), 2);
        entityStateLogger.registerNewEntity(entityIdentifier1);
        assertTrue(entityStateLogger.isNew(entityIdentifier1));
        assertFalse(entityStateLogger.isNew(entityIdentifier2));
    }

    @Test
    void registerDeletedEntity() {
        EntityIdentifier entityIdentifier = new EntityIdentifier(String.class, String.class.getSimpleName(), 1);
        entityStateLogger.registerDeletedEntity(entityIdentifier);

        List<EntityOperation> operations = entityStateLogger.getOperations();
        assertEquals(1, operations.size());
        EntityOperation entityOperation = operations.iterator().next();
        assertEquals(entityIdentifier, entityOperation.getEntityIdentifier());
        assertEquals(OperationType.DELETE, entityOperation.getOperationType());
    }

    @Test
    void isPreviousStateLoaded() {
        EntityIdentifier entityIdentifier1 = new EntityIdentifier(String.class, String.class.getSimpleName(), 1);
        EntityIdentifier entityIdentifier2 = new EntityIdentifier(String.class, String.class.getSimpleName(), 2);
        String attributeName = "test";
        AuditData attributeData1 = new AuditDataImpl("data1");
        entityStateLogger.setPreviousState(entityIdentifier1, getDataMap(attributeName, attributeData1));
        assertTrue(entityStateLogger.isPreviousStateLoaded(entityIdentifier1));
        assertFalse(entityStateLogger.isPreviousStateLoaded(entityIdentifier2));
    }

    @Test
    void logUpdateData() {
        EntityIdentifier entityIdentifier = new EntityIdentifier(String.class, String.class.getSimpleName(), 1);
        String attributeName = "test";
        AuditData attributeData1 = new AuditDataImpl("data1");
        AuditData attributeData2 = new AuditDataImpl("data2");

        entityStateLogger.setPreviousState(entityIdentifier, getDataMap(attributeName, attributeData1));
        entityStateLogger.setCurrentState(entityIdentifier, getDataMap(attributeName, attributeData2));

        List<EntityOperation> operations = entityStateLogger.getOperations();
        assertEquals(1, operations.size());
        EntityOperation entityOperation = operations.iterator().next();
        assertEquals(entityIdentifier, entityOperation.getEntityIdentifier());
        assertEquals(OperationType.UPDATE, entityOperation.getOperationType());

        assertTrue(entityOperation.getAttributes().containsKey(attributeName));
        EntityAttributeData entityAttributeData = entityOperation.getAttributes().get(attributeName);
        assertEquals(attributeData1, entityAttributeData.getPreviousValue());
        assertEquals(attributeData2, entityAttributeData.getValue());
    }

    @Test
    void logNewData() {
        EntityIdentifier entityIdentifier = new EntityIdentifier(String.class, String.class.getSimpleName(), 1);
        String attributeName = "test";
        AuditData attributeData1 = new AuditDataImpl("data1");
        AuditData attributeData2 = new AuditDataImpl("data2");

        entityStateLogger.registerNewEntity(entityIdentifier);
        entityStateLogger.setPreviousState(entityIdentifier, getDataMap(attributeName, attributeData1));
        entityStateLogger.setCurrentState(entityIdentifier, getDataMap(attributeName, attributeData2));

        List<EntityOperation> operations = entityStateLogger.getOperations();
        assertEquals(1, operations.size());
        EntityOperation entityOperation = operations.iterator().next();
        assertEquals(entityIdentifier, entityOperation.getEntityIdentifier());
        assertEquals(OperationType.CREATE, entityOperation.getOperationType());

        assertTrue(entityOperation.getAttributes().containsKey(attributeName));
        EntityAttributeData entityAttributeData = entityOperation.getAttributes().get(attributeName);

        assertNotEquals(attributeData1, entityAttributeData.getPreviousValue());
        assertNotNull(entityAttributeData.getPreviousValue());
        assertEquals(Optional.empty(), entityAttributeData.getPreviousValue().getData());

        assertEquals(attributeData2, entityAttributeData.getValue());
    }

    @Test
    void shouldHaveNoLogForRemovedNewRecords() {
        EntityIdentifier entityIdentifier = new EntityIdentifier(String.class, String.class.getSimpleName(), 1);
        entityStateLogger.registerDeletedEntity(entityIdentifier);
        entityStateLogger.registerNewEntity(entityIdentifier);

        assertTrue(entityStateLogger.getOperations().isEmpty());
    }

    private Map<String, AuditData> getDataMap(String attributeName, AuditData attributeData) {
        return Collections.singletonMap(attributeName, attributeData);
    }
}