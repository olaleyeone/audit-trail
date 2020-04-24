package com.olaleyeone.audittrail.api;

import com.olaleyeone.audittrail.impl.AuditDataImpl;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EntityAttributeDataTest {

    @Test
    void testIsModifiedWithNulls() {
        assertFalse(new EntityAttributeData(new AuditDataImpl(Optional.empty()), new AuditDataImpl(Optional.empty())).isModified());
    }

    @Test
    void testIsModifiedWithOneNulls() {
        assertTrue(new EntityAttributeData(new AuditDataImpl(Optional.empty()), new AuditDataImpl(Optional.of(""))).isModified());
    }

    @Test
    void testToString() {
        String text = UUID.randomUUID().toString();
        AuditData value = new AuditDataImpl(text);
        assertTrue(new EntityAttributeData(value, value).toString().contains(text));
    }
}