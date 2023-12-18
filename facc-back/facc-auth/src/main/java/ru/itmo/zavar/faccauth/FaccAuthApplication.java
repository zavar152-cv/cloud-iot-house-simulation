package ru.itmo.zavar.faccauth;

import com.google.common.io.Resources;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import ru.itmo.zavar.faccauth.service.impl.CloudLoggingServiceImpl;
import yandex.cloud.sdk.ServiceFactory;
import yandex.cloud.sdk.auth.Auth;
import yandex.cloud.sdk.auth.provider.CredentialProvider;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Objects;

@SpringBootApplication
@Slf4j
public class FaccAuthApplication {

    @Value("${yandex.auth-key-file}")
    private String authKeyFile;

    @Value("${yandex.compute-engine}")
    private boolean computeEngine;

    public static void main(String[] args) {
        SpringApplication.run(FaccAuthApplication.class, args);
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

    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(Locale.ENGLISH);
        return slr;
    }

    @Bean
    public ServiceFactory yandexCloudServiceFactory() throws URISyntaxException, IOException {
        URL url = getClass().getResource(authKeyFile);
        String content = Resources.toString(url, StandardCharsets.UTF_8);
        CredentialProvider credentialProvider;
        if (computeEngine) {
            credentialProvider = Auth.computeEngineBuilder()
                    .build();
        } else {
            credentialProvider = Auth.apiKeyBuilder().fromJson(content)
                    .build();
        }
        log.info("yandexCloudServiceFactory is building");
        return ServiceFactory.builder()
                .credentialProvider(credentialProvider)
                .build();
    }
}
