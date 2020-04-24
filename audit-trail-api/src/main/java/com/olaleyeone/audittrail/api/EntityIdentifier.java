package com.olaleyeone.audittrail.api;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

@Data
@RequiredArgsConstructor
public class EntityIdentifier implements Serializable {

    @NonNull
    private final Class<?> entityType;
    private final String entityName;
    @NonNull
    private final Serializable primaryKey;
}
