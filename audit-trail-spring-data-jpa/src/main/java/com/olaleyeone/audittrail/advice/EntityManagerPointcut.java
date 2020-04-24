package com.olaleyeone.audittrail.advice;

import org.aspectj.lang.annotation.Pointcut;

interface EntityManagerPointcut {

    @Pointcut("execution(* javax.persistence.EntityManager.persist(Object))")
    default void persist() {
    }

    @Pointcut("execution(* javax.persistence.EntityManager.merge(Object))")
    default void merge() {
    }

    @Pointcut("execution(* javax.persistence.EntityManager.remove(Object))")
    default void remove() {
    }
}
