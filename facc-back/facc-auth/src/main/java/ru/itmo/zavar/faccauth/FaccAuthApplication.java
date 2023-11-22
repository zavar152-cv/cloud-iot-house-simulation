package ru.itmo.zavar.faccauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import ru.itmo.zavar.faccauth.error.CustomErrorAttributes;

@SpringBootApplication
public class FaccAuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(FaccAuthApplication.class, args);
	}

	@Bean
	public CustomErrorAttributes customErrorAttributes() {
		return new CustomErrorAttributes();
	}

	@Bean
	public RoleHierarchy roleHierarchy() {
		var hierarchy = new RoleHierarchyImpl();
		hierarchy.setHierarchy("ROLE_ADMIN > ROLE_USER");
		return hierarchy;
	}

	@Bean
	public MethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy) {
		DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
		expressionHandler.setRoleHierarchy(roleHierarchy);
		return expressionHandler;
	}

}
