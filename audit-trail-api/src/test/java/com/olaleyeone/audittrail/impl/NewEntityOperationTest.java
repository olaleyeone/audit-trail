package com.olaleyeone.audittrail.impl;

import com.olaleyeone.audittrail.api.AuditData;
import com.olaleyeone.audittrail.api.EntityAttributeData;
import com.olaleyeone.audittrail.api.EntityIdentifier;
import com.olaleyeone.audittrail.api.EntityOperation;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class NewEntityOperationTest {

    @Test
    public void testNewEntityOperationCreation() {
        EntityIdentifier entityIdentifier = Mockito.mock(EntityIdentifier.class);
        String attributeName = "test";
        AuditData attributeData = new AuditDataImpl("data");
        Map<String, AuditData> dataMap = Collections.singletonMap(attributeName, attributeData);

        EntityOperation entityOperation = new NewEntityOperation(entityIdentifier, dataMap);
        assertEquals(entityIdentifier, entityOperation.getEntityIdentifier());
        assertNotNull(entityOperation.getAttributes());
        assertEquals(1, entityOperation.getAttributes().size());

        Map.Entry<String, EntityAttributeData> entityAttributeDataEntry = entityOperation.getAttributes().entrySet().iterator().next();

        assertEquals(attributeName, entityAttributeDataEntry.getKey());
        assertEquals(attributeData, entityAttributeDataEntry.getValue().getValue());

        assertNotNull(entityAttributeDataEntry.getValue().getPreviousValue());
        assertEquals(Optional.empty(), entityAttributeDataEntry.getValue().getPreviousValue().getData());
    }
}