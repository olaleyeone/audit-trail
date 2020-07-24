package com.olaleyeone.audittrail.impl;

import com.olaleyeone.audittrail.api.Activity;
import com.olaleyeone.audittrail.entity.CodeContext;
import com.olaleyeone.audittrail.entity.Failure;
import com.olaleyeone.audittrail.entity.TaskActivity;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.lang.reflect.SourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class CodeContextUtil {

    private static final Logger logger = LoggerFactory.getLogger(CodeContextUtil.class);
    private static final ThreadLocal<Map<Throwable, CodeContext>> errorThreadLocal = new ThreadLocal<>();

    private CodeContextUtil() {
    }

    public static Activity getActivityAnnotation(MethodSignature methodSignature) {
        return methodSignature.getMethod().getDeclaredAnnotation(Activity.class);
    }

    public static CodeContext getEntryPoint(StackTraceElement stackTraceElement) {
        CodeContext entryPoint = new CodeContext();
        entryPoint.setClassName(stackTraceElement.getClassName());
        entryPoint.setMethodName(stackTraceElement.getMethodName());
        entryPoint.setLineNumber(stackTraceElement.getLineNumber());
        return entryPoint;
    }

    public static CodeContext getEntryPoint(SourceLocation sourceLocation, MethodSignature methodSignature) {
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
        try {
            entryPoint.setLineNumber(sourceLocation.getLine());
        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
        }
        return entryPoint;
    }

    public static void setFailurePoint(TaskActivity taskActivity, Exception e) {
        taskActivity.setFailure(toFailure(e));
        taskActivity.setStatus(TaskActivity.Status.FAILED);
    }

    public static Failure toFailure(Throwable e) {
        Throwable rootCause = getRootCause(e);

        Failure failure = new Failure();
        failure.setFailureType(rootCause.getClass().getName());
        failure.setFailureReason(rootCause.getMessage());
        failure.setCodeContext(CodeContextUtil.getOrigin(rootCause));
        return failure;
    }

    private static CodeContext getOrigin(Throwable e) {
        Map<Throwable, CodeContext> errorInstructionMap = errorThreadLocal.get();
        if (errorInstructionMap != null) {
            CodeContext codeContext = errorInstructionMap.get(e);
            if (codeContext != null) {
                return codeContext;
            }
        }
        if (errorInstructionMap == null) {
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
