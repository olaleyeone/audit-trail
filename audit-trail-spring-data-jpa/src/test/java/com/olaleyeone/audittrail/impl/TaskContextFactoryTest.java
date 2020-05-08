package com.olaleyeone.audittrail.impl;

import com.ComponentTest;
import com.olaleyeone.audittrail.context.TaskContext;
import com.olaleyeone.audittrail.entity.Task;
import com.olaleyeone.audittrail.entity.TaskActivity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class TaskContextFactoryTest extends ComponentTest {

    private TaskContextFactory taskContextFactory;
    private TaskContextHolder taskContextHolder;

    @Mock
    private TaskContextSaver taskContextSaver;

    @Mock
    private TaskTransactionContextFactory taskTransactionContextFactory;

    @BeforeEach
    void setUp() {
        taskContextHolder = new TaskContextHolder();
        taskContextFactory = new TaskContextFactory(taskContextHolder, taskTransactionContextFactory, taskContextSaver);
    }

    @Test
    void createContext() {
        Task task = new Task();
        TaskContext context = taskContextFactory.start(task);
        assertNotNull(context);
        assertSame(context, taskContextHolder.getObject());
    }

    @Test
    void testTwoLevels() {
        Task task = new Task();
        TaskContextImpl initialContext = taskContextFactory.start(task);
        TaskContextImpl childContext = initialContext.executeAndReturn(faker.lordOfTheRings().location(), faker.lordOfTheRings().character(), () -> {
            TaskContextImpl context2 = taskContextHolder.getObject();
            assertNotSame(initialContext, context2);
            assertSame(initialContext.getTask(), context2.getTask());
            return context2;
        });
        assertTrue(initialContext.getChildren().contains(childContext));
        assertTrue(childContext.getTaskActivity().isPresent());
        TaskActivity taskActivity = childContext.getTaskActivity().get();
        assertNull(taskActivity.getParentActivity());
        assertTrue(initialContext.getTaskActivities().contains(taskActivity));
    }

    @Test
    void testSetDescription() {
        String description = faker.lordOfTheRings().location();
        Task task = new Task();
        TaskContextImpl context = taskContextFactory.start(task);
        context.setDescription(description);
        assertEquals(description, task.getDescription());
    }

    @Test
    void testSetDescriptionInChildContext() {
        String description = faker.lordOfTheRings().location();
        Task task = new Task();
        TaskContextImpl context1 = taskContextFactory.start(task);
        TaskActivity child = context1.executeAndReturn(faker.lordOfTheRings().location(), faker.lordOfTheRings().character(), () -> {
            TaskContextImpl context2 = taskContextHolder.getObject();
            context2.setDescription(description);
            return context2.getTaskActivity().get();
        });
        assertEquals(description, child.getDescription());
    }

    @Test
    void testThreeLevels() {
        Task task = new Task();
        TaskContextImpl context1 = taskContextFactory.start(task);
        context1.execute(faker.lordOfTheRings().location(), faker.lordOfTheRings().character(), () -> {
            TaskContextImpl context2 = taskContextHolder.getObject();
            context2.execute(faker.lordOfTheRings().location(), () -> {
                TaskContextImpl context3 = taskContextHolder.getObject();
                assertNotSame(context2, context3);
                assertSame(context2.getTask(), context3.getTask());
                assertNotSame(context2.getTaskActivity().get(), context3.getTaskActivity().get());
                assertNotNull(context3.getTaskActivity().get().getParentActivity());
                assertSame(context2.getTaskActivity().get(), context3.getTaskActivity().get().getParentActivity());
            });
        });
    }

    @Test
    void testActivityError() {
        Task task = new Task();
        TaskContextImpl context1 = taskContextFactory.start(task);
        assertThrows(RuntimeException.class, () -> context1.execute(faker.lordOfTheRings().location(), faker.lordOfTheRings().character(), () -> {
            throw new RuntimeException();
        }));
        assertFalse(context1.getTaskActivities().isEmpty());
        TaskActivity taskActivity = context1.getTaskActivities().iterator().next();
        assertEquals(TaskActivity.Status.FAILED, taskActivity.getStatus());
    }

    @Test
    public void startBackgroundTask() {
        String name = faker.funnyName().name();
        String description = faker.backToTheFuture().quote();
        Task task = taskContextFactory.startBackgroundTask(name, description, () -> {
        });
        assertNotNull(task);
        assertEquals(name, task.getName());
        assertEquals(description, task.getDescription());
        Mockito.verify(taskContextSaver, Mockito.times(1))
                .save(Mockito.argThat(argument -> {
                    assertSame(task, argument.getTask());
                    return true;
                }));
    }

    @Test
    public void startBackgroundTaskWithError() {
        String name = faker.funnyName().name();
        String description = faker.backToTheFuture().quote();
        Task task = taskContextFactory.startBackgroundTask(name, description, () -> {
            throw new RuntimeException();
        });
        assertNotNull(task);
        assertEquals(name, task.getName());
        assertEquals(description, task.getDescription());
        Mockito.verify(taskContextSaver, Mockito.times(1))
                .save(Mockito.argThat(argument -> {
                    assertSame(task, argument.getTask());
                    return true;
                }));
    }
}