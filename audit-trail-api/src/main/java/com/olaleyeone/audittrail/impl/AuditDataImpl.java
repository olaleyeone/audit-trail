package com.olaleyeone.audittrail.impl;

import com.olaleyeone.audittrail.api.AuditData;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Optional;

@RequiredArgsConstructor
@Data
public class AuditDataImpl implements AuditData {

    private final Object value;
    private boolean ignoreData;

    public AuditDataImpl(Object value, boolean ignoreData) {
        this.value = value;
        this.ignoreData = ignoreData;
    }

    @Override
    public Optional<Object> getData() {
        return Optional.ofNullable(value);
    }

    @Override
    public Optional<String> getTextValue() {
        if (ignoreData) {
            return Optional.empty();
        }
        if (value == null) {
            return Optional.empty();
        }
        if (value instanceof byte[]) {
            return Optional.of(Base64.getEncoder().encodeToString((byte[]) value));
        }
        if (value instanceof Byte[]) {
            return Optional.of(Base64.getEncoder().encodeToString(toPrimitive((Byte[]) value)));
        }
        return Optional.of(value.toString());
    }

    private byte[] toPrimitive(Byte[] referenceBytes) {
        byte[] primitiveBytes = new byte[referenceBytes.length];
        for (int i = 0; i < referenceBytes.length; i++) {
            primitiveBytes[i] = referenceBytes[i];
        }
        return primitiveBytes;
    }
}
