package com.olaleyeone.audittrail.advice;

import com.olaleyeone.audittrail.api.Activity;
import com.olaleyeone.audittrail.entity.CodeInstruction;
import com.olaleyeone.audittrail.entity.TaskActivity;
import com.olaleyeone.audittrail.impl.ActivityRunner;
import com.olaleyeone.audittrail.impl.CodeLocationUtil;
import com.olaleyeone.audittrail.impl.TaskContextHolder;
import com.olaleyeone.audittrail.impl.TaskContextImpl;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
@RequiredArgsConstructor
public class ActivityAdvice implements ActivityPointCut {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final TaskContextHolder taskContextHolder;

    @Around("activityMethod()")
    public Object adviceActivityMethod(ProceedingJoinPoint jp) throws Throwable {
        TaskContextImpl parentContext = taskContextHolder.getObject();

        MethodSignature methodSignature = (MethodSignature) jp.getSignature();

        CodeInstruction entryPoint = CodeLocationUtil.getEntryPoint(methodSignature);
        Activity activity = CodeLocationUtil.getActivityAnnotation(methodSignature);

        TaskActivity taskActivity = createTaskActivity(parentContext, entryPoint, activity);
        return ActivityRunner.startActivity(parentContext, taskActivity, () -> jp.proceed(jp.getArgs()));
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
