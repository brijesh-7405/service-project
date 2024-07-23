/**
 * 
 */
package com.workruit.us.application.security;

import java.lang.reflect.Method;
import java.util.List;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.workruit.us.application.configuration.WorkruitAuthorizationException;
import com.workruit.us.application.dto.UserDetailsDTO;
import com.workruit.us.application.models.Role.Roles;

/**
 * @author Santosh
 *
 */
@Aspect
@Component
public class UserAuthorizedAspect {
	@SuppressWarnings("unlikely-arg-type")
	@Before("@annotation(com.workruit.us.application.security.UserAuthorized)")
	public void before(JoinPoint joinpoint) throws Exception {
		MethodInvocationProceedingJoinPoint methodInvocationProceedingJoinPoint = (MethodInvocationProceedingJoinPoint) joinpoint;
		MethodSignature signature = (MethodSignature) methodInvocationProceedingJoinPoint.getSignature();
		Method method = signature.getMethod();
		UserAuthorized userAuthorized = AnnotationUtils.findAnnotation(method, UserAuthorized.class);
		UserDetailsDTO userDetailsDTO = (UserDetailsDTO) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		if (userAuthorized != null) {
			List<String> roles = userDetailsDTO.getRoles();
			Roles[] methodRolesRequired = userAuthorized.userRoles();
			for (Roles role : methodRolesRequired) {
				if (roles.contains(role.name())) {
					return;
				}
			}
			throw new WorkruitAuthorizationException("");
		}
	}
}
