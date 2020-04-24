package com.olaleyeone.audittrail.impl;

import com.olaleyeone.audittrail.api.*;

import java.util.HashMap;
import java.util.Map;

public class NewEntityOperation extends EntityOperation {

    public NewEntityOperation(EntityIdentifier entityIdentifier, Map<String, AuditData> data) {
        super(entityIdentifier, OperationType.CREATE);
        Map<String, EntityAttributeData> dataUpdateMap = new HashMap<>();

        data.entrySet().forEach(entry -> dataUpdateMap.put(entry.getKey(), EntityAttributeData.builder()
                .value(entry.getValue())
                .previousValue(new AuditDataImpl(null))
                .build()));

        setAttributes(dataUpdateMap);
    }
}
