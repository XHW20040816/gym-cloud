package com.gym.common.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Aspect
@Component
public class LogAspect {

    @Around("execution(* com.gym..controller..*.*(..))")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        long startTime = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        String className = signature.getDeclaringTypeName();
        String methodName = signature.getName();

        String url = "";
        String httpMethod = "";
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                url = request.getRequestURL().toString();
                httpMethod = request.getMethod();
            }
        } catch (Exception ignored) {
        }

        log.info("【进入】{} {} -> {}.{}()", httpMethod, url, className, methodName);

        Object result;
        try {
            result = pjp.proceed();
        } catch (Throwable e) {
            long cost = System.currentTimeMillis() - startTime;
            log.warn("【异常】{}.{}() 耗时 {}ms，异常: {}", className, methodName, cost, e.getMessage());
            throw e;
        }

        long cost = System.currentTimeMillis() - startTime;
        log.info("【退出】{}.{}() 耗时 {}ms", className, methodName, cost);
        return result;
    }
}
