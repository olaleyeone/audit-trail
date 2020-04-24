package com.olaleyeone.audittrail.impl;

import com.olaleyeone.audittrail.api.AuditData;
import com.olaleyeone.audittrail.api.EntityAttributeData;
import com.olaleyeone.audittrail.api.EntityIdentifier;
import com.olaleyeone.audittrail.api.EntityOperation;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EntityUpdateOperationTest {

    @Test
    public void testNewEntityOperationCreation() {
        EntityIdentifier entityIdentifier = Mockito.mock(EntityIdentifier.class);
        String attributeName = "test";

        AuditData attributeData1 = new AuditDataImpl("data1");
        AuditData attributeData2 = new AuditDataImpl("data2");

        EntityOperation entityOperation = new EntityUpdateOperation(entityIdentifier,
                getDataMap(attributeName, attributeData1),
                getDataMap(attributeName, attributeData2));
        assertEquals(entityIdentifier, entityOperation.getEntityIdentifier());
        assertNotNull(entityOperation.getAttributes());
        assertEquals(1, entityOperation.getAttributes().size());

        Map.Entry<String, EntityAttributeData> entityAttributeDataEntry = entityOperation.getAttributes().entrySet().iterator().next();

        assertEquals(attributeName, entityAttributeDataEntry.getKey());
        assertEquals(attributeData1, entityAttributeDataEntry.getValue().getPreviousValue());
        assertEquals(attributeData2, entityAttributeDataEntry.getValue().getValue());
    }

    private Map<String, AuditData> getDataMap(String attributeName, AuditData attributeData) {
        return Collections.singletonMap(attributeName, attributeData);
    }
}