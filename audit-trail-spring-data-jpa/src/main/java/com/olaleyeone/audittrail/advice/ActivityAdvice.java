package com.olaleyeone.audittrail.advice;

import com.olaleyeone.audittrail.api.Activity;
import com.olaleyeone.audittrail.entity.CodeInstruction;
import com.olaleyeone.audittrail.embeddable.Duration;
import com.olaleyeone.audittrail.entity.TaskActivity;
import com.olaleyeone.audittrail.impl.TaskContextHolder;
import com.olaleyeone.audittrail.impl.TaskContextImpl;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Aspect
@RequiredArgsConstructor
public class ActivityAdvice implements ActivityPointCut {

    private final TaskContextHolder taskContextHolder;

    @Around("activityMethod()")
    public Object adviceActivityMethod(ProceedingJoinPoint jp) throws Throwable {
        TaskContextImpl taskContext = taskContextHolder.getObject();

        MethodSignature methodSignature = (MethodSignature) jp.getSignature();

        CodeInstruction entryPoint = CodeLocationUtil.getEntryPoint(methodSignature);
        Activity activity = CodeLocationUtil.getActivityAnnotation(methodSignature);

        TaskActivity taskActivity = getTaskActivity(taskContext, entryPoint, activity);
        return startActivity(taskActivity, jp, taskContext);
    }

    private Object startActivity(TaskActivity taskActivity, ProceedingJoinPoint jp, TaskContextImpl parentTaskContext) throws Throwable {

        taskContextHolder.registerContext(new TaskContextImpl(taskActivity, taskContextHolder));
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
            parentTaskContext.resume();
        }
    }

    private TaskActivity getTaskActivity(TaskContextImpl taskContext, CodeInstruction entryPoint, Activity activity) {
        TaskActivity taskActivity = new TaskActivity();
        taskActivity.setTask(taskContext.getTask());
        taskActivity.setParentActivity(taskContext.getTaskActivity().orElse(null));
        taskActivity.setName(activity.value());
        taskActivity.setStatus(TaskActivity.Status.IN_PROGRESS);
        taskActivity.setEntryPoint(entryPoint);
        taskContext.addActivity(taskActivity);
        return taskActivity;
    }

}
