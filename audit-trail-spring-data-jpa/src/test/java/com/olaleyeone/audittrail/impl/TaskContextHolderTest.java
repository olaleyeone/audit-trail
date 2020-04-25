package com.olaleyeone.audittrail.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class TaskContextHolderTest {

    private TaskContextHolder taskContextHolder;

    @BeforeEach
    public void setUp() {
        taskContextHolder = new TaskContextHolder();
    }

    @Test
    void getObject() {
        TaskContext mock = Mockito.mock(TaskContext.class);
        taskContextHolder.registerContext(mock);
        assertSame(mock, taskContextHolder.getObject());
    }

    @Test
    void isSingleton() {
        assertFalse(taskContextHolder.isSingleton());
    }

    @Test
    void getObjectType() {
        assertEquals(TaskContext.class, taskContextHolder.getObjectType());
    }
}