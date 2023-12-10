package ru.itmo.zavar.error;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ru.itmo.zavar.service.CloudLoggingService;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class FaccControlWebMvcConfigurer implements WebMvcConfigurer {
    private final ApplicationContext context;
    private final CloudLoggingService cloudLoggingService;

    @Override
    public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
        var resolver = new CustomResponseStatusExceptionResolver(cloudLoggingService);
        resolver.setMessageSource(context);
        resolver.setWarnLogCategory(resolver.getClass().getName());
        resolvers.add(0, resolver);
    }
}
