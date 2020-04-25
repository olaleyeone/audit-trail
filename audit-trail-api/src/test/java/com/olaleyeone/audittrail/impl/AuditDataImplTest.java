package com.olaleyeone.audittrail.impl;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

    @Test
    void shouldHandleNullData() {
        AuditDataImpl auditData = new AuditDataImpl(null);
        assertEquals(Optional.empty(), auditData.getTextValue());
    }

    @Test
    void shouldHandleByteData() {
        String value = UUID.randomUUID().toString();
        AuditDataImpl auditData = new AuditDataImpl(value.getBytes());

        assertArrayEquals(value.getBytes(), auditData.getTextValue().map(it -> Base64.getDecoder().decode(it)).get());
    }

    @Test
    void shouldHandleByteData2() {
        String value = UUID.randomUUID().toString();
        AuditDataImpl auditData = new AuditDataImpl(toObjects(value.getBytes()));

        assertArrayEquals(value.getBytes(), auditData.getTextValue().map(it -> Base64.getDecoder().decode(it)).get());
    }

    Byte[] toObjects(byte[] bytesPrim) {
        Byte[] bytes = new Byte[bytesPrim.length];
        Arrays.setAll(bytes, n -> bytesPrim[n]);
        return bytes;
    }

}