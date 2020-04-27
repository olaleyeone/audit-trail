package com.olaleyeone.audittrail.impl;

import com.olaleyeone.audittrail.api.Activity;
import com.olaleyeone.audittrail.entity.CodeContext;
import com.olaleyeone.audittrail.entity.TaskActivity;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CodeContextUtil {

    private static final ThreadLocal<Map<Throwable, CodeContext>> errorThreadLocal = new ThreadLocal<>();

    public static Activity getActivityAnnotation(MethodSignature methodSignature) {
        return methodSignature.getMethod().getDeclaredAnnotation(Activity.class);
    }

    public static CodeContext getEntryPoint(MethodSignature methodSignature) {
        CodeContext entryPoint = new CodeContext();
        entryPoint.setClassName(methodSignature.getDeclaringTypeName());
        entryPoint.setMethodName(methodSignature.getName());
        entryPoint.setMethodSignature(String.format("(%s)",
                Arrays.asList(methodSignature.getParameterTypes())
                        .stream().map(it -> {
                    if (it.isPrimitive()) {
                        return it.toString();
                    }
                    return it.getName();
                }).collect(Collectors.joining(", "))));
        return entryPoint;
    }

    public static void setFailurePoint(TaskActivity taskActivity, Exception e) {
        Throwable rootCause = getRootCause(e);
        taskActivity.setFailureType(rootCause.getClass().getName());
        taskActivity.setFailureReason(rootCause.getMessage());
        taskActivity.setFailurePoint(CodeContextUtil.getOrigin(rootCause));
        taskActivity.setStatus(TaskActivity.Status.FAILED);
    }

    public static CodeContext getEntryPoint(StackTraceElement stackTraceElement) {
        CodeContext entryPoint = new CodeContext();
        entryPoint.setClassName(stackTraceElement.getClassName());
        entryPoint.setMethodName(stackTraceElement.getMethodName());
        entryPoint.setLineNumber(stackTraceElement.getLineNumber());
        return entryPoint;
    }

    private static CodeContext getOrigin(Throwable e) {
        Map<Throwable, CodeContext> errorInstructionMap = errorThreadLocal.get();
        if (errorInstructionMap != null) {
            CodeContext codeContext = errorInstructionMap.get(e);
            if (codeContext != null) {
                return codeContext;
            }
        } else {
            errorInstructionMap = new HashMap<>();
            errorThreadLocal.set(errorInstructionMap);
        }
        Throwable rootCause = getRootCause(e);
        CodeContext codeContext = getEntryPoint(rootCause.getStackTrace()[0]);
        errorInstructionMap.put(e, codeContext);
        return codeContext;
    }

    private static Throwable getRootCause(Throwable e) {
        Throwable rootCause = e;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }
        return rootCause;
    }
}
