package ru.itmo.zavar.error;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class FaccControlWebMvcConfigurer implements WebMvcConfigurer {
    @Autowired
    private ApplicationContext context;

    @Override
    public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
        var resolver = new CustomResponseStatusExceptionResolver();
        resolver.setMessageSource(context);
        resolver.setWarnLogCategory(resolver.getClass().getName());
        resolvers.add(0, resolver);
    }
}
