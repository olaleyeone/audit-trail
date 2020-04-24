package com.olaleyeone.audittrail.impl;

import com.olalayeone.audittrailtest.EntityTest;
import com.olaleyeone.audittrail.entity.EntityState;
import com.olaleyeone.audittrail.entity.Task;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EntityIdentifierImplTest extends EntityTest {

    @Test
    void testToString() {
        EntityIdentifierImpl entityIdentifier = new EntityIdentifierImpl(entityManager.getMetamodel().entity(Task.class), 1);
        assertNotNull(entityIdentifier.toString());
    }

    @Test
    void testEqualsWithEqualValues() {
        EntityIdentifierImpl entityIdentifier1 = new EntityIdentifierImpl(entityManager.getMetamodel().entity(Task.class), 1);
        EntityIdentifierImpl entityIdentifier2 = new EntityIdentifierImpl(entityManager.getMetamodel().entity(Task.class), 1);
        assertEquals(entityIdentifier1, entityIdentifier2);
    }

    @Test
    void testEqualsWithUnequalIds() {
        EntityIdentifierImpl entityIdentifier1 = new EntityIdentifierImpl(entityManager.getMetamodel().entity(Task.class), 1);
        EntityIdentifierImpl entityIdentifier2 = new EntityIdentifierImpl(entityManager.getMetamodel().entity(Task.class), 2);
        assertNotEquals(entityIdentifier1, entityIdentifier2);
    }

    @Test
    void testEqualsWithUnequalEntities() {
        EntityIdentifierImpl entityIdentifier1 = new EntityIdentifierImpl(entityManager.getMetamodel().entity(Task.class), 1);
        EntityIdentifierImpl entityIdentifier2 = new EntityIdentifierImpl(entityManager.getMetamodel().entity(EntityState.class), 1);
        assertNotEquals(entityIdentifier1, entityIdentifier2);
    }
}