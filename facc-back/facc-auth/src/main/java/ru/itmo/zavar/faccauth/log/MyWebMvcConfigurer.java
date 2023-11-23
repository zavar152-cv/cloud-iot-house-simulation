package ru.itmo.zavar.faccauth.log;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class MyWebMvcConfigurer implements WebMvcConfigurer {
    @Autowired
    private ApplicationContext context;

    @Override
    public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
        var responseStatusExceptionResolver = new CustomResponseStatusExceptionResolver();
        responseStatusExceptionResolver.setMessageSource(context);
        responseStatusExceptionResolver.setWarnLogCategory(responseStatusExceptionResolver.getClass().getName());
        resolvers.add(0, responseStatusExceptionResolver);
    }
}
