package com.olaleyeone.audittrail.impl;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AuditDataImplTest {

    @Test
    void testGetData() {
        String value = UUID.randomUUID().toString();
        AuditDataImpl auditData = new AuditDataImpl(value);
        assertEquals(Optional.of(value), auditData.getData());
        assertEquals(Optional.of(value), auditData.getTextValue());
    }

    @Test
    void shouldHonorIgnoreData() {
        String value = UUID.randomUUID().toString();
        AuditDataImpl auditData = new AuditDataImpl(value, true);
        assertEquals(Optional.of(value), auditData.getData());
        assertEquals(Optional.empty(), auditData.getTextValue());
    }

}