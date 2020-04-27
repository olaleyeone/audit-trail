package com.olaleyeone.audittrail.impl;

import com.olaleyeone.audittrail.api.Activity;
import com.olaleyeone.audittrail.entity.CodeInstruction;
import com.olaleyeone.audittrail.entity.TaskActivity;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.HashMap;
import java.util.Map;

public class CodeLocationUtil {

    private static final ThreadLocal<Map<Throwable, CodeInstruction>> errorThreadLocal = new ThreadLocal<>();

    public static Activity getActivityAnnotation(MethodSignature methodSignature) {
        return methodSignature.getMethod().getDeclaredAnnotation(Activity.class);
    }

    public static CodeInstruction getEntryPoint(MethodSignature methodSignature) {
        CodeInstruction entryPoint = new CodeInstruction();
        entryPoint.setClassName(methodSignature.getDeclaringTypeName());
        entryPoint.setMethodName(methodSignature.getName());
        entryPoint.setSignature(methodSignature.toLongString());
        return entryPoint;
    }

    public static void setFailurePoint(TaskActivity taskActivity, Exception e) {
        Throwable rootCause = getRootCause(e);
        taskActivity.setFailureType(rootCause.getClass().getName());
        taskActivity.setFailureReason(rootCause.getMessage());
        taskActivity.setFailurePoint(CodeLocationUtil.getOrigin(rootCause));
        taskActivity.setStatus(TaskActivity.Status.FAILED);
    }

    public static CodeInstruction getEntryPoint(StackTraceElement stackTraceElement) {
        CodeInstruction entryPoint = new CodeInstruction();
        entryPoint.setClassName(stackTraceElement.getClassName());
        entryPoint.setMethodName(stackTraceElement.getMethodName());
        entryPoint.setLineNumber(stackTraceElement.getLineNumber());
        return entryPoint;
    }

    private static CodeInstruction getOrigin(Throwable e) {
        Map<Throwable, CodeInstruction> errorInstructionMap = errorThreadLocal.get();
        if (errorInstructionMap != null) {
            CodeInstruction codeInstruction = errorInstructionMap.get(e);
            if (codeInstruction != null) {
                return codeInstruction;
            }
        } else {
            errorInstructionMap = new HashMap<>();
            errorThreadLocal.set(errorInstructionMap);
        }
        Throwable rootCause = getRootCause(e);
        CodeInstruction codeInstruction = getEntryPoint(rootCause.getStackTrace()[0]);
        errorInstructionMap.put(e, codeInstruction);
        return codeInstruction;
    }

    private static Throwable getRootCause(Throwable e) {
        Throwable rootCause = e;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }
        return rootCause;
    }
}
