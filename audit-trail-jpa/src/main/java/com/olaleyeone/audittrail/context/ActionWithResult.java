package com.olaleyeone.audittrail.context;

@FunctionalInterface
public interface ActionWithResult<T> {
    T get() throws Throwable;
}
