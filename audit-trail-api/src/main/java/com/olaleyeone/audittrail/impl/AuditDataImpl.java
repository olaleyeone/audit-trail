package com.olaleyeone.audittrail.impl;

import com.olaleyeone.audittrail.api.AuditData;
import lombok.Data;
import lombok.RequiredArgsConstructor;

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
        return getData().map(Object::toString);
    }
}
