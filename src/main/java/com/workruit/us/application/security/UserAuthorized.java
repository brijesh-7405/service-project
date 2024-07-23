/**
 * 
 */
package com.workruit.us.application.security;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.workruit.us.application.models.Role.Roles;

@Retention(RUNTIME)
@Target(METHOD)
public @interface UserAuthorized {
	Roles[] userRoles() default { Roles.AGENT };
}
