package com.olaleyeone.audittrail.context;

public interface TaskContext {

    <E> E execute(String name, String description, Action<E> action);

    <E> E execute(String name, Action<E> action);

    void setDescription(String description);
}
