package com.olaleyeone.audittrail.impl;

import com.olaleyeone.audittrail.api.*;

import java.util.*;
import java.util.stream.Collectors;

public class EntityStateLoggerImpl implements EntityStateLogger {

    private final Set<EntityIdentifier> newEntries = new HashSet<>();
    private final Set<EntityIdentifier> removedEntries = new HashSet<>();

    private final Map<EntityIdentifier, Map<String, AuditData>> previousState = new HashMap<>();
    private final Map<EntityIdentifier, Map<String, AuditData>> currentState = new HashMap<>();

    @Override
    public void registerNewEntity(EntityIdentifier entityIdentifier) {
        this.newEntries.add(entityIdentifier);
    }

    @Override
    public void registerDeletedEntity(EntityIdentifier entityIdentifier) {
        this.removedEntries.add(entityIdentifier);
    }

    @Override
    public boolean isNew(EntityIdentifier identifier) {
        return newEntries.contains(identifier);
    }

    @Override
    public boolean isPreviousStateLoaded(EntityIdentifier identifier) {
        return previousState.containsKey(identifier);
    }

    @Override
    public void setPreviousState(EntityIdentifier identifier, Map<String, AuditData> state) {
        previousState.put(identifier, state);
    }

    @Override
    public void setCurrentState(EntityIdentifier identifier, Map<String, AuditData> state) {
        currentState.put(identifier, state);
    }

    @Override
    public List<EntityOperation> getOperations() {
        Set<EntityIdentifier> allEntries = new HashSet<>();
        allEntries.addAll(newEntries);
        allEntries.addAll(removedEntries);
        allEntries.addAll(previousState.keySet());
        allEntries.addAll(currentState.keySet());

        return allEntries.stream()
                .map(this::getEntityOperation)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<EntityOperation> getEntityOperation(EntityIdentifier entityIdentifier) {
        if (removedEntries.contains(entityIdentifier)) {
            if (newEntries.contains(entityIdentifier)) {
                return Optional.empty();
            }
            return Optional.of(new EntityOperation(entityIdentifier, OperationType.DELETE));
        } else if (newEntries.contains(entityIdentifier)) {
            return Optional.of(new NewEntityOperation(entityIdentifier, currentState.get(entityIdentifier)));
        }
        return Optional.of(new EntityUpdateOperation(entityIdentifier,
                previousState.get(entityIdentifier),
                currentState.get(entityIdentifier)));
    }
}
