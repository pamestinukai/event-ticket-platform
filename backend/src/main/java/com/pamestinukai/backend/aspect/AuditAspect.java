package com.pamestinukai.backend.aspects;

import com.pamestinukai.backend.entities.AuditLog;
import com.pamestinukai.backend.entities.Employee;
import com.pamestinukai.backend.repositories.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;

@Aspect
@Component
@ConditionalOnProperty(name = "app.audit.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;

    @Pointcut("execution(public * com.pamestinukai.backend.services.implementations.*.*(..))")
    public void serviceMethod() {}

    @Around("serviceMethod()")
    public Object auditServiceCall(ProceedingJoinPoint joinPoint) throws Throwable {
        String className  = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username    = resolveUsername(auth);
        String authorities = resolveAuthorities(auth);

        log.info("[AUDIT] user={} roles={} class={} method={} args={}",
                username, authorities, className, methodName,
                sanitizeArgs(joinPoint.getArgs()));

        Object result = joinPoint.proceed();

        if (isMutatingMethod(methodName) && auth != null && auth.getPrincipal() instanceof Employee employee) {
            persistAuditLog(employee, className, methodName, joinPoint.getArgs());
        }

        return result;
    }

    private void persistAuditLog(Employee employee, String className, String methodName, Object[] args) {
        try {
            AuditLog entry = new AuditLog();
            entry.setEmployee(employee);
            entry.setAction(resolveAction(methodName));
            entry.setEntityType(resolveEntityType(className));
            entry.setEntityId(resolveEntityId(args));
            entry.setChanges(className + "." + methodName);
            entry.setCreatedAt(LocalDateTime.now());
            auditLogRepository.save(entry);
        } catch (Exception ex) {
            log.error("[AUDIT] Failed to persist audit log for {}.{}: {}", className, methodName, ex.getMessage());
        }
    }

    private static AuditLog.EntityType resolveEntityType(String className) {
        if (className.contains("Event"))      return AuditLog.EntityType.EVENT;
        if (className.contains("TicketType")) return AuditLog.EntityType.TICKET_TYPE;
        if (className.contains("Auditorium")) return AuditLog.EntityType.AUDITORIUM;
        if (className.contains("SeatLayout")) return AuditLog.EntityType.SEAT_LAYOUT;
        return null; // other services (Auth, Employee, Org…) — column is nullable
    }

    private static Long resolveEntityId(Object[] args) {
        if (args == null) return null;
        for (Object arg : args) {
            if (arg instanceof Long id) return id;
        }
        return null;
    }

    private static boolean isMutatingMethod(String name) {
        return name.startsWith("create")
                || name.startsWith("update")
                || name.startsWith("delete")
                || name.startsWith("save")
                || name.startsWith("cancel")
                || name.startsWith("confirm")
                || name.startsWith("reserve")
                || name.startsWith("release")
                || name.startsWith("sync")
                || name.startsWith("checkIn")
                || name.startsWith("notify");
    }

    private static AuditLog.AuditAction resolveAction(String methodName) {
        if (methodName.startsWith("create") || methodName.startsWith("reserve")) {
            return AuditLog.AuditAction.CREATE;
        }
        if (methodName.startsWith("delete") || methodName.startsWith("cancel")) {
            return AuditLog.AuditAction.DELETE;
        }
        return AuditLog.AuditAction.UPDATE;
    }

    private static String resolveUsername(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return "anonymous";
        return auth.getName();
    }

    private static String resolveAuthorities(Authentication auth) {
        if (auth == null) return "[]";
        return auth.getAuthorities().toString();
    }

    private static String sanitizeArgs(Object[] args) {
        if (args == null || args.length == 0) return "[]";
        return Arrays.stream(args)
                .map(arg -> {
                    if (arg == null) return "null";
                    if (arg instanceof byte[]) return "[binary data]";
                    String str = arg.toString();
                    return str.length() > 200 ? str.substring(0, 200) + "…" : str;
                })
                .toList()
                .toString();
    }
}