package com.olaleyeone.audittrail.impl;

import com.ComponentTest;
import com.olaleyeone.audittrail.context.TaskContext;
import com.olaleyeone.audittrail.entity.Failure;
import com.olaleyeone.audittrail.entity.Task;
import com.olaleyeone.audittrail.entity.TaskActivity;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

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
        validateTaskProperties(name, description, task);
    }

    @Test
    public void startBackgroundTaskWithError() {
        String name = faker.funnyName().name();
        String description = faker.backToTheFuture().quote();

        Exception exception = null;
        AtomicReference<TaskActivity> atomicReference = new AtomicReference<>();
        try {
            taskContextFactory.startBackgroundTask(name, description, () -> {
                TaskContextImpl taskContext = taskContextFactory.start(taskContextHolder.getObject().getTask());
                taskContext.execute("", () -> {
                    atomicReference.set(taskContextHolder.getObject().getTaskActivity().get());
                    Configuration configuration = new Configuration(Configuration.VERSION_2_3_22);
                    configuration.setDefaultEncoding("UTF-8");
                    configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

                    Template tpl = new Template(null, new StringReader("${oops}"), configuration);
                    ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    OutputStreamWriter writer = new OutputStreamWriter(bout);
                    tpl.process(Collections.EMPTY_MAP, writer);
                });
            });
        } catch (Exception e) {
            exception = e;
        }
        assertNotNull(exception);
        validateTaskProperties(name, description, taskContextHolder.getObject().getTask());
        assertNotNull(atomicReference.get());
        assertNotNull(atomicReference.get().getFailure());
        Failure failure = atomicReference.get().getFailure();
        assertNotNull(failure.getCodeContext());
        assertNotNull(failure.getCodeContext().getClassName());
        assertNotNull(failure.getCodeContext().getMethodName());
    }

    private void validateTaskProperties(String name, String description, Task task) {
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