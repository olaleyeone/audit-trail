package com.olaleyeone.audittrail.api;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class EntityOperationTest {

    private EntityIdentifier entityIdentifier;

    @Test
    void testToStringWithoutAttributes() {
        String text = new EntityOperation(entityIdentifier, OperationType.CREATE).toString();
        assertTrue(text.contains(OperationType.CREATE.name()));
    }

    @Test
    void testToStringWithAttributes() {
        entityIdentifier = new EntityIdentifier(Object.class, Object.class.getSimpleName(), 1);
        EntityOperation entityOperation = new EntityOperation(entityIdentifier, OperationType.UPDATE);
        entityOperation.setAttributes(Collections.emptyMap());
        String text = entityOperation.toString();
        assertTrue(text.contains(OperationType.UPDATE.name()));
    }
}