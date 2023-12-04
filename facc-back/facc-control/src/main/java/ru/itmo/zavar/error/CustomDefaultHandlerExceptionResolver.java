package ru.itmo.zavar.error;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;
import org.springframework.web.util.ContentCachingRequestWrapper;

@Component
@Slf4j(topic = "Server errors")
public class CustomDefaultHandlerExceptionResolver extends DefaultHandlerExceptionResolver {
    @Override
    protected ModelAndView doResolveException(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, Object handler, @NonNull Exception exception) {
        ContentCachingRequestWrapper requestWrapper = requestWrapper(request);

        log.error("Received request: {} {} from {} with body\n{}", requestWrapper.getMethod(), requestWrapper.getRequestURI(), requestWrapper.getRemoteAddr(),
                new String(requestWrapper.getContentAsByteArray()).replace("\n", "").replace(" ", ""));
        log.error("Got server error:", exception);

        return super.doResolveException(request, response, handler, exception);
    }

    private ContentCachingRequestWrapper requestWrapper(ServletRequest request) {
        if (request instanceof ContentCachingRequestWrapper requestWrapper) {
            return requestWrapper;
        }
        return new ContentCachingRequestWrapper((HttpServletRequest) request);
    }
}
