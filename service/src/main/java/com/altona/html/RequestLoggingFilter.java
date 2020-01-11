package com.altona.html;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import javax.servlet.http.HttpServletRequest;

@Component
public class RequestLoggingFilter extends CommonsRequestLoggingFilter {

    private static final String ATTRIBUTE = "altona.timer";

    public RequestLoggingFilter() {
        setBeforeMessagePrefix("Received [");
        setAfterMessagePrefix("Returned [");
    }

    @Override
    protected boolean shouldLog(HttpServletRequest request) {
        return logger.isInfoEnabled();
    }

    @Override
    protected void beforeRequest(HttpServletRequest request, String message) {
        logger.info(message);
        request.setAttribute(ATTRIBUTE, System.currentTimeMillis());
    }

    @Override
    protected void afterRequest(HttpServletRequest request, String message) {
        long start = (long) request.getAttribute(ATTRIBUTE);
        logger.info(message + " in " + (System.currentTimeMillis() - start) + " ms");
    }

}
