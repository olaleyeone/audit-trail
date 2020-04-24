package com.olaleyeone.audittrail.api;

import java.util.List;
import java.util.Map;

public interface EntityStateLogger {

    void registerNewEntity(EntityIdentifier entityIdentifier);

    void registerDeletedEntity(EntityIdentifier entityIdentifier);

    boolean isNew(EntityIdentifier identifier);

    boolean isPreviousStateLoaded(EntityIdentifier identifier);

    void setPreviousState(EntityIdentifier identifier, Map<String, AuditData> state);

    void setCurrentState(EntityIdentifier identifier, Map<String, AuditData> state);

    List<EntityOperation> getOperations();
}
