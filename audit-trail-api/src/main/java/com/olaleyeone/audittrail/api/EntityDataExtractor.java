package com.olaleyeone.audittrail.api;

import java.util.Map;

public interface EntityDataExtractor {

    Map<String, AuditData> extractAttributes(Object entity);

    EntityIdentifier getIdentifier(Object entity);

    Object getEntityBeforeOperation(EntityIdentifier entityIdentifier);
}
