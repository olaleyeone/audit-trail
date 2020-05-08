package com.olaleyeone.audittrail.context;

public interface TaskContext {

    <E> E executeAndReturn(String name, String description, ActionWithResult<E> action);

    <E> E executeAndReturn(String name, ActionWithResult<E> action);

    void execute(String name, String description, Action action);

    void execute(String name, Action action);

    void setDescription(String description);
}
