package com.example.partymaker.server;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;

/**
 * Custom error handler for Jetty to handle common errors gracefully,
 * particularly NullPointerExceptions that may occur with session handling.
 */
public class JettyErrorHandler extends ErrorHandler {
    private static final Logger logger = LoggerFactory.getLogger(JettyErrorHandler.class);

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Check if this is a Google connectivity check
        String uri = request.getRequestURI();
        if (uri != null && uri.contains("generate_204")) {
            logger.debug("Handling generate_204 request");
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            baseRequest.setHandled(true);
            return;
        }

        // Log the error details
        int code = response.getStatus();
        String message = response.getHeader("Error-Message");
        
        if (code >= 500) {
            logger.error("Server error: {} - {} for URI: {}", code, message, uri);
        } else if (code >= 400) {
            logger.warn("Client error: {} - {} for URI: {}", code, message, uri);
        }

        // Set content type
        response.setContentType("application/json");
        
        // Write error response in JSON format
        try (Writer writer = response.getWriter()) {
            writer.write(String.format("{\"status\":\"error\",\"code\":%d,\"message\":\"%s\"}",
                    code, message != null ? escapeJsonString(message) : "Unknown error"));
        }
        
        baseRequest.setHandled(true);
    }
    
    /**
     * Simple method to escape JSON strings
     */
    private String escapeJsonString(String input) {
        if (input == null) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            switch (c) {
                case '\"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }
} 