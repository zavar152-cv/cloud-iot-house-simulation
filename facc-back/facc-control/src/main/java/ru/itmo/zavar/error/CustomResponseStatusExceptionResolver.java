package ru.itmo.zavar.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.annotation.ResponseStatusExceptionResolver;
import ru.itmo.zavar.service.CloudLoggingService;
import yandex.cloud.api.logging.v1.LogEntryOuterClass;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "ResponseStatusException logs")
public class CustomResponseStatusExceptionResolver extends ResponseStatusExceptionResolver {

    private final CloudLoggingService cloudLoggingService;

    @Override
    protected ModelAndView doResolveException(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, Object handler, @NonNull Exception exception) {
        ModelAndView modelAndView = super.doResolveException(request, response, handler, exception);
        if (modelAndView != null) {
            String s = this.buildLogMessage(exception, request);
            log.warn(s + " for request: {} {} from {}", request.getMethod(), request.getRequestURI(), request.getRemoteAddr());
            cloudLoggingService.log(LogEntryOuterClass.LogLevel.Level.WARN, "ResponseStatusException logs",
                    s + " for request: {} {} from {}", request.getMethod(), request.getRequestURI(), request.getRemoteAddr());
        }
        return modelAndView;
    }
}
