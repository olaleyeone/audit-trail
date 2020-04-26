package com.olaleyeone.audittrail.advice;

import com.olaleyeone.audittrail.api.Activity;
import com.olaleyeone.audittrail.embeddable.Duration;
import com.olaleyeone.audittrail.entity.CodeInstruction;
import com.olaleyeone.audittrail.entity.TaskActivity;
import com.olaleyeone.audittrail.impl.TaskContextHolder;
import com.olaleyeone.audittrail.impl.TaskContextImpl;
import com.olaleyeone.audittrail.impl.TaskTransactionContextFactory;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Aspect
@RequiredArgsConstructor
public class ActivityAdvice implements ActivityPointCut {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final TaskContextHolder taskContextHolder;
    private final TaskTransactionContextFactory taskTransactionContextFactory;

    @Around("activityMethod()")
    public Object adviceActivityMethod(ProceedingJoinPoint jp) throws Throwable {
        TaskContextImpl parentContext = taskContextHolder.getObject();

        MethodSignature methodSignature = (MethodSignature) jp.getSignature();

        CodeInstruction entryPoint = CodeLocationUtil.getEntryPoint(methodSignature);
        Activity activity = CodeLocationUtil.getActivityAnnotation(methodSignature);

        TaskActivity taskActivity = createTaskActivity(parentContext, entryPoint, activity);
        return startActivity(taskActivity, jp, parentContext);
    }

    private Object startActivity(TaskActivity taskActivity, ProceedingJoinPoint jp, TaskContextImpl parentTaskContext) throws Throwable {


        TaskContextImpl taskContext = new TaskContextImpl(taskActivity, taskContextHolder, taskTransactionContextFactory);
        taskContext.start(parentTaskContext);

        LocalDateTime now = LocalDateTime.now();

        Object result;
        try {
            result = jp.proceed(jp.getArgs());
            taskActivity.setStatus(TaskActivity.Status.SUCCESSFUL);
            return result;
        } catch (Exception e) {
            CodeLocationUtil.setFailurePoint(taskActivity, e);
            throw e;
        } finally {
            taskActivity.setDuration(Duration.builder()
                    .startedOn(now)
                    .nanoSeconds(now.until(LocalDateTime.now(), ChronoUnit.NANOS))
                    .build());
            taskContext.end();
        }
    }

    private TaskActivity createTaskActivity(TaskContextImpl parentContext, CodeInstruction entryPoint, Activity activity) {
        TaskActivity taskActivity = new TaskActivity();
        taskActivity.setTask(parentContext.getTask());
        taskActivity.setParentActivity(parentContext.getTaskActivity().orElse(null));
        taskActivity.setName(activity.value());
        taskActivity.setStatus(TaskActivity.Status.IN_PROGRESS);
        taskActivity.setEntryPoint(entryPoint);
        return taskActivity;
    }

}
