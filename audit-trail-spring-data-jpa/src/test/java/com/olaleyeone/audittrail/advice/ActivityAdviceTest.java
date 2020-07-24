package com.olaleyeone.audittrail.advice;

import com.ComponentTest;
import com.olaleyeone.audittrail.api.Activity;
import com.olaleyeone.audittrail.entity.Failure;
import com.olaleyeone.audittrail.entity.Task;
import com.olaleyeone.audittrail.entity.TaskActivity;
import com.olaleyeone.audittrail.impl.CodeContextUtil;
import com.olaleyeone.audittrail.impl.TaskContextHolder;
import com.olaleyeone.audittrail.impl.TaskContextImpl;
import com.olaleyeone.audittrail.impl.TaskTransactionContextFactory;
import lombok.NoArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.lang.reflect.SourceLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ActivityAdviceTest extends ComponentTest {

    private TaskContextHolder taskContextHolder;
    private ActivityAdvice activityAdvice;

    private ProceedingJoinPoint proceedingJoinPoint;

    private Task task;

    @Mock
    private TaskTransactionContextFactory taskTransactionContextFactory;

    @BeforeEach
    public void setUp() throws NoSuchMethodException {
        taskContextHolder = new TaskContextHolder();
        activityAdvice = new ActivityAdvice(taskContextHolder);

        proceedingJoinPoint = Mockito.mock(ProceedingJoinPoint.class);
        Object[] args = new Object[]{new Object()};
        Mockito.doReturn(args).when(proceedingJoinPoint).getArgs();

        Method method = Target.class.getMethod("activity");

        MethodSignature methodSignature = Mockito.mock(MethodSignature.class);
        Mockito.doReturn(method).when(methodSignature).getMethod();
        Mockito.doReturn(method.getParameterTypes()).when(methodSignature).getParameterTypes();

        Mockito.doReturn(methodSignature).when(proceedingJoinPoint).getSignature();

        Mockito.doReturn(Mockito.mock(SourceLocation.class)).when(proceedingJoinPoint).getSourceLocation();
        task = new Task();
    }

    @Test
    void activityMethodInvoked() throws Throwable {

        TaskContextImpl taskContext = new TaskContextImpl(task, null, taskContextHolder, taskTransactionContextFactory);
        taskContext.start(null);

        activityAdvice.adviceActivityMethod(proceedingJoinPoint);

        assertSame(taskContext, taskContextHolder.getObject());
        assertEquals(1, taskContext.getTaskActivities().size());

        TaskActivity taskActivity = taskContext.getTaskActivities().iterator().next();
        assertSame(task, taskActivity.getTask());
        assertEquals(1, taskActivity.getPrecedence());
        assertEquals(TaskActivity.Status.SUCCESSFUL, taskActivity.getStatus());

        assertEquals(taskActivity.getEntryPoint(), CodeContextUtil.getEntryPoint(proceedingJoinPoint.getSourceLocation(), (MethodSignature) proceedingJoinPoint.getSignature()));
    }

    @Test
    void activityMethodThrowsError() throws Throwable {

        TaskContextImpl taskContext = new TaskContextImpl(task, null, taskContextHolder, taskTransactionContextFactory);
        taskContext.start(null);

        Mockito.doAnswer(invocation -> {
            new Target().activity();
            return null;
        }).when(proceedingJoinPoint).proceed(Mockito.any());

        try {
            activityAdvice.adviceActivityMethod(proceedingJoinPoint);
            fail("Exception expected");
        } catch (Exception e) {

            assertSame(taskContext, taskContextHolder.getObject());
            assertEquals(1, taskContext.getTaskActivities().size());

            TaskActivity taskActivity = taskContext.getTaskActivities().iterator().next();
            assertSame(task, taskActivity.getTask());
            assertEquals(1, taskActivity.getPrecedence());
            assertEquals(TaskActivity.Status.FAILED, taskActivity.getStatus());

            assertEquals(taskActivity.getEntryPoint(), CodeContextUtil.getEntryPoint(proceedingJoinPoint.getSourceLocation(), (MethodSignature) proceedingJoinPoint.getSignature()));
            Failure failure = taskActivity.getFailure();
            assertNotNull(failure);
            assertEquals(Delegate.class.getName(), failure.getCodeContext().getClassName());
            assertEquals("error", failure.getCodeContext().getMethodName());
        }
    }

    @Test
    void testPropagatedError() throws Throwable {

        TaskContextImpl taskContext1 = new TaskContextImpl(task, null, taskContextHolder, taskTransactionContextFactory);
        taskContextHolder.registerContext(taskContext1);

        List<TaskActivity> list = new ArrayList<>();

        Mockito.doAnswer(invocation -> {
            TaskContextImpl taskContext2 = taskContextHolder.getObject();
            list.add(taskContext2.getTaskActivity().get());
            taskContext2.execute("abc", () -> {
                list.add(taskContextHolder.getObject().getTaskActivity().get());
                new Target().activity();
            });
            return null;
        }).when(proceedingJoinPoint).proceed(Mockito.any());

        try {
            activityAdvice.adviceActivityMethod(proceedingJoinPoint);
            fail("Exception expected");
        } catch (Exception e) {
            Iterator<TaskActivity> iterator = list.iterator();
            TaskActivity taskActivity1 = iterator.next();
            TaskActivity taskActivity2 = iterator.next();
            assertNotNull(taskActivity1.getFailure());
            assertNotNull(taskActivity2.getFailure());
            assertEquals(taskActivity1.getFailure().getCodeContext(), taskActivity2.getFailure().getCodeContext());
        }
    }

    @NoArgsConstructor
    private static class Target {

        @Activity("Test")
        public void activity() {
            try {
                new Delegate().error();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class Delegate {

        void error() throws Exception {
            throw new Exception();
        }
    }
}