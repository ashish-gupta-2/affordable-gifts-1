package com.affordablegifts.backend.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Guards /api/admin/** with a shared secret sent as the X-Admin-Key header.
 * This is a stop-gap until real admin accounts and roles exist; with no key
 * configured every admin request is rejected.
 */
@Component
public class AdminApiKeyInterceptor implements HandlerInterceptor {

    static final String HEADER = "X-Admin-Key";

    private final byte[] expectedKey;

    public AdminApiKeyInterceptor(@Value("${app.admin.api-key:}") String apiKey) {
        this.expectedKey = apiKey.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String provided = request.getHeader(HEADER);
        boolean allowed = expectedKey.length > 0
                && provided != null
                && MessageDigest.isEqual(expectedKey, provided.getBytes(StandardCharsets.UTF_8));

        if (!allowed) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/problem+json");
            response.getWriter().write(
                    "{\"status\":401,\"title\":\"Unauthorized\",\"detail\":\"Missing or invalid admin key\"}");
            return false;
        }
        return true;
    }
}
