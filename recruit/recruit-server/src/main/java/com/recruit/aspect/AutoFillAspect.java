package com.recruit.aspect;

import com.recruit.annotation.AutoFill;
import com.recruit.context.BaseContext;
import com.recruit.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自动填充职位的创建、修改时间和操作人。
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    @Pointcut("execution(* com.recruit.mapper.*.*(..)) && @annotation(com.recruit.annotation.AutoFill)")
    public void autoFillPointCut() {
    }

    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length == 0 || args[0] == null) {
            return;
        }

        AutoFill autoFill = ((MethodSignature) joinPoint.getSignature())
                .getMethod()
                .getAnnotation(AutoFill.class);
        Object entity = args[0];
        Long currentUserId = BaseContext.getCurrentUserId();
        LocalDateTime now = LocalDateTime.now();

        if (autoFill.value() == OperationType.INSERT) {
            invoke(entity, "setCreateTime", LocalDateTime.class, now);
            invoke(entity, "setUpdateTime", LocalDateTime.class, now);
            invoke(entity, "setCreateUser", Long.class, currentUserId);
            invoke(entity, "setUpdateUser", Long.class, currentUserId);
        } else {
            invoke(entity, "setUpdateTime", LocalDateTime.class, now);
            invoke(entity, "setUpdateUser", Long.class, currentUserId);
        }
    }

    private void invoke(Object target, String methodName, Class<?> parameterType, Object value) {
        try {
            Method method = target.getClass().getMethod(methodName, parameterType);
            method.invoke(target, value);
        } catch (NoSuchMethodException ignored) {
            // 只对包含对应审计字段的实体执行填充。
        } catch (Exception ex) {
            log.warn("自动填充职位审计字段失败", ex);
        }
    }
}
