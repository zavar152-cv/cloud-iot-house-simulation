package ru.itmo.zavar.faccauth.log;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Collection;
import java.util.function.Function;

@Component
@Slf4j(topic = "Request/Response logs")
public class LoggingFilterBean extends GenericFilterBean {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        ContentCachingRequestWrapper requestWrapper = requestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = responseWrapper(response);

        chain.doFilter(requestWrapper, responseWrapper);

        logRequest(requestWrapper);
        logResponse(responseWrapper);
    }

    private void logRequest(ContentCachingRequestWrapper request) {
        log.info("Received request: {} {} from {} with body\n{}", request.getMethod(), request.getRequestURI(), request.getRemoteAddr(),
                new String(request.getContentAsByteArray()).replace("\n","").replace(" ", ""));
    }

    private void logResponse(ContentCachingResponseWrapper response) throws IOException {
        String s = new String(response.getContentAsByteArray());
        if(s.isEmpty()) {
            log.info("Sent response: with status {}", response.getStatus());
        } else {
            log.info("Sent response: with status {} and body\n{}", response.getStatus(), new String(response.getContentAsByteArray()));
        }
        response.copyBodyToResponse();
    }

    private String headersToString(Collection<String> headerNames, Function<String, String> headerValueResolver) {
        StringBuilder builder = new StringBuilder();
        for (String headerName : headerNames) {
            String header = headerValueResolver.apply(headerName);
            builder.append("%s=%s".formatted(headerName, header)).append("\n");
        }
        return builder.toString();
    }

    private ContentCachingRequestWrapper requestWrapper(ServletRequest request) {
        if (request instanceof ContentCachingRequestWrapper requestWrapper) {
            return requestWrapper;
        }
        return new ContentCachingRequestWrapper((HttpServletRequest) request);
    }

    private ContentCachingResponseWrapper responseWrapper(ServletResponse response) {
        if (response instanceof ContentCachingResponseWrapper responseWrapper) {
            return responseWrapper;
        }
        return new ContentCachingResponseWrapper((HttpServletResponse) response);
    }
}
