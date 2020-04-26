package com.olaleyeone.audittrail.impl;

import com.ComponentTest;
import com.olaleyeone.audittrail.entity.Task;
import com.olaleyeone.audittrail.entity.TaskActivity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskContextFactoryTest extends ComponentTest {

    private TaskContextFactory taskContextFactory;
    private TaskContextHolder taskContextHolder;

    @BeforeEach
    void setUp() {
        taskContextHolder = new TaskContextHolder();
        taskContextFactory = new TaskContextFactory(taskContextHolder);
    }

    @Test
    void createContext() {
        Task task = new Task();
        TaskContext context = taskContextFactory.createContext(task);
        assertNotNull(context);
        assertSame(context, taskContextHolder.getObject());
    }

    @Test
    void testTwoLevels() {
        Task task = new Task();
        TaskContextImpl context1 = taskContextFactory.createContext(task);
        TaskActivity child = context1.doAndReturn(faker.lordOfTheRings().location(), faker.lordOfTheRings().character(), () -> {
            TaskContext context2 = taskContextHolder.getObject();
            assertNotSame(context1, context2);
            assertSame(context1.getTask(), context2.getTask());
            assertTrue(context2.getTaskActivity().isPresent());
            return context2.getTaskActivity().get();
        });
        assertTrue(context1.getTaskActivities().contains(child));
    }

    @Test
    void testSetDescription() {
        String description = faker.lordOfTheRings().location();
        Task task = new Task();
        TaskContextImpl context1 = taskContextFactory.createContext(task);
        TaskActivity child = context1.doAndReturn(faker.lordOfTheRings().location(), faker.lordOfTheRings().character(), () -> {
            TaskContext context2 = taskContextHolder.getObject();
            context2.setDescription(description);
            return context2.getTaskActivity().get();
        });
        assertEquals(description, child.getDescription());
    }

    @Test
    void testThreeLevels() {
        Task task = new Task();
        TaskContext context1 = taskContextFactory.createContext(task);
        context1.doAndReturn(faker.lordOfTheRings().location(), faker.lordOfTheRings().character(), () -> {
            TaskContext context2 = taskContextHolder.getObject();
            context2.doAndReturn(faker.lordOfTheRings().location(), () -> {
                TaskContext context3 = taskContextHolder.getObject();
                assertNotSame(context2, context3);
                assertSame(context2.getTask(), context3.getTask());
                assertNotSame(context2.getTaskActivity().get(), context3.getTaskActivity().get());
                assertNotNull(context3.getTaskActivity().get().getParentActivity());
                assertSame(context2.getTaskActivity().get(), context3.getTaskActivity().get().getParentActivity());
                return null;
            });
            return null;
        });
    }

    @Test
    void testActivityError() {
        Task task = new Task();
        TaskContextImpl context1 = taskContextFactory.createContext(task);
        assertThrows(RuntimeException.class, () -> context1.doAndReturn(faker.lordOfTheRings().location(), faker.lordOfTheRings().character(), () -> {
            throw new RuntimeException();
        }));
        assertFalse(context1.getTaskActivities().isEmpty());
        TaskActivity taskActivity = context1.getTaskActivities().iterator().next();
        assertEquals(TaskActivity.Status.FAILED, taskActivity.getStatus());
    }
}