package com.olaleyeone.audittrail.context;

@FunctionalInterface
public interface Action {
    void execute() throws Throwable;
}
