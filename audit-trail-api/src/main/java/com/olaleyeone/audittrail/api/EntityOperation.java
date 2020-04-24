package com.olaleyeone.audittrail.api;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
@Data
public class EntityOperation {

    private final EntityIdentifier entityIdentifier;
    private final OperationType operationType;

    private Map<String, EntityAttributeData> attributes;

    @Override
    public String toString() {
        if (attributes != null) {
            return String.format("%s %s (%s)", operationType, entityIdentifier, attributes);
        }
        return String.format("%s %s", operationType, entityIdentifier);
    }
}
