package com.olaleyeone.audittrail.context;

@FunctionalInterface
public interface Action<T> {
    T get() throws Throwable;
}
