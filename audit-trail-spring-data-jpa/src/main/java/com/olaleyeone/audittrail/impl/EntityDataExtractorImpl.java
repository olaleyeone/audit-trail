package com.olaleyeone.audittrail.impl;

import com.olaleyeone.audittrail.api.AuditData;
import com.olaleyeone.audittrail.api.EntityDataExtractor;
import com.olaleyeone.audittrail.api.EntityIdentifier;
import com.olaleyeone.audittrail.api.IgnoreData;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public abstract class EntityDataExtractorImpl implements EntityDataExtractor {

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final EntityManager entityManager;

    @Override
    public Map<String, AuditData> extractAttributes(Object entity) {
        EntityIdentifier entityIdentifier = getIdentifier(entity);
        EntityType<?> entityType = entityManager.getMetamodel().entity(entityIdentifier.getEntityType());
        Set<Attribute<?, ?>> attributes = (Set<Attribute<?, ?>>) entityType.getAttributes();
        return getAuditDataMap(entity, attributes);
    }

    @Override
    public EntityIdentifier getIdentifier(Object entity) {
        Class<?> type = getType(entity);
        EntityType<?> entityType = entityManager.getMetamodel().entity(type);
        Serializable identifier = getPrimaryKey(entity);
        EntityIdentifier entityIdentifier = new EntityIdentifierImpl(entityType, identifier);
        return entityIdentifier;
    }

    @Override
    public Object getEntityBeforeOperation(EntityIdentifier entityIdentifier) {
        return entityManager.find(entityIdentifier.getEntityType(), entityIdentifier.getPrimaryKey());
    }

    public abstract Class<?> getType(Object e);

    Map<String, AuditData> getEmbeddedData(Object embeddable) {
        EmbeddableType<?> embeddableType = entityManager.getMetamodel().embeddable(embeddable.getClass());
        Set<Attribute<?, ?>> attributes = (Set<Attribute<?, ?>>) embeddableType.getAttributes();
        return getAuditDataMap(embeddable, attributes);
    }

    Map<String, AuditData> getAuditDataMap(Object entity, Set<Attribute<?, ?>> attributes) {
        Map<String, AuditData> dataMap = new HashMap<>();
        for (Attribute<?, ?> attribute : attributes) {
            Object value = getMemberValue(entity, attribute.getJavaMember());
            if (attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.BASIC) {
                dataMap.put(attribute.getName(), new AuditDataImpl(value, ignoreData(attribute.getJavaMember())));
            } else if (attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.EMBEDDED) {
                if (value == null) {
                    dataMap.put(attribute.getName(), new AuditDataImpl(null));
                } else {
                    getEmbeddedData(value).entrySet()
                            .forEach(entry -> dataMap.put(attribute.getName() + "." + entry.getKey(), entry.getValue()));
                }
            } else if (attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.MANY_TO_ONE
                    || attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.ONE_TO_ONE) {
                Serializable identifier = value != null ? getPrimaryKey(value) : null;
                dataMap.put(attribute.getName(), new AuditDataImpl(identifier));
            } else {
                logger.warn("Ignored {} {}.{}", attribute.getPersistentAttributeType(),
                        attribute.getDeclaringType().getJavaType().getSimpleName(),
                        attribute.getName());
            }
        }
        return dataMap;
    }

    Serializable getPrimaryKey(Object value) {
        return (Serializable) entityManager.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(value);
    }

    @SneakyThrows
    Object getMemberValue(Object object, Member member) {
        if (member instanceof Field) {
            Field field = (Field) member;
            field.setAccessible(true);
            return field.get(object);
        } else {
            return ((Method) member).invoke(object);
        }
    }

    boolean ignoreData(Member member) {
        if (member instanceof Field) {
            Field field = (Field) member;
            return field.getAnnotation(IgnoreData.class) != null;
        } else {
            return ((Method) member).getAnnotation(IgnoreData.class) != null;
        }
    }
}
