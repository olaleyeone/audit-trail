package com.olaleyeone.audittrail.context;

import java.util.function.Supplier;

public interface TaskContext {

    <E> E execute(String name, String description, Supplier<E> action);

    <E> E execute(String name, Supplier<E> action);

    void setDescription(String description);
}
