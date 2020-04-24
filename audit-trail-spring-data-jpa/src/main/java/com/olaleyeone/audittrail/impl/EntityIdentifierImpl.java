package com.olaleyeone.audittrail.impl;

import com.olaleyeone.audittrail.api.EntityIdentifier;

import javax.persistence.metamodel.EntityType;
import java.io.Serializable;

class EntityIdentifierImpl extends EntityIdentifier {

    public EntityIdentifierImpl(EntityType<?> entityType, Serializable identifier) {
        super(entityType.getJavaType(), entityType.getName(), identifier);
    }

    @Override
    public String toString() {
        return String.format("%s:%s", getEntityType().getName(), getPrimaryKey());
    }
}
